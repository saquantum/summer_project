/* eslint-disable @typescript-eslint/no-explicit-any */

import { mount, flushPromises } from '@vue/test-utils'
import SelectUserDialog from '@/components/dialog/SelectUserDialog.vue'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick } from 'vue'

config.global.config.warnHandler = () => {}

// Mock Element Plus Message
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn()
    }
  }
})

// Mock API functions
vi.mock('@/api/admin', () => ({
  adminGetUsersTotalService: vi.fn(),
  adminSearchUsersService: vi.fn()
}))

// Import the mocked functions for use in tests
import { adminGetUsersTotalService, adminSearchUsersService } from '@/api/admin'

const mockAdminGetUsersTotalService = vi.mocked(adminGetUsersTotalService)
const mockAdminSearchUsersService = vi.mocked(adminSearchUsersService)

// Mock child components
vi.mock('@/components/search/UserSearch.vue', () => ({
  default: {
    name: 'UserSearch',
    template: '<div data-test="user-search">User Search Component</div>',
    props: ['fetchTableData', 'userSearchBody']
  }
}))

vi.mock('@/components/search/SortTool.vue', () => ({
  default: {
    name: 'SortTool',
    template: '<div data-test="sort-tool">Sort Tool Component</div>',
    props: ['multiSort', 'columns', 'fetchTableData']
  }
}))

// Mock data
const mockUserItems = [
  {
    user: {
      id: 'user1',
      name: 'John Doe',
      password: null,
      admin: false,
      adminLevel: 0,
      avatar: '',
      accessControlGroup: {
        rowId: 'group1',
        name: 'Regular Users',
        description: 'Regular user permissions',
        canCreateAsset: true,
        canSetPolygonOnCreate: false,
        canUpdateAssetFields: true,
        canUpdateAssetPolygon: false,
        canDeleteAsset: false,
        canUpdateProfile: true
      },
      address: {
        country: 'UK',
        city: 'London',
        street: '123 Main St',
        postcode: 'SW1A 1AA'
      },
      contactDetails: {
        email: 'john@example.com',
        phone: '+44123456789'
      },
      contactPreferences: {
        whatsapp: true,
        discord: false,
        post: false,
        phone: true,
        telegram: false,
        email: true
      }
    },
    accumulation: 5
  },
  {
    user: {
      id: 'user2',
      name: 'Jane Smith',
      password: null,
      admin: true,
      adminLevel: 1,
      avatar: '',
      accessControlGroup: {
        rowId: 'group2',
        name: 'Administrators',
        description: 'Admin permissions',
        canCreateAsset: true,
        canSetPolygonOnCreate: true,
        canUpdateAssetFields: true,
        canUpdateAssetPolygon: true,
        canDeleteAsset: true,
        canUpdateProfile: true
      },
      address: {
        country: 'UK',
        city: 'Manchester',
        street: '456 Oak St',
        postcode: 'M1 1AA'
      },
      contactDetails: {
        email: 'jane@example.com',
        phone: '+44987654321'
      },
      contactPreferences: {
        whatsapp: false,
        discord: true,
        post: false,
        phone: true,
        telegram: false,
        email: true
      }
    },
    accumulation: 10
  },
  {
    user: {
      id: 'user3',
      name: null,
      password: null,
      admin: false,
      adminLevel: 0,
      avatar: '',
      accessControlGroup: {
        rowId: 'group1',
        name: 'Regular Users',
        description: 'Regular user permissions',
        canCreateAsset: true,
        canSetPolygonOnCreate: false,
        canUpdateAssetFields: true,
        canUpdateAssetPolygon: false,
        canDeleteAsset: false,
        canUpdateProfile: true
      },
      address: {
        country: 'UK',
        city: 'Birmingham',
        street: '789 Pine St',
        postcode: 'B1 1AA'
      },
      contactDetails: {
        email: 'user3@example.com',
        phone: '+44111222333'
      },
      contactPreferences: {
        whatsapp: false,
        discord: false,
        post: true,
        phone: false,
        telegram: false,
        email: true
      }
    },
    accumulation: 3
  }
] as const

