/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import AllUsers from '@/views/admin/AllUsers.vue'
import {
  adminAssignUsersToGroup,
  adminDeleteUserService,
  adminGetUsersTotalService
} from '@/api/admin'

import { nextTick } from 'vue'

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', async (importOriginal) => {
  const actual = (await importOriginal()) as Record<string, unknown>
  return {
    ...actual,
    useRouter: () => ({ push: pushMock })
  }
})

// Mock the API services
vi.mock('@/api/admin', () => ({
  adminAssignUsersToGroup: vi.fn(),
  adminDeleteUserService: vi.fn(),
  adminGetUsersTotalService: vi.fn()
}))

// Mock Element Plus
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn()
    }
  }
})

// Mock child components
vi.mock('@/components/search/UserSearch.vue', () => ({
  default: {
    name: 'UserSearch',
    template: '<div data-testid="user-search">UserSearch</div>',
    props: ['fetchTableData', 'userSearchBody'],
    emits: ['update:userSearchBody']
  }
}))

vi.mock('@/components/search/SortTool.vue', () => ({
  default: {
    name: 'SortTool',
    template: '<div data-testid="sort-tool">SortTool</div>',
    props: ['multiSort', 'columns', 'fetchTableData'],
    emits: ['update:multiSort']
  }
}))

vi.mock('@/components/UserCollapse.vue', () => ({
  default: {
    name: 'UserCollapse',
    template: '<div data-testid="user-collapse">UserCollapse</div>',
    props: ['users']
  }
}))

vi.mock('@/components/PermissionIndicator.vue', () => ({
  default: {
    name: 'PermissionIndicator',
    template:
      '<span data-testid="permission-indicator">{{ field }}: {{ status }}</span>',
    props: ['status', 'field']
  }
}))

vi.mock('@/components/dialog/PermissionDialog.vue', () => ({
  default: {
    name: 'PermissionDialog',
    template:
      '<div data-testid="permission-dialog" v-if="permissionDialogVisible">Permission Dialog</div>',
    props: ['permissionDialogVisible', 'selectAll', 'total'],
    emits: [
      'update:permissionDialogVisible',
      'update:selectAll',
      'updatePermissionGroup'
    ]
  }
}))

// Mock user data
const mockUsers = [
  {
    user: {
      id: 'user1',
      name: 'John Doe' as string,
      admin: false,
      accessControlGroup: {
        userId: 'user1',
        canCreateAsset: true,
        canSetPolygonOnCreate: false,
        canUpdateAssetFields: true,
        canUpdateAssetPolygon: false,
        canDeleteAsset: false,
        canUpdateProfile: true
      }
    },
    accumulation: 150
  },
  {
    user: {
      id: 'admin1',
      name: 'Admin User' as string,
      admin: true,
      accessControlGroup: {
        userId: 'admin1',
        canCreateAsset: true,
        canSetPolygonOnCreate: true,
        canUpdateAssetFields: true,
        canUpdateAssetPolygon: true,
        canDeleteAsset: true,
        canUpdateProfile: true
      }
    },
    accumulation: 500
  }
]

// Mock stores
const mockUserStore = {
  users: mockUsers,
  getUsers: vi.fn().mockResolvedValue(undefined),
  userSearchHistory: []
}

// Mock useUserStore
vi.mock('@/stores', () => ({
  useUserStore: vi.fn(() => mockUserStore)
}))

