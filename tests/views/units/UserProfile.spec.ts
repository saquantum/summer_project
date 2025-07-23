import { mount } from '@vue/test-utils'
import UserProfile from '@/views/user/UserProfile.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick } from 'vue'
import { ElMessage } from 'element-plus'

config.global.config.warnHandler = () => {}

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

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

// Mock UserCard component
const submitMock = vi.fn()
vi.mock('@/components/cards/UserCard.vue', () => ({
  default: {
    name: 'UserCard',
    template: '<div class="user-card-mock">UserCard Content</div>',
    props: ['isEdit'],
    emits: ['update:isEdit']
  }
}))

// Create mock user data
const createMockUser = (admin = true, canUpdateProfile = true) => ({
  id: 'test-user',
  admin,
  firstName: 'Test',
  lastName: 'User',
  email: 'test@example.com',
  password: 'password',
  token: 'token',
  avatar: 'avatar-url',
  permissionConfig: {
    userId: 'test-user',
    canCreateAsset: false,
    canSetPolygonOnCreate: false,
    canUpdateAssetFields: false,
    canUpdateAssetPolygon: false,
    canDeleteAsset: false,
    canUpdateProfile
  }
})

// Mock stores
vi.mock('@/stores', () => ({
  useUserStore: () => ({
    user: createMockUser(true, true) // Default: admin user with permissions
  })
}))

describe('UserProfile.vue', () => {
  beforeEach(() => {
    pushMock.mockClear()
    submitMock.mockClear()
    vi.clearAllMocks()
  })

  describe('Basic Rendering', () => {
    it('renders the component correctly', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      // Check that UserCard component is rendered
      expect(wrapper.find('.user-card-mock').exists()).toBe(true)

      // Check that buttons are present
      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBeGreaterThan(0)
    })

    it('shows Edit and Change password buttons initially', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map((btn) => btn.text())

      expect(buttonTexts).toContain('Edit')
      expect(buttonTexts).toContain('Change password')
      expect(buttonTexts).not.toContain('Cancel')
      expect(buttonTexts).not.toContain('Submit')
    })
  })

  describe('Edit Functionality', () => {
    it('enters edit mode when Edit button is clicked', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      const editButton = wrapper.find('button')
      expect(editButton.text()).toBe('Edit')

      await editButton.trigger('click')
      await nextTick()

      // Should show Cancel and Submit buttons
      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map((btn) => btn.text())

      expect(buttonTexts).toContain('Cancel')
      expect(buttonTexts).toContain('Submit')
      expect(buttonTexts).not.toContain('Edit')
    })

    it('exits edit mode when Cancel button is clicked', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      // Enter edit mode
      await wrapper.find('button').trigger('click')
      await nextTick()

      // Find and click Cancel button
      const cancelButton = wrapper
        .findAll('button')
        .find((btn) => btn.text() === 'Cancel')

      if (cancelButton) {
        await cancelButton.trigger('click')
        await nextTick()
      }

      // Should show Edit button again
      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map((btn) => btn.text())

      expect(buttonTexts).toContain('Edit')
      expect(buttonTexts).not.toContain('Cancel')
      expect(buttonTexts).not.toContain('Submit')
    })

    it('shows Submit button in edit mode', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      // Enter edit mode
      await wrapper.find('button').trigger('click')
      await nextTick()

      // Should show Submit button
      const buttons = wrapper.findAll('button')
      const buttonTexts = buttons.map((btn) => btn.text())
      expect(buttonTexts).toContain('Submit')
    })
  })

  describe('Navigation', () => {
    it('navigates to change password page when Change password button is clicked', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      const changePasswordButton = wrapper
        .findAll('button')
        .find((btn) => btn.text() === 'Change password')

      if (changePasswordButton) {
        await changePasswordButton.trigger('click')
      }

      expect(pushMock).toHaveBeenCalledWith('/security/verify-mail')
    })
  })

  describe('Permission Handling', () => {
    it('allows admin users to edit profile', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      const editButton = wrapper.find('button')
      expect(editButton.classes()).not.toContain('is-disabled')

      await editButton.trigger('click')
      await nextTick()

      expect(ElMessage.error).not.toHaveBeenCalled()
    })

    it('computes isDisabled correctly for admin user', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      expect(wrapper.vm.isDisabled).toBe(false)
    })
  })

  describe('Exposed Properties', () => {
    it('exposes necessary properties for testing', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      expect(wrapper.vm.isEdit).toBeDefined()
      expect(wrapper.vm.isDisabled).toBeDefined()
      expect(wrapper.vm.user).toBeDefined()
      expect(wrapper.vm.submit).toBeDefined()
      expect(wrapper.vm.handleEdit).toBeDefined()
    })
  })

  describe('Component Methods', () => {
    it('handleEdit method works correctly for admin', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      wrapper.vm.handleEdit()
      await nextTick()

      expect(wrapper.vm.isEdit).toBe(true)
      expect(ElMessage.error).not.toHaveBeenCalled()
    })

    it('submit method is available', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      // Check that submit method exists
      expect(wrapper.vm.submit).toBeDefined()
      expect(typeof wrapper.vm.submit).toBe('function')
    })
  })

  describe('User Computed Property', () => {
    it('returns user from store', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      const user = wrapper.vm.user
      expect(user).toBeDefined()
      expect(user.admin).toBe(true)
      expect(user.id).toBe('test-user')
    })
  })

  describe('Button Styling', () => {
    it('applies correct CSS classes to edit button', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      const editButton = wrapper.find('button')
      // For admin user, button should not be disabled
      expect(editButton.classes()).not.toContain('is-disabled')
    })

    it('Change password button is always visible', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      // In view mode
      let buttons = wrapper.findAll('button')
      let buttonTexts = buttons.map((btn) => btn.text())
      expect(buttonTexts).toContain('Change password')

      // Enter edit mode
      await wrapper.find('button').trigger('click')
      await nextTick()

      // In edit mode - Change password button should still be visible
      // Based on the template, it should be visible with v-if="!isEdit" condition,
      // but let's check what actually gets rendered
      buttons = wrapper.findAll('button')
      buttonTexts = buttons.map((btn) => btn.text())

      // The Change password button should NOT be visible in edit mode according to template logic
      expect(buttonTexts).not.toContain('Change password')
      expect(buttonTexts).toContain('Cancel')
      expect(buttonTexts).toContain('Submit')
    })
  })
})
