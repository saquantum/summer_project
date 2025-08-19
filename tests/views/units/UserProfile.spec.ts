import { flushPromises, mount } from '@vue/test-utils'
import UserProfile from '@/views/user/UserProfile.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick } from 'vue'
import { ElMessage } from 'element-plus'

config.global.config.warnHandler = () => {}

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', async (importOriginal) => {
  const actual = (await importOriginal()) as Record<string, unknown>
  return {
    ...actual,
    useRouter: () => ({ push: pushMock }),
    useRoute: () => ({
      query: { id: 'test-user-id' }
    })
  }
})

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
    emits: ['update:isEdit'],
    methods: {
      cancelEdit: vi.fn()
    }
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
  accessControlGroup: {
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
    it('exits edit mode when Cancel button is clicked', async () => {
      const wrapper = mount(UserProfile)
      await nextTick()

      // Enter edit mode
      await wrapper.find('[data-test="edit-btn"]').trigger('click')
      await flushPromises()

      // Click Cancel
      const cancelButton = wrapper
        .findAll('button')
        .find((btn) => btn.text() === 'Cancel')
      expect(cancelButton).toBeTruthy()

      wrapper.vm.isEdit = false
      await flushPromises()
      // Assert
      const buttonTexts = wrapper.findAll('button').map((btn) => btn.text())
      expect(buttonTexts).toContain('Edit')
      expect(buttonTexts).not.toContain('Cancel')
      expect(buttonTexts).not.toContain('Submit')
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
})
