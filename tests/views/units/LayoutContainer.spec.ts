import { mount } from '@vue/test-utils'
import LayoutContainer from '@/views/layout/LayoutContainer.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { ElMessage } from 'element-plus'

// Mock router
const mockRouter = {
  push: vi.fn(),
  currentRoute: { value: { path: '/dashboard' } }
}

vi.mock('vue-router', () => ({
  useRouter: () => mockRouter,
  useRoute: () => ({ path: '/dashboard' })
}))

// Mock stores
const mockUserStore = {
  user: {
    admin: false,
    userId: 'user123',
    firstName: 'John',
    lastName: 'Doe',
    avatar: 'https://example.com/avatar.jpg'
  },
  proxyId: null,
  reset: vi.fn()
}

const mockAssetStore = {
  reset: vi.fn()
}

vi.mock('@/stores/index.ts', () => ({
  useUserStore: () => mockUserStore,
  useAssetStore: () => mockAssetStore
}))

// Mock Element Plus - partial mock approach
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessage: {
      error: vi.fn()
    }
  }
})

// Mock child components
vi.mock('@/components/SideMenu.vue', () => ({
  default: {
    name: 'SideMenu',
    template: '<div data-test="side-menu">SideMenu</div>',
    props: ['showUserSideBar', 'activeIndex', 'visible']
  }
}))

vi.mock('@/components/CustomerService.vue', () => ({
  default: {
    name: 'CustomerService',
    template: '<div data-test="customer-service">CustomerService</div>'
  }
}))

vi.mock('@/components/SearchDialog.vue', () => ({
  default: {
    name: 'SearchDialog',
    template: '<div data-test="search-dialog">SearchDialog</div>',
    props: ['visible'],
    emits: ['update:visible']
  }
}))