describe('AllUsers', () => {
  beforeEach(() => {
    vi.mocked(adminGetUsersTotalService).mockResolvedValue({
      data: 2
    } as any)

    vi.mocked(adminAssignUsersToGroup).mockResolvedValue({
      data: undefined
    } as any)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly', async () => {
    const wrapper = mount(AllUsers, { attachTo: document.body })

    await nextTick()

    expect(wrapper.find('[data-testid="user-search"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="sort-tool"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="user-collapse"]').exists()).toBe(true)
  })

  it('computes user table correctly', async () => {
    const wrapper = mount(AllUsers, { attachTo: document.body })
    await nextTick()

    const users = wrapper.vm.users
    expect(users).toHaveLength(2)

    expect(users[0]).toEqual({
      uid: 'user1',
      name: 'John Doe',
      assets: 'user1',
      count: 150,
      role: 'user',
      permission: mockUsers[0].user.accessControlGroup
    })

    expect(users[1]).toEqual({
      uid: 'admin1',
      name: 'Admin User',
      assets: 'admin1',
      count: 500,
      role: 'admin',
      permission: mockUsers[1].user.accessControlGroup
    })
  })

  it('handles edit user action', async () => {
    const wrapper = mount(AllUsers, { attachTo: document.body })
    await nextTick()
    await nextTick()

    wrapper.find('[data-test="edit"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith({
      path: '/admin/user/detail',
      query: { id: 'user1' }
    })
  })

  it('opens permission dialog when set permission button is clicked', async () => {
    const wrapper = mount(AllUsers, { attachTo: document.body })
    await nextTick()
    await nextTick()

    const permissionButton = wrapper.find('[data-test="set-permission"]')
    await permissionButton.trigger('click')

    const permissionDialog = wrapper.findComponent({ name: 'PermissionDialog' })
    expect(permissionDialog.isVisible()).toBe(true)
  })

  it('deletes an asset', async () => {
    vi.useFakeTimers()
    const deleteSpy = vi
      .mocked(adminDeleteUserService)
      .mockResolvedValue({} as any)

    const wrapper = mount(AllUsers, { attachTo: document.body })
    await nextTick()
    await nextTick()

    // Click delete button
    const deleteButton = wrapper.findAll('[data-test="delete"]')[0]
    await deleteButton.trigger('click')
    await nextTick()

    // Advance timer to complete countdown
    vi.advanceTimersByTime(5000)
    await nextTick()

    // Verify dialog state
    const confirmDialog = wrapper.findComponent({ name: 'ConfirmDialog' })
    expect(confirmDialog.isVisible()).toBe(true)

    // Find confirm button within the component
    const confirmButton = confirmDialog.find('[data-test="confirm"]')
    expect(confirmButton.exists()).toBe(true)

    // Click confirm button
    await confirmButton.trigger('click')

    // Wait for async operations to complete
    await flushPromises()

    await nextTick()
    await nextTick()
    await nextTick()

    // Verify results
    expect(deleteSpy).toHaveBeenCalledWith(['user1'])
    expect(mockUserStore.getUsers).toHaveBeenCalled()
    // can not test visible, can not figure out why
    expect(confirmDialog.props('modelValue')).toBe(false)

    vi.useRealTimers()
  })

  it('handles API errors gracefully', async () => {
    const consoleErrorSpy = vi
      .spyOn(console, 'error')
      .mockImplementation(() => {})
    vi.mocked(adminGetUsersTotalService).mockRejectedValueOnce(
      new Error('API Error')
    )

    mount(AllUsers, { attachTo: document.body })
    await nextTick()

    // Wait for the async operation to complete
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(ElMessage.error).toHaveBeenCalledWith('Failed to fetch table data')
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Failed to fetch table data:',
      expect.any(Error)
    )

    consoleErrorSpy.mockRestore()
  })

  it('handles null user name correctly', async () => {
    // Test with null name
    const mockUsersWithNull: typeof mockUsers = [
      {
        user: {
          id: 'user2',
          name: null as unknown as string,
          admin: false,
          accessControlGroup: {
            userId: 'user2',
            canCreateAsset: false,
            canSetPolygonOnCreate: false,
            canUpdateAssetFields: false,
            canUpdateAssetPolygon: false,
            canDeleteAsset: false,
            canUpdateProfile: false
          }
        },
        accumulation: 0
      }
    ]

    // Update mock store
    Object.assign(mockUserStore, { users: mockUsersWithNull })

    const wrapper = mount(AllUsers, { attachTo: document.body })
    await nextTick()

    const userTable = wrapper.vm.users
    expect(userTable[0].name).toBe('Null')
  })
})
