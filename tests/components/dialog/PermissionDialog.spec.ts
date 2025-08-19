import { mount, flushPromises } from '@vue/test-utils'
import PermissionDialog from '@/components/dialog/PermissionDialog.vue'
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

// Mock admin API
vi.mock('@/api/admin', () => ({
  adminGetPermissionGroupsService: vi.fn()
}))

import { adminGetPermissionGroupsService } from '@/api/admin'
import type { PermissionGroup } from '@/types'

const mockAdminGetPermissionGroupsService = vi.mocked(
  adminGetPermissionGroupsService
)

// Mock permission groups data
const mockPermissionGroups: PermissionGroup[] = [
  {
    rowId: '1',
    name: 'Admin',
    description: 'Full administrative access',
    canCreateAsset: true,
    canSetPolygonOnCreate: true,
    canUpdateAssetFields: true,
    canUpdateAssetPolygon: true,
    canDeleteAsset: true,
    canUpdateProfile: true
  },
  {
    rowId: '2',
    name: 'Editor',
    description: 'Can edit assets',
    canCreateAsset: true,
    canSetPolygonOnCreate: false,
    canUpdateAssetFields: true,
    canUpdateAssetPolygon: false,
    canDeleteAsset: false,
    canUpdateProfile: true
  },
  {
    rowId: '3',
    name: 'Viewer',
    description: 'Read-only access',
    canCreateAsset: false,
    canSetPolygonOnCreate: false,
    canUpdateAssetFields: false,
    canUpdateAssetPolygon: false,
    canDeleteAsset: false,
    canUpdateProfile: false
  }
]

describe('PermissionDialog.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockAdminGetPermissionGroupsService.mockResolvedValue({
      code: 200,
      message: 'Success',
      data: mockPermissionGroups
    })

    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Complete User Flow', () => {
    it('successfully completes permission group selection flow', async () => {
      const wrapper = mount(PermissionDialog, {
        props: {
          total: 10,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()
      await flushPromises()

      // Step 1: Dialog loads with permission groups
      expect(mockAdminGetPermissionGroupsService).toHaveBeenCalled()
      expect(wrapper.text()).toContain('Select All (10)')

      // Step 2: User sees all permission groups in table
      expect(wrapper.text()).toContain('Admin')
      expect(wrapper.text()).toContain('Editor')
      expect(wrapper.text()).toContain('Viewer')
      expect(wrapper.text()).toContain('Full administrative access')

      // Step 3: User can toggle select all checkbox
      const selectAllCheckbox = wrapper.find('[data-test="select-all"]')
      expect(selectAllCheckbox.exists()).toBe(true)
      await selectAllCheckbox.trigger('click')

      // Step 4: User selects a specific permission group (simulate row click)
      const tableRows = wrapper.findAll('tr')
      // Find the Admin row and click it
      const adminRow = tableRows.find((row) => row.text().includes('Admin'))
      expect(adminRow).toBeTruthy()
      if (adminRow) {
        await adminRow.trigger('click')
      }

      // Step 5: User confirms selection
      const confirmButton = wrapper.find('[data-test="confirm"]')
      expect(confirmButton.exists()).toBe(true)
      await confirmButton.trigger('click')

      // Step 6: Dialog emits the selected permission group
      const emittedEvents = wrapper.emitted('update-permission-group')
      expect(emittedEvents).toBeTruthy()
      expect(emittedEvents![0]).toContain('Admin')
    })

    it('allows user to cancel without selecting', async () => {
      const wrapper = mount(PermissionDialog, {
        props: {
          total: 5,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()
      await flushPromises()

      // User sees dialog
      expect(wrapper.text()).toContain('Select All (5)')

      // User clicks cancel
      const cancelButton = wrapper.find('[data-test="cancel"]')
      expect(cancelButton.exists()).toBe(true)
      await cancelButton.trigger('click')

      // Dialog should close (model value updated)
      expect(wrapper.emitted('update:permissionDialogVisible')).toBeTruthy()
      const visibilityEvents = wrapper.emitted('update:permissionDialogVisible')
      expect(visibilityEvents![0]).toEqual([false])
    })
  })

  describe('Error Handling', () => {
    it('handles API failure gracefully', async () => {
      mockAdminGetPermissionGroupsService.mockRejectedValue(
        new Error('Network error')
      )

      const wrapper = mount(PermissionDialog, {
        props: {
          total: 10,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()

      // Catch the promise rejection to prevent unhandled error
      await flushPromises().catch(() => {
        // Error is expected and handled
      })

      expect(mockAdminGetPermissionGroupsService).toHaveBeenCalled()

      // Dialog should still be functional even without data
      expect(wrapper.find('[data-test="cancel"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="confirm"]').exists()).toBe(true)
    })

    it('handles empty permission groups response', async () => {
      mockAdminGetPermissionGroupsService.mockResolvedValue({
        code: 200,
        message: 'Success',
        data: []
      })

      const wrapper = mount(PermissionDialog, {
        props: {
          total: 0,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()
      await flushPromises()

      expect(wrapper.text()).toContain('Select All (0)')

      // Confirm button should still work but emit empty string
      const confirmButton = wrapper.find('[data-test="confirm"]')
      await confirmButton.trigger('click')

      const emittedEvents = wrapper.emitted('update-permission-group')
      expect(emittedEvents).toBeTruthy()
      expect(emittedEvents![0]).toEqual([''])
    })
  })

  describe('Edge Cases', () => {
    it('handles confirm without selecting any row', async () => {
      const wrapper = mount(PermissionDialog, {
        props: {
          total: 3,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()
      await flushPromises()

      // User clicks confirm without selecting any row
      const confirmButton = wrapper.find('[data-test="confirm"]')
      await confirmButton.trigger('click')

      // Should emit empty string when no row is selected
      const emittedEvents = wrapper.emitted('update-permission-group')
      expect(emittedEvents).toBeTruthy()
      expect(emittedEvents![0]).toEqual([''])
    })

    it('displays all permission details correctly', async () => {
      const wrapper = mount(PermissionDialog, {
        props: {
          total: 3,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()
      await flushPromises()

      // Check that all table columns are displayed
      expect(wrapper.text()).toContain('Row id')
      expect(wrapper.text()).toContain('Name')
      expect(wrapper.text()).toContain('Description')
      expect(wrapper.text()).toContain('Create asset')
      expect(wrapper.text()).toContain('set polygon on create')
      expect(wrapper.text()).toContain('Update asset fields')
      expect(wrapper.text()).toContain('Update asset polygon')
      expect(wrapper.text()).toContain('Delete asset')
      expect(wrapper.text()).toContain('Update profile')

      // Check that permission data is displayed
      expect(wrapper.text()).toContain('Full administrative access')
      expect(wrapper.text()).toContain('Can edit assets')
      expect(wrapper.text()).toContain('Read-only access')
    })

    it('updates select all model correctly', async () => {
      const wrapper = mount(PermissionDialog, {
        props: {
          total: 3,
          permissionDialogVisible: true,
          selectAll: false
        }
      })
      await nextTick()

      const selectAllCheckbox = wrapper.find('[data-test="select-all"]')

      // Trigger the input event to simulate user interaction
      await selectAllCheckbox.find('input').setValue(true)
      await nextTick()

      // Should emit update for selectAll model
      expect(wrapper.emitted('update:selectAll')).toBeTruthy()
      const selectAllEvents = wrapper.emitted('update:selectAll')
      expect(selectAllEvents![0]).toEqual([true])
    })
  })
})
