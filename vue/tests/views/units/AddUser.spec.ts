import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AddUser from '@/views/admin/AddUser.vue' // adjust path
import { nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { adminInsertUserService } from '@/api/admin'
import type { ApiResponse } from '@/types'

// Mock api
vi.mock('@/api/admin', () => ({
  adminInsertUserService: vi.fn()
}))

// Mock Element Plus - partial mock approach
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn()
    }
  }
})

// mock form utils
vi.mock('@/utils/formUtils', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/utils/formUtils')>()
  return {
    ...actual,
    usernameRules: vi.fn(),
    uniqueEmailRules: vi.fn()
  }
})

describe('AddUser.vue', () => {
  const mockUser = {
    id: 'testuser',
    password: 'Password123!',
    repassword: 'Password123!',
    firstName: 'John',
    lastName: 'Doe',
    contactDetails: {
      email: 'john@example.com',
      phone: '1234567890'
    },
    address: {
      street: '123 Main St',
      postcode: '12345',
      city: 'Cityville',
      country: 'Countryland'
    },
    contactPreferences: {
      email: true,
      phone: true,
      whatsapp: false,
      discord: false,
      post: false,
      telegram: false
    },
    permissionConfig: {
      userId: '',
      canCreateAsset: false,
      canSetPolygonOnCreate: false,
      canUpdateAssetFields: false,
      canUpdateAssetPolygon: false,
      canDeleteAsset: false,
      canUpdateProfile: false
    }
  }

  beforeEach(() => {
    vi.mocked(adminInsertUserService).mockReset()
    vi.mocked(ElMessage.success).mockReset()
    vi.mocked(ElMessage.error).mockReset()
  })

  it('submits the form successfully', async () => {
    vi.mocked(adminInsertUserService).mockResolvedValue({} as ApiResponse)

    const wrapper = mount(AddUser)

    // Fill required fields correctly
    await wrapper.find('[data-test="id"]').setValue('user1')
    await wrapper.find('[data-test="password"]').setValue('Password123!')
    await wrapper.find('[data-test="repassword"]').setValue('Password123!')
    await wrapper.find('[data-test="firstName"]').setValue('John')
    await wrapper.find('[data-test="lastName"]').setValue('Doe')
    await wrapper.find('[data-test="email"]').setValue('john@example.com')
    await wrapper.find('[data-test="phone"]').setValue('1234567890')

    await wrapper.find('[data-test="submit"]').trigger('click')

    await flushPromises()

    expect(vi.mocked(adminInsertUserService)).toHaveBeenCalled()

    expect(ElMessage.success).toHaveBeenCalledWith('Successfully add an user')
  })

  it('shows error when API fails', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {})
    vi.mocked(adminInsertUserService).mockRejectedValue({} as ApiResponse)

    const wrapper = mount(AddUser)

    // Fill all required fields
    await wrapper.find('[data-test="id"]').setValue(mockUser.id)
    await wrapper.find('[data-test="password"]').setValue(mockUser.password)
    await wrapper.find('[data-test="repassword"]').setValue(mockUser.repassword)
    await wrapper.find('[data-test="firstName"]').setValue(mockUser.firstName)
    await wrapper.find('[data-test="lastName"]').setValue(mockUser.lastName)
    await wrapper
      .find('[data-test="email"]')
      .setValue(mockUser.contactDetails.email)
    await wrapper
      .find('[data-test="phone"]')
      .setValue(mockUser.contactDetails.phone)

    await wrapper.find('[data-test="submit"]').trigger('click')

    await flushPromises()

    expect(vi.mocked(adminInsertUserService)).toHaveBeenCalled()

    expect(ElMessage.error).toHaveBeenCalledWith('Server Error')

    vi.restoreAllMocks()
  })

  it('resets the form', async () => {
    const wrapper = mount(AddUser)

    await wrapper.find('[data-test="id"]').setValue('somevalue')
    await wrapper.find('[data-test="reset"]').trigger('click')
    await nextTick()

    expect(wrapper.find('[data-test="id"]').text()).toBe('')
  })
})
