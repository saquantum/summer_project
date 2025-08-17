/* eslint-disable @typescript-eslint/no-explicit-any */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AssetTypePage from '@/views/admin/AssetTypes.vue'
import * as adminApi from '@/api/admin'
import { nextTick } from 'vue'

// Mock API functions
vi.mock('@/api/admin', () => ({
  adminInsetAssetTypeService: vi.fn(),
  adminUpdateAssetTypeService: vi.fn(),
  adminDeleteAssetTypeService: vi.fn()
}))

// Mock store
const mockAssetStore = {
  assetTypes: [{ id: '1', name: 'Type A', description: 'Description A' }],
  getAssetTypes: vi.fn()
}

vi.mock('@/stores/index.ts', () => ({
  useAssetStore: () => mockAssetStore
}))

beforeEach(() => {
  mockAssetStore.getAssetTypes.mockClear()
})

describe('AssetTypePage.vue', () => {
  it('adds a new asset type', async () => {
    const insertSpy = vi
      .mocked(adminApi.adminInsetAssetTypeService)
      .mockResolvedValue({} as any)

    const wrapper = mount(AssetTypePage, { attachTo: document.body })

    // Open Add dialog
    const addButton = wrapper.find('[data-test="add-btn"]')
    await addButton.trigger('click')
    await nextTick()

    // Fill form
    await wrapper.find('[data-test="name"]').setValue('New Type')
    await wrapper.find('[data-test="description"]').setValue('New Description')

    // Submit
    await wrapper.find('[data-test="submit"]').trigger('click')
    await nextTick()

    expect(insertSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'New Type',
        description: 'New Description'
      })
    )
    expect(mockAssetStore.getAssetTypes).toHaveBeenCalled()

    const dialog = wrapper.find('[data-test="add-dialog"]')
    expect(dialog.isVisible()).toBe(false)
  })

  it('edits an existing asset type', async () => {
    const updateSpy = vi
      .mocked(adminApi.adminUpdateAssetTypeService)
      .mockResolvedValue({} as any)

    const wrapper = mount(AssetTypePage, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    const editButton = wrapper.find('[data-test="edit"]')
    await editButton.trigger('click')
    await nextTick()

    // Change form values
    await wrapper.find('[data-test="name"]').setValue('Updated Type')
    await wrapper
      .find('[data-test="description"]')
      .setValue('Updated Description')

    // Submit
    await wrapper.find('[data-test="submit"]').trigger('click')
    await nextTick()

    expect(updateSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Updated Type',
        description: 'Updated Description'
      })
    )
    expect(mockAssetStore.getAssetTypes).toHaveBeenCalled()
    const dialog = wrapper.find('[data-test="edit-dialog"]')
    expect(dialog.isVisible()).toBe(false)
  })

  it('deletes an asset type', async () => {
    vi.useFakeTimers()
    const deleteSpy = vi
      .mocked(adminApi.adminDeleteAssetTypeService)
      .mockResolvedValue({} as any)

    const wrapper = mount(AssetTypePage, { attachTo: document.body })
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
    expect(wrapper.vm.deleteDialogVisible).toBe(true)

    // Locate ConfirmDialog component precisely
    const confirmDialog = wrapper.findComponent({ name: 'ConfirmDialog' })
    expect(confirmDialog.exists()).toBe(true)
    expect(confirmDialog.props('modelValue')).toBe(true) // Verify dialog is open

    // Find confirm button within the component
    const confirmButton = confirmDialog.find('[data-test="confirm"]')
    expect(confirmButton.exists()).toBe(true)

    // Click confirm button
    await confirmButton.trigger('click')
    await nextTick()

    // Verify results
    expect(deleteSpy).toHaveBeenCalledWith(['1'])
    expect(mockAssetStore.getAssetTypes).toHaveBeenCalled()
    expect(wrapper.vm.deleteDialogVisible).toBe(false)

    vi.useRealTimers()
  })
})