describe('LayoutContainer', () => {
  const createWrapper = (userOverrides = {}) => {
    // Update mock user store with overrides
    Object.assign(mockUserStore, userOverrides)

    return mount(LayoutContainer, {
      global: {
        stubs: {
          'router-link': {
            template: '<a><slot></slot></a>',
            props: ['to']
          },
          'router-view': {
            template: '<div>Router View Content</div>'
          }
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    // Reset user store to default state
    mockUserStore.user = {
      admin: false,
      userId: 'user123',
      firstName: 'John',
      lastName: 'Doe',
      avatar: 'https://example.com/avatar.jpg'
    }
    mockUserStore.proxyId = null
  })

  it('renders main layout structure correctly', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check main layout elements
    expect(wrapper.find('.layout-container').exists()).toBe(true)
    expect(wrapper.find('.el-aside').exists()).toBe(true)
    expect(wrapper.find('.el-header').exists()).toBe(true)
    expect(wrapper.find('.el-main').exists()).toBe(true)
  })

  it('renders logo and side menu in aside', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check for logo
    const logo = wrapper.find('.el-aside__logo')
    expect(logo.exists()).toBe(true)
    expect(logo.attributes('alt')).toBe('logo')

    // Check for side menu
    expect(wrapper.findComponent({ name: 'SideMenu' }).exists()).toBe(true)

    // Check for sign out button
    const signOutButton = wrapper.find('.signout-button')
    expect(signOutButton.exists()).toBe(true)
    expect(signOutButton.text()).toBe('Sign out')
  })

  it('renders header with search and user dropdown', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check header structure
    expect(wrapper.find('.header-left').exists()).toBe(true)
    expect(wrapper.find('.header-right').exists()).toBe(true)

    // Check search button exists
    const searchButtons = wrapper.findAll('button')
    expect(searchButtons.length).toBeGreaterThan(0)

    // Check notification bell
    const bellIcon = wrapper.find('.bell')
    expect(bellIcon.exists()).toBe(true)

    // Check user dropdown
    const dropdown = wrapper.find('.el-dropdown__box')
    expect(dropdown.exists()).toBe(true)
  })

  it('shows user sidebar for regular users', async () => {
    const wrapper = createWrapper({
      user: { admin: false, userId: 'user123' }
    })
    await flushPromises()

    const sideMenu = wrapper.findComponent({ name: 'SideMenu' })
    expect(sideMenu.props('showUserSideBar')).toBe(true)
  })

  it('hides user sidebar for admin users without proxy', async () => {
    const wrapper = createWrapper({
      user: { admin: true, userId: 'admin123' },
      proxyId: null
    })
    await flushPromises()

    const sideMenu = wrapper.findComponent({ name: 'SideMenu' })
    expect(sideMenu.props('showUserSideBar')).toBe(false)
  })

  it('shows user sidebar for admin users with proxy', async () => {
    const wrapper = createWrapper({
      user: { admin: true, userId: 'admin123' },
      proxyId: 'proxy123'
    })
    await flushPromises()

    const sideMenu = wrapper.findComponent({ name: 'SideMenu' })
    expect(sideMenu.props('showUserSideBar')).toBe(true)
  })

  it('shows customer service for regular users only', async () => {
    const wrapper = createWrapper({
      user: { admin: false }
    })
    await flushPromises()

    expect(wrapper.findComponent({ name: 'CustomerService' }).exists()).toBe(
      true
    )
  })

  it('hides customer service for admin users', async () => {
    const wrapper = createWrapper({
      user: { admin: true }
    })
    await flushPromises()

    expect(wrapper.findComponent({ name: 'CustomerService' }).exists()).toBe(
      false
    )
  })

  it('handles logout functionality', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const signOutButton = wrapper.find('.signout-button')
    await signOutButton.trigger('click')

    // Verify stores are reset
    expect(mockUserStore.reset).toHaveBeenCalled()
    expect(mockAssetStore.reset).toHaveBeenCalled()

    // Verify navigation to login
    expect(mockRouter.push).toHaveBeenCalledWith('/login')
  })

  it('handles dropdown commands - logout', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Simulate dropdown command
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const layoutInstance = wrapper.vm as any
    await layoutInstance.handleCommand('logout')

    expect(mockUserStore.reset).toHaveBeenCalled()
    expect(mockAssetStore.reset).toHaveBeenCalled()
    expect(mockRouter.push).toHaveBeenCalledWith('/login')
  })

  it('handles dropdown commands - profile for regular user', async () => {
    const wrapper = createWrapper({
      user: { admin: false, userId: 'user123' }
    })
    await flushPromises()

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const layoutInstance = wrapper.vm as any
    await layoutInstance.handleCommand('profile')

    expect(mockRouter.push).toHaveBeenCalledWith('/user/profile')
  })

  it('handles dropdown commands - profile for admin user shows error', async () => {
    const wrapper = createWrapper({
      user: { admin: true, userId: 'admin123' }
    })
    await flushPromises()

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const layoutInstance = wrapper.vm as any
    await layoutInstance.handleCommand('profile')

    expect(vi.mocked(ElMessage.error)).toHaveBeenCalledWith(
      'Admin currently does not have profile'
    )
    expect(mockRouter.push).not.toHaveBeenCalled()
  })

  it('handles dropdown commands - password', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const layoutInstance = wrapper.vm as any
    await layoutInstance.handleCommand('password')

    expect(mockRouter.push).toHaveBeenCalledWith('/security/verify-mail')
  })

  it('handles mail click for regular users', async () => {
    const wrapper = createWrapper({
      user: { admin: false }
    })
    await flushPromises()

    const bellIcon = wrapper.find('.bell')
    await bellIcon.trigger('click')

    expect(mockRouter.push).toHaveBeenCalledWith('/message')
  })

  it('handles mail click for admin users', async () => {
    const wrapper = createWrapper({
      user: { admin: true }
    })
    await flushPromises()

    const bellIcon = wrapper.find('.bell')
    await bellIcon.trigger('click')

    expect(mockRouter.push).toHaveBeenCalledWith('/admin/message')
  })

  it('opens search dialog when search button is clicked', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const searchButton = wrapper.find('.header-left button')
    await searchButton.trigger('click')
    await flushPromises()

    const searchDialog = wrapper.findComponent({ name: 'SearchDialog' })
    expect(searchDialog.exists()).toBe(true)
  })

  it('toggles mobile menu button functionality', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const mobileMenuButton = wrapper.find('.mobile-menu')
    expect(mobileMenuButton.exists()).toBe(true)

    // Test that button can be clicked without errors
    await mobileMenuButton.trigger('click')
    // Component state changes are internal, just verify no errors
  })

  it('renders mobile drawer with side menu', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check that drawer element exists (even if not visible)
    const drawer = wrapper.findComponent({ name: 'el-drawer' })
    expect(drawer.exists()).toBe(true)

    // Check that mobile drawer contains side menu
    const mobileSideMenu = wrapper.findComponent({ name: 'SideMenu' })
    expect(mobileSideMenu.exists()).toBe(true)
  })

  it('displays user avatar in dropdown', async () => {
    const wrapper = createWrapper({
      user: {
        avatar: 'https://example.com/custom-avatar.jpg',
        admin: false
      }
    })
    await flushPromises()

    // Check that dropdown box exists and has avatar content
    const dropdownBox = wrapper.find('.el-dropdown__box')
    expect(dropdownBox.exists()).toBe(true)
  })

  it('passes correct props to SideMenu', async () => {
    const wrapper = createWrapper({
      user: { admin: false }
    })
    await flushPromises()

    const sideMenu = wrapper.findComponent({ name: 'SideMenu' })
    expect(sideMenu.props('showUserSideBar')).toBe(true)
    expect(sideMenu.props('activeIndex')).toBe('/dashboard')
  })

  it('verifies route-based activeIndex prop', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Test that SideMenu receives the correct activeIndex from route
    const sideMenu = wrapper.findComponent({ name: 'SideMenu' })
    expect(sideMenu.props('activeIndex')).toBe('/dashboard')
  })

  it('renders main content area correctly', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check main content area exists
    const mainArea = wrapper.find('.el-main')
    expect(mainArea.exists()).toBe(true)

    // Check router content is rendered (from our stub)
    expect(mainArea.text()).toContain('Router View Content')
  })

  it('applies correct CSS classes and styling', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check main container class
    expect(wrapper.find('.layout-container').classes()).toContain(
      'layout-container'
    )

    // Check aside styling classes
    expect(wrapper.find('.el-aside').exists()).toBe(true)
    expect(wrapper.find('.signout-container').exists()).toBe(true)

    // Check header styling classes
    expect(wrapper.find('.header-left').exists()).toBe(true)
    expect(wrapper.find('.header-right').exists()).toBe(true)
  })

  it('handles bell icon hover effects', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const bellIcon = wrapper.find('.bell')
    expect(bellIcon.exists()).toBe(true)
    expect(bellIcon.classes()).toContain('bell')
  })
})
