import { mount, flushPromises, VueWrapper, DOMWrapper } from '@vue/test-utils'
import ResetPassword from '@/views/user/ResetPassword.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick, type ComponentPublicInstance } from 'vue'

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

// Helper function to find button by text
const findButtonByText = (
  wrapper: VueWrapper<ComponentPublicInstance>,
  text: string
) => {
  const buttons = wrapper.findAll('button')
  return buttons.find((button: DOMWrapper<Element>) => button.text() === text)
}

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
  })

  describe('Initial State', () => {
    it('renders the component correctly', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Check initial message
      expect(wrapper.text()).toContain(
        'For your account safety, we need to verify your email'
      )
      expect(wrapper.text()).toContain('OTP code will send to your email')
      expect(wrapper.text()).toContain('test@example.com')
    })

    it('shows verification form initially', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Should show code input and verification buttons
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      expect(codeInput.exists()).toBe(true)

      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBe(2)
      expect(buttons[0].text()).toBe('Send')
      expect(buttons[1].text()).toBe('Verify')
    })

    it('does not show reset password form initially', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Should not show password inputs initially
      const passwordInputs = wrapper.findAll('input[type="password"]')
      expect(passwordInputs.length).toBe(0)

      expect(wrapper.text()).not.toContain('Reset your password')
    })
  })

  describe('Send OTP Code', () => {
    it('calls userGetEmailService when Send OTP button is clicked', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      const buttons = wrapper.findAll('button')
      const sendButton = buttons[0] // First button is "Send"
      await sendButton.trigger('click')
      await nextTick()

      expect(mockUserGetEmailService).toHaveBeenCalledWith('test@example.com')
    })

    it('handles error when sending OTP code fails', async () => {
      mockUserGetEmailService.mockRejectedValue(new Error('Network error'))

      const wrapper = mount(ResetPassword)
      await nextTick()

      const buttons = wrapper.findAll('button')
      const sendButton = buttons[0] // First button is "Send OTP code"

      await sendButton.trigger('click')

      // Flush all pending promises to handle the rejection
      await flushPromises().catch(() => {
        // Catch the rejection to prevent unhandled promise rejection
      })

      expect(mockUserGetEmailService).toHaveBeenCalledWith('test@example.com')
      // Error should be handled silently as per the component implementation
    })
  })

  describe('Email Verification', () => {
    it('calls userEmailVerificationService when Verify button is clicked', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Set OTP code
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('123456')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton?.trigger('click')
      await nextTick()
      await flushPromises()
      expect(mockUserEmailVerificationService).toHaveBeenCalledWith({
        code: '123456',
        password: '',
        repassword: '',
        email: 'test@example.com'
      })
    })

    it('shows reset password form after successful verification', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Set OTP code and verify
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('123456')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton?.trigger('click')
      await nextTick()
      await flushPromises()
      // Should now show the reset password form
      expect(wrapper.text()).toContain('Reset your password')

      const passwordInputs = wrapper.findAll('input[type="password"]')
      expect(passwordInputs.length).toBe(2)

      const confirmButton = findButtonByText(wrapper, 'Confirm')
      expect(confirmButton?.exists()).toBe(true)
    })

    it('handles verification error gracefully', async () => {
      mockUserEmailVerificationService.mockRejectedValue(
        new Error('Invalid code')
      )

      const wrapper = mount(ResetPassword)
      await nextTick()

      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('invalid')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      expect(verifyButton).toBeDefined()
      await verifyButton!.trigger('click')
      await nextTick()
      await flushPromises()
      expect(mockUserEmailVerificationService).toHaveBeenCalledWith({
        code: 'invalid',
        password: '',
        repassword: '',
        email: 'test@example.com'
      })

      // Should still show verification form
      expect(
        wrapper.find('input[placeholder="Please input OTP code"]').exists()
      ).toBe(true)
    })
  })

  describe('Password Reset', () => {
    it('shows password reset form after successful verification', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // First verify email
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('123456')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton.trigger('click')
      await nextTick()
      await flushPromises()
      // Check password reset form elements
      expect(wrapper.text()).toContain('Reset your password')

      const passwordInputs = wrapper.findAll('input[type="password"]')
      expect(passwordInputs.length).toBe(2)

      expect(passwordInputs[0].attributes('placeholder')).toContain(
        'Please input password'
      )
      expect(passwordInputs[1].attributes('placeholder')).toContain(
        'Please input password again'
      )
    })

    it('calls userResetPasswordService and logs out on successful password reset', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // First verify email
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('123456')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton.trigger('click')
      await nextTick()
      await flushPromises()

      // Set new passwords
      const passwordInputs = wrapper.findAll('input[type="password"]')
      await passwordInputs[0].setValue('newpassword123')
      await passwordInputs[1].setValue('newpassword123')

      // Confirm password reset
      const confirmButton = findButtonByText(wrapper, 'Confirm')
      await confirmButton.trigger('click')
      await nextTick()

      expect(mockUserResetPasswordService).toHaveBeenCalledWith({
        code: '123456',
        password: 'newpassword123',
        repassword: 'newpassword123',
        email: 'test@example.com'
      })

      expect(mockLogout).toHaveBeenCalled()
    })

    it('handles password reset error gracefully', async () => {
      mockUserResetPasswordService.mockRejectedValue(new Error('Reset failed'))

      const wrapper = mount(ResetPassword)
      await nextTick()

      // First verify email
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('123456')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton.trigger('click')
      await nextTick()
      await flushPromises()

      // Set new passwords
      const passwordInputs = wrapper.findAll('input[type="password"]')
      await passwordInputs[0].setValue('newpassword123')
      await passwordInputs[1].setValue('newpassword123')

      // Confirm password reset
      const confirmButton = findButtonByText(wrapper, 'Confirm')
      await confirmButton.trigger('click')
      await nextTick()

      expect(mockUserResetPasswordService).toHaveBeenCalled()
      expect(mockLogout).not.toHaveBeenCalled()
    })
  })

  describe('Form Data Binding', () => {
    it('updates form data when inputs change', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Test code input
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('654321')

      // Verify the form data is updated
      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton.trigger('click')
      await nextTick()
      await flushPromises()

      expect(mockUserEmailVerificationService).toHaveBeenCalledWith({
        code: '654321',
        password: '',
        repassword: '',
        email: 'test@example.com'
      })
    })

    it('prefills email from user store', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Check that email is displayed
      expect(wrapper.text()).toContain('test@example.com')

      // Verify that email is used in API calls
      const sendButton = findButtonByText(wrapper, 'Send')
      await sendButton.trigger('click')
      await nextTick()
      await flushPromises()

      expect(mockUserGetEmailService).toHaveBeenCalledWith('test@example.com')
    })
  })

  describe('Component State Management', () => {
    it('manages resetFormVisible state correctly', async () => {
      const wrapper = mount(ResetPassword)
      await nextTick()

      // Initially should show verification form
      expect(
        wrapper.find('input[placeholder="Please input OTP code"]').exists()
      ).toBe(true)
      expect(wrapper.findAll('input[type="password"]').length).toBe(0)

      // After successful verification, should show reset form
      const codeInput = wrapper.find(
        'input[placeholder="Please input OTP code"]'
      )
      await codeInput.setValue('123456')

      const verifyButton = findButtonByText(wrapper, 'Verify')
      await verifyButton.trigger('click')
      await nextTick()
      await flushPromises()

      expect(
        wrapper.find('input[placeholder="Please input OTP code"]').exists()
      ).toBe(false)
      expect(wrapper.findAll('input[type="password"]').length).toBe(2)
    })
  })
})
