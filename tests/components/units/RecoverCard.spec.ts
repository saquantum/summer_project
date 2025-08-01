import { mount, flushPromises } from '@vue/test-utils'
import RecoverCard from '@/components/cards/RecoverCard.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as userApi from '@/api/user'

// Mock router
const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

vi.mock('@/api/user', () => ({
  userGetEmailService: vi.fn(),
  userEmailVerificationService: vi.fn(),
  userResetPasswordService: vi.fn()
}))

describe('RecoverCard.vue', () => {
  beforeEach(() => {
    vi.mocked(userApi.userGetEmailService).mockClear()
    vi.mocked(userApi.userEmailVerificationService).mockClear()
    vi.mocked(userApi.userResetPasswordService).mockClear()
    pushMock.mockClear()
  })

  it('renders email input and send button', () => {
    const wrapper = mount(RecoverCard)
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('button').text()).toContain('Send')
  })

  it('shows code input after sending email', async () => {
    const wrapper = mount(RecoverCard)
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('button').trigger('click')
    await flushPromises()
    expect(userApi.userGetEmailService).toHaveBeenCalledWith('test@example.com')
    expect(wrapper.find('input[maxlength="6"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Verify')
  })

  it('shows password reset form after verifying code', async () => {
    const wrapper = mount(RecoverCard)
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('button').trigger('click')
    await flushPromises()
    await wrapper.find('input[maxlength="6"]').setValue('123456')
    await wrapper.findAll('button')[1].trigger('click') // Verify button
    await flushPromises()
    expect(userApi.userEmailVerificationService).toHaveBeenCalled()
    expect(wrapper.text()).toContain('Reset your password')
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('calls reset password and navigates to login', async () => {
    const wrapper = mount(RecoverCard)
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('button').trigger('click')
    await flushPromises()
    await wrapper.find('input[maxlength="6"]').setValue('123456')
    await wrapper.findAll('button')[1].trigger('click')
    await flushPromises()
    await wrapper
      .find('input[placeholder="Please input password"]')
      .setValue('newpass')
    await wrapper
      .find('input[placeholder="Please input password again"]')
      .setValue('newpass')
    await wrapper.find('button').trigger('click')
    await flushPromises()
    expect(userApi.userResetPasswordService).toHaveBeenCalled()
    expect(pushMock).toHaveBeenCalledWith('/login')
  })

  // error handling
  it('handles email verification failure', async () => {
    vi.mocked(userApi.userEmailVerificationService).mockRejectedValue(
      new Error('Verification failed')
    )
    const wrapper = mount(RecoverCard)
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('button').trigger('click')
    await flushPromises()
    await wrapper.find('input[maxlength="6"]').setValue('123456')
    await wrapper.findAll('button')[1].trigger('click')
    await flushPromises()
    expect(wrapper.text()).not.toContain('Reset your password')
  })
})