describe('SelectUserDialog.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockAdminSearchUsersService.mockResolvedValue({
      data: mockUserItems
    } as any)
    mockAdminGetUsersTotalService.mockResolvedValue({
      data: 100
    } as any)

    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Dialog Visibility and Basic Functionality', () => {
    it('displays dialog when visible is true', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      expect(wrapper.find('.el-dialog').exists()).toBe(true)
      expect(wrapper.find('[data-test="user-search"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="sort-tool"]').exists()).toBe(true)
    })

    it('does not display dialog when visible is false', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: false,
          selectAll: false
        }
      })
      await nextTick()

      expect(wrapper.find('.el-dialog').exists()).toBe(false)
    })

    it('fetches and displays user data on mount', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenCalledWith('count', {
        filters: {},
        orderList: 'user_id,asc',
        limit: 10,
        offset: 0
      })
      expect(mockAdminGetUsersTotalService).toHaveBeenCalledWith({
        filters: {}
      })

      // Check if users are displayed in the table
      const tableRows = wrapper.findAll('.el-table__row')
      expect(tableRows.length).toBe(3)
      expect(wrapper.text()).toContain('John Doe')
      expect(wrapper.text()).toContain('Jane Smith')
      expect(wrapper.text()).toContain('Null') // null name should display as 'Null'
    })
  })

  describe('User Selection Functionality', () => {
    it('displays total count in select all checkbox', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      const selectAllCheckbox = wrapper.find('.el-checkbox')
      expect(selectAllCheckbox.text()).toContain('Select All (100)')
    })

    it('prevents admin users from being selected', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Admin users (Jane Smith) should have disabled selection checkbox
      const tableRows = wrapper.findAll('.el-table__row')
      const adminRow = tableRows.find((row) => row.text().includes('admin'))

      if (adminRow) {
        const checkbox = adminRow.find('.el-checkbox input')
        expect(checkbox.attributes('disabled')).toBeDefined()
      }
    })
  })

  describe('Pagination Functionality', () => {
    it('handles page size changes', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Clear previous calls
      mockAdminSearchUsersService.mockClear()
      mockAdminGetUsersTotalService.mockClear()

      // Trigger page size change
      const paginationComponent = wrapper.getComponent({ name: 'ElPagination' })
      await paginationComponent.vm.$emit('size-change', 20)
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenCalledWith('count', {
        filters: {},
        orderList: 'user_id,asc',
        limit: 20,
        offset: 0
      })
    })

    it('handles page changes', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Clear previous calls
      mockAdminSearchUsersService.mockClear()
      mockAdminGetUsersTotalService.mockClear()

      // Trigger page change
      const paginationComponent = wrapper.getComponent({ name: 'ElPagination' })
      await paginationComponent.vm.$emit('current-change', 2)
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenCalledWith('count', {
        filters: {},
        orderList: 'user_id,asc',
        limit: 10,
        offset: 10 // page 2 with page size 10
      })
    })
  })

  describe('Sorting Functionality', () => {
    it('handles table sorting changes', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Clear previous calls
      mockAdminSearchUsersService.mockClear()

      // Trigger sort change
      const tableComponent = wrapper.getComponent({ name: 'ElTable' })
      await tableComponent.vm.$emit('sort-change', {
        prop: 'name',
        order: 'descending'
      })
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenCalledWith('count', {
        filters: {},
        orderList: 'user_name,desc',
        limit: 10,
        offset: 0
      })
    })

    it('handles multiple sort criteria', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Clear previous calls
      mockAdminSearchUsersService.mockClear()

      // Add first sort
      const tableComponent = wrapper.getComponent({ name: 'ElTable' })
      await tableComponent.vm.$emit('sort-change', {
        prop: 'name',
        order: 'ascending'
      })
      await flushPromises()

      // Add second sort
      await tableComponent.vm.$emit('sort-change', {
        prop: 'count',
        order: 'descending'
      })
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenLastCalledWith('count', {
        filters: {},
        orderList: 'user_name,asc,accumulation,desc',
        limit: 10,
        offset: 0
      })
    })

    it('removes sort when order is null', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      const tableComponent = wrapper.getComponent({ name: 'ElTable' })

      // Add sort
      await tableComponent.vm.$emit('sort-change', {
        prop: 'name',
        order: 'ascending'
      })
      await flushPromises()

      // Clear previous calls
      mockAdminSearchUsersService.mockClear()

      // Remove sort
      await tableComponent.vm.$emit('sort-change', {
        prop: 'name',
        order: null
      })
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenCalledWith('count', {
        filters: {},
        orderList: 'user_id,asc', // back to default
        limit: 10,
        offset: 0
      })
    })
  })

  describe('Confirmation Functionality', () => {
    it('emits correct filters when selectAll is true', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: true
        }
      })
      await flushPromises()

      const confirmButton = wrapper.find('[data-test="confirm"]')
      await confirmButton.trigger('click')

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]?.[0]).toEqual({
        filters: {}
      })
    })
  })

  describe('Error Handling', () => {
    it('handles API errors gracefully', async () => {
      mockAdminSearchUsersService.mockRejectedValue(new Error('Network error'))
      mockAdminGetUsersTotalService.mockRejectedValue(
        new Error('Network error')
      )

      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Wait additional time for error handling to complete
      await nextTick()
      await flushPromises()

      // Should show error message
      expect(wrapper.text()).not.toContain('John Doe')
      expect(wrapper.find('.el-table').exists()).toBe(true)

      // Check loading state in component instance instead of DOM
      const componentInstance = wrapper.vm as any
      expect(componentInstance.isLoading).toBe(false)
    })
  })

  describe('Data Transformation', () => {
    it('correctly transforms user data', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      const componentInstance = wrapper.vm as any
      const transformedUsers = componentInstance.users

      expect(transformedUsers).toHaveLength(3)

      // Check first user transformation
      expect(transformedUsers[0]).toEqual({
        uid: 'user1',
        name: 'John Doe',
        assets: 'user1',
        count: 5,
        role: 'user',
        permission: {
          rowId: 'group1',
          name: 'Regular Users',
          description: 'Regular user permissions',
          canCreateAsset: true,
          canSetPolygonOnCreate: false,
          canUpdateAssetFields: true,
          canUpdateAssetPolygon: false,
          canDeleteAsset: false,
          canUpdateProfile: true
        }
      })

      // Check admin user transformation
      expect(transformedUsers[1]).toEqual({
        uid: 'user2',
        name: 'Jane Smith',
        assets: 'user2',
        count: 10,
        role: 'admin',
        permission: {
          rowId: 'group2',
          name: 'Administrators',
          description: 'Admin permissions',
          canCreateAsset: true,
          canSetPolygonOnCreate: true,
          canUpdateAssetFields: true,
          canUpdateAssetPolygon: true,
          canDeleteAsset: true,
          canUpdateProfile: true
        }
      })

      // Check null name transformation
      expect(transformedUsers[2]).toEqual({
        uid: 'user3',
        name: 'Null',
        assets: 'user3',
        count: 3,
        role: 'user',
        permission: {
          rowId: 'group1',
          name: 'Regular Users',
          description: 'Regular user permissions',
          canCreateAsset: true,
          canSetPolygonOnCreate: false,
          canUpdateAssetFields: true,
          canUpdateAssetPolygon: false,
          canDeleteAsset: false,
          canUpdateProfile: true
        }
      })
    })

    it('handles empty user data', async () => {
      mockAdminSearchUsersService.mockResolvedValue({
        code: 200,
        message: 'Success',
        data: []
      })

      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      const componentInstance = wrapper.vm as any
      expect(componentInstance.users).toEqual([])

      // Table should still render but with no rows
      expect(wrapper.find('.el-table').exists()).toBe(true)
      expect(wrapper.findAll('.el-table__row')).toHaveLength(0)
    })
  })

  describe('Component Integration', () => {
    it('exposes users data through defineExpose', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      const componentInstance = wrapper.vm as any
      expect(componentInstance.users).toBeDefined()
      expect(Array.isArray(componentInstance.users)).toBe(true)
    })

    it('properly updates search body and refetches data', async () => {
      const wrapper = mount(SelectUserDialog, {
        props: {
          visible: true,
          selectAll: false
        }
      })
      await flushPromises()

      // Clear initial calls
      mockAdminSearchUsersService.mockClear()
      mockAdminGetUsersTotalService.mockClear()

      // Simulate search component updating the search body
      const componentInstance = wrapper.vm as any
      componentInstance.userSearchBody.filters = {
        user_name: { op: 'like', val: 'John' }
      }

      // Trigger refetch
      await componentInstance.fetchTableData()
      await flushPromises()

      expect(mockAdminSearchUsersService).toHaveBeenCalledWith('count', {
        filters: { user_name: { op: 'like', val: 'John' } },
        orderList: 'user_id,asc',
        limit: 10,
        offset: 0
      })
    })
  })
})
