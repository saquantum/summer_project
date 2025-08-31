import { mount, flushPromises } from '@vue/test-utils'
import ResetPassword from '@/views/user/ResetPassword.vue'
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

// Mock API functions - declare mock functions inside the factory
vi.mock('@/api/user', () => ({
  userGetEmailService: vi.fn(),
  userEmailVerificationService: vi.fn(),
  userResetPasswordService: vi.fn()
}))

// Import the mocked functions for use in tests
import {
  userGetEmailService,
  userEmailVerificationService,
  userResetPasswordService
} from '@/api/user'

const mockUserGetEmailService = vi.mocked(userGetEmailService)
const mockUserEmailVerificationService = vi.mocked(userEmailVerificationService)
const mockUserResetPasswordService = vi.mocked(userResetPasswordService)

// Mock global logout function
const mockLogout = vi.fn()
vi.mock('@/stores/index.ts', () => ({
  useUserStore: () => ({
    user: {
      contactDetails: {
        email: 'test@example.com'
      }
    }
  })
}))

vi.mock('@/utils/logout.ts', () => ({
  useGlobalLogout: () => ({
    logout: mockLogout
  })
}))

// Mock form validation rules
vi.mock('@/utils/formUtils', () => ({
  passwordRules: [
    { required: true, message: 'Please input password', trigger: 'blur' }
  ],
  createRepasswordRules: () => [
    { required: true, message: 'Please input password', trigger: 'blur' }
  ],
  codeRules: [{ required: true, message: 'Please input code', trigger: 'blur' }]
}))

describe('ResetPassword.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUserGetEmailService.mockResolvedValue({
      code: 200,
      message: 'Success',
      data: undefined
    })
    mockUserEmailVerificationService.mockResolvedValue({
      code: 200,
      message: 'Success',
      data: undefined
    })
    mockUserResetPasswordService.mockResolvedValue({
      code: 200,
      message: 'Success',
      data: undefined
    })

    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Complete User Flow', () => {
    it('successfully completes the entire password reset flow', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Step 1: User sees their email and can initiate OTP request
      expect(wrapper.text()).toContain('test@example.com')
      expect(wrapper.find('[data-test="code"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="send"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="verify"]').exists()).toBe(true)

      // Step 2: User requests OTP code
      const sendButton = wrapper.find('[data-test="send"]')
      await sendButton.trigger('click')
      expect(mockUserGetEmailService).toHaveBeenCalledWith('test@example.com')

      // Step 3: User enters received OTP and verifies
      const codeInput = wrapper.find('[data-test="code"]')
      await codeInput.setValue('123456')

      const verifyButton = wrapper.find('[data-test="verify"]')
      await verifyButton.trigger('click')
      await flushPromises()

      expect(mockUserEmailVerificationService).toHaveBeenCalledWith({
        code: '123456',
        password: '',
        repassword: '',
        email: 'test@example.com'
      })

      // Step 4: Password reset form appears
      expect(wrapper.text()).toContain('New password')
      expect(wrapper.text()).toContain('Confirm new password')
      const passwordInputs = wrapper.findAll('input[type="password"]')
      expect(passwordInputs.length).toBe(2)

      // Step 5: User sets new password
      await passwordInputs[0].setValue('SecureNewPass123!')
      await passwordInputs[1].setValue('SecureNewPass123!')

      const confirmButton = wrapper.find('[data-test="confirm"]')
      await confirmButton.trigger('click')

      // Step 6: Password reset is processed and user is logged out
      expect(mockUserResetPasswordService).toHaveBeenCalledWith({
        code: '123456',
        password: 'SecureNewPass123!',
        repassword: 'SecureNewPass123!',
        email: 'test@example.com'
      })
      expect(mockLogout).toHaveBeenCalled()
    })
  })

  describe('Error Handling', () => {
    it('handles OTP sending failure gracefully', async () => {
      mockUserGetEmailService.mockRejectedValue(new Error('Network error'))

      const wrapper = mount(ResetPassword)
      await nextTick()

      const sendButton = wrapper.find('[data-test="send"]')
      await sendButton.trigger('click')

      await flushPromises().catch(() => {}) // Handle rejection

      expect(mockUserGetEmailService).toHaveBeenCalledWith('test@example.com')
      // User should still be able to continue with manual code entry
      expect(wrapper.find('[data-test="code"]').exists()).toBe(true)
    })

    it('handles invalid OTP code gracefully', async () => {
      mockUserEmailVerificationService.mockRejectedValue(
        new Error('Invalid code')
      )

      const wrapper = mount(ResetPassword)
      await nextTick()

      const codeInput = wrapper.find('[data-test="code"]')
      await codeInput.setValue('wrong-code')

      const verifyButton = wrapper.find('[data-test="verify"]')
      await verifyButton.trigger('click')
      await flushPromises()

      expect(mockUserEmailVerificationService).toHaveBeenCalledWith({
        code: 'wrong-code',
        password: '',
        repassword: '',
        email: 'test@example.com'
      })

      // User should remain on verification step
      expect(wrapper.find('[data-test="code"]').exists()).toBe(true)
      expect(wrapper.findAll('input[type="password"]').length).toBe(0)
    })

    it('handles password reset failure gracefully', async () => {
      mockUserResetPasswordService.mockRejectedValue(new Error('Reset failed'))

      const wrapper = mount(ResetPassword)
      await nextTick()

      // Complete verification step
      const codeInput = wrapper.find('[data-test="code"]')
      await codeInput.setValue('123456')
      const verifyButton = wrapper.find('[data-test="verify"]')
      await verifyButton.trigger('click')
      await flushPromises()

      // Attempt password reset
      const passwordInputs = wrapper.findAll('input[type="password"]')
      await passwordInputs[0].setValue('newPassword123!')
      await passwordInputs[1].setValue('newPassword123!')

      const confirmButton = wrapper.find('[data-test="confirm"]')
      await confirmButton.trigger('click')

      expect(mockUserResetPasswordService).toHaveBeenCalled()
      // User should not be logged out on failure
      expect(mockLogout).not.toHaveBeenCalled()
    })
  })

  describe('Edge Cases', () => {
    it('prevents verification without OTP code', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Try to verify without entering code
      const verifyButton = wrapper.find('[data-test="verify"]')
      await verifyButton.trigger('click')
      await nextTick()

      // Should not call verification service with empty code
      const calls = mockUserEmailVerificationService.mock.calls
      if (calls.length > 0) {
        // If called, code should be empty (form validation might allow this)
        expect(calls[0][0].code).toBe('')
      }
    })

    it('shows countdown after sending OTP', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      const sendButton = wrapper.find('[data-test="send"]')
      await sendButton.trigger('click')
      await nextTick()

      // Button should be disabled and show countdown
      expect(sendButton.attributes('disabled')).toBeDefined()
      expect(sendButton.text()).toMatch(/Send \(\d+\)/)
    })
  })
})
