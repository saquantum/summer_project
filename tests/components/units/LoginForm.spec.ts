import { mount } from '@vue/test-utils'
import LoginForm from '@/components/LoginForm.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

// Mock stores and API calls
vi.mock('@/stores/index.ts', () => ({
  useUserStore: () => ({
    getUser: vi.fn(),
    getUserInfo: vi.fn()
  }),
  useAssetStore: () => ({ getAssetTypes: vi.fn() })
}))
vi.mock('@/api/user', () => ({
  userCheckEmailService: vi.fn(),
  userCheckUIDService: vi.fn(),
  userRegisterService: vi.fn()
}))

// Basic tests
describe('LoginForm.vue', () => {
  beforeEach(() => {
    pushMock.mockClear()
  })

  it('renders sign in form by default', () => {
    const wrapper = mount(LoginForm)
    expect(wrapper.find('.form-title').text()).toBe('SIGN IN')
  })

  it('shows register form when clicking Register', async () => {
    const wrapper = mount(LoginForm)
    await wrapper.find('.fixed-bottom-tip .el-link').trigger('click')
    expect(wrapper.find('.form-title').text()).toBe('SIGN UP')
  })

  it('calls login when sign in button is clicked', async () => {
    const wrapper = mount(LoginForm)
    await wrapper.find('input[placeholder*="username" i]').setValue('testuser')
    await wrapper.find('input[placeholder*="password" i]').setValue('testpass')
    await wrapper.find('button.button').trigger('click')
    await flushPromises()
    expect(pushMock).toHaveBeenCalled() // router.push should be called
  })

  it('shows error if username is empty', async () => {
    const wrapper = mount(LoginForm)
    await wrapper.find('input[placeholder*="password" i]').setValue('testpass')
    await wrapper.find('button.button').trigger('click')
    // Should not call router.push
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('shows error if password is empty', async () => {
    const wrapper = mount(LoginForm)
    await wrapper.find('input[placeholder*="username" i]').setValue('testuser')
    await wrapper.find('button.button').trigger('click')
    // Should not call router.push
    expect(pushMock).not.toHaveBeenCalled()
  })
})

// Basic tests
describe('Register', () => {
  beforeEach(() => {
    pushMock.mockClear()
  })

  it('shows register form when clicking Register', async () => {
    const wrapper = mount(LoginForm)
    await wrapper.find('.fixed-bottom-tip .el-link').trigger('click')
    expect(wrapper.find('.form-title').text()).toBe('SIGN UP')
  })

  it('cannot goto step 2 if email is empty', async () => {
    const wrapper = mount(LoginForm)
    await wrapper.find('.fixed-bottom-tip .el-link').trigger('click')
    await wrapper.find('[data-test="register-button1"]').trigger('click')
    await flushPromises()
    expect(wrapper.vm.currentStep).toBe(1)
  })
})
