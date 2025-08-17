/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PermissionPage from '@/views/admin/AccessControl.vue'
import * as adminApi from '@/api/admin'
import { nextTick } from 'vue'
import type { ApiResponse, PermissionGroup } from '@/types'

// Mock API functions
vi.mock('@/api/admin', () => ({
  adminGetPermissionGroupsService: vi.fn(),
  adminInsertPermissionGroupService: vi.fn(),
  adminUpdatePermissionGroupService: vi.fn(),
  adminDeletPermissionGroup: vi.fn()
}))

describe('PermissionPage.vue', () => {
  const mockData = [
    {
      rowId: '1',
      name: 'Admin',
      description: 'Administrator group',
      canCreateAsset: true,
      canSetPolygonOnCreate: true,
      canUpdateAssetFields: true,
      canUpdateAssetPolygon: true,
      canDeleteAsset: true,
      canUpdateProfile: true
    }
  ] as PermissionGroup[]

  beforeEach(() => {
    vi.mocked(adminApi.adminGetPermissionGroupsService).mockResolvedValue({
      code: 200,
      data: mockData,
      message: 'Success'
    })
  })

  it('opens create dialog and submits a new permission group', async () => {
    const createSpy = vi
      .mocked(adminApi.adminInsertPermissionGroupService)
      .mockResolvedValue({} as ApiResponse)

    const wrapper = mount(PermissionPage, { attachTo: document.body })

    // Click "Add permission group" button
    const addButton = wrapper.find('[data-test="add-permission-group"]')
    expect(addButton.exists()).toBe(true)
    await addButton.trigger('click')
    await nextTick()

    // Fill in the name
    const nameInput = wrapper.find('[data-test="input-name"]')
    await nameInput.setValue('Test Group')

    // Click Submit
    const submitButton = wrapper.find('[data-test="create"]')
    await submitButton.trigger('click')
    await nextTick()

    expect(createSpy).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'Test Group' })
    )
  })

  it('opens edit dialog, updates a permission group, and closes the dialog', async () => {
    const updateSpy = vi
      .mocked(adminApi.adminUpdatePermissionGroupService)
      .mockResolvedValue(undefined as any)

    const wrapper = mount(PermissionPage, { attachTo: document.body })
    await nextTick()
    await nextTick() // Wait for table to render

    // Click Edit
    const editButton = wrapper.find('[data-test="trigger-edit"]')
    expect(editButton.exists()).toBe(true)
    await editButton.trigger('click')
    await nextTick()

    // Change the name
    const nameInput = wrapper.find('[data-test="input-name"]')
    await nameInput.setValue('Updated Group')

    // Click Submit
    const submitButton = wrapper.find('[data-test="update"]')
    await submitButton.trigger('click')
    await nextTick()

    expect(updateSpy).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'Updated Group' })
    )
  })

  it('deletes a permission group', async () => {
    const deleteSpy = vi
      .mocked(adminApi.adminDeletPermissionGroup)
      .mockResolvedValue({} as any)

    vi.useFakeTimers()

    const wrapper = mount(PermissionPage, { attachTo: document.body })
    await nextTick()
    await nextTick() // Wait for table to render

    // Click Delete
    const deleteButton = wrapper.find('[data-test="delete"]')
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')
    await nextTick()

    // Advance timers to finish countdown
    vi.advanceTimersByTime(6000)
    await nextTick() // Wait for DOM update

    // The ConfirmDialog may be teleported to body
    const confirmButton = document.querySelector(
      '[data-test="confirm"]'
    ) as HTMLButtonElement
    expect(confirmButton).not.toBeNull()

    confirmButton.click()
    await nextTick()

    expect(deleteSpy).toHaveBeenCalledWith(['1'])

    vi.useRealTimers()
  })
})
