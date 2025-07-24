/* eslint-disable @typescript-eslint/no-explicit-any */
import { mount } from '@vue/test-utils'
import InboxView from '@/views/inbox/index.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick, ref } from 'vue'
import type { Mail } from '@/types'

// config.global.config.warnHandler = () => {}

// Mock mail services
vi.mock('@/api/mail', () => ({
  readMailService: vi.fn(),
  getMailService: vi.fn()
}))

// Mock composables
vi.mock('@/composables/useResponsiveAction', () => ({
  useResponsiveAction: vi.fn(() => ({
    screenWidth: ref(1200)
  }))
}))

// Import the mocked functions after mocking
import { readMailService } from '@/api/mail'
import { useResponsiveAction } from '@/composables/useResponsiveAction'

const readMailServiceMock = vi.mocked(readMailService)
const mockUseResponsiveAction = vi.mocked(useResponsiveAction)

// Mock mail data
const mockMails: Mail[] = [
  {
    rowId: 1,
    title: 'Test Mail 1',
    message: 'This is test mail 1 content',
    issuedDate: 1640995200000, // 2022-01-01
    hasRead: false,
    validUntil: 1641081600000,
    userId: 'user1'
  },
  {
    rowId: 2,
    title: 'Test Mail 2',
    message: 'This is test mail 2 content',
    issuedDate: 1641081600000, // 2022-01-02
    hasRead: true,
    validUntil: 1641168000000,
    userId: 'user1'
  },
  {
    rowId: 3,
    title: 'Another Subject',
    message: 'Different content here',
    issuedDate: 1641168000000, // 2022-01-03
    hasRead: false,
    validUntil: 1641254400000,
    userId: 'user1'
  }
]

// Mock getUserAssets method
const getMailsMock = vi.fn()
const mailsRef = ref(mockMails)

const mockMailStore = {
  get mails() {
    return mailsRef.value
  },
  set mails(value) {
    mailsRef.value = value
  },
  getMails: getMailsMock
}

const mockUserStore = {
  user: {
    id: 'user1',
    name: 'Test User',
    admin: false
  },
  proxyId: ''
}

vi.mock('@/stores/index.ts', () => ({
  useUserStore: vi.fn(() => mockUserStore),
  useMailStore: vi.fn(() => mockMailStore)
}))

vi.mock('@/stores/modules/mail', () => ({
  useMailStore: vi.fn(() => mockMailStore)
}))

describe('InboxView', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    // Reset mock store data
    mockMailStore.mails = [...mockMails]

    // Reset mock implementations
    getMailsMock.mockResolvedValue(undefined)
    readMailServiceMock.mockResolvedValue({ data: {} } as any)

    // Reset responsive mock to default wide screen
    mockUseResponsiveAction.mockImplementation(
      (callback: (_width: number) => void) => {
        callback(1200) // Wide screen
        return { screenWidth: ref(1200) }
      }
    )
  })

  const createWrapper = (options = {}) => {
    return mount(InboxView, {
      ...options
    })
  }

  describe('Component Initialization', () => {
    it('should render without errors', () => {
      const wrapper = createWrapper()
      expect(wrapper.exists()).toBe(true)
    })

    it('should call getMails on mount', () => {
      createWrapper()
      expect(mockMailStore.getMails).toHaveBeenCalled()
    })

    it('should display inbox header', () => {
      const wrapper = createWrapper()
      expect(wrapper.text()).toContain('Inbox')
    })

    it('should show search input', () => {
      const wrapper = createWrapper()
      const searchInput = wrapper.find('.el-input')
      expect(searchInput.exists()).toBe(true)
    })
  })

  describe('Mail List Display', () => {
    it('should display mail items', async () => {
      const wrapper = createWrapper()
      await nextTick()

      expect(wrapper.text()).toContain('Test Mail 1')
      expect(wrapper.text()).toContain('Test Mail 2')
      expect(wrapper.text()).toContain('Another Subject')
    })

    it('should show mail dates correctly', async () => {
      const wrapper = createWrapper()
      await nextTick()

      // Check that dates are formatted and displayed
      expect(wrapper.html()).toContain('2022') // Should contain year from formatted dates
    })

    it('should handle string dates correctly', async () => {
      const mailWithStringDate = {
        ...mockMails[0],
        issuedDate: '2022-01-01T00:00:00Z'
      }
      mockMailStore.mails = [mailWithStringDate]

      const wrapper = createWrapper()
      await nextTick()

      expect(wrapper.text()).toContain('Test Mail 1')
    })

    it('should apply unread class to unread mails', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const mailItems = wrapper.findAll('.mail-item')
      expect(mailItems.length).toBeGreaterThan(0)

      // Check if unread class is applied to unread mails
      const unreadItems = wrapper.findAll('.mail-item.unread')
      expect(unreadItems.length).toBe(2) // Two unread mails in mock data
    })
  })

  describe('Search Functionality', () => {
    it('should filter mails by title', async () => {
      const wrapper = createWrapper()
      await nextTick()

      // Get the search input and update its value
      const vm = wrapper.vm as any
      vm.searchKeyword = 'Another'
      await nextTick()

      // Should only show the mail with "Another Subject"
      expect(wrapper.text()).toContain('Another Subject')
      expect(wrapper.text()).not.toContain('Test Mail 1')
      expect(wrapper.text()).not.toContain('Test Mail 2')
    })

    it('should filter mails by message content', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.searchKeyword = 'Different'
      await nextTick()

      expect(wrapper.text()).toContain('Another Subject')
      expect(wrapper.text()).not.toContain('Test Mail 1')
    })

    it('should be case insensitive', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.searchKeyword = 'ANOTHER'
      await nextTick()

      expect(wrapper.text()).toContain('Another Subject')
    })
  })

  describe('Filter Functionality', () => {
    it('should show all mails by default', async () => {
      const wrapper = createWrapper()
      await nextTick()

      expect(wrapper.text()).toContain('Test Mail 1')
      expect(wrapper.text()).toContain('Test Mail 2')
      expect(wrapper.text()).toContain('Another Subject')
    })

    it('should filter to show only unread mails', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.filter = 'unread'
      await nextTick()

      // Should show unread mails only
      expect(wrapper.text()).toContain('Test Mail 1')
      expect(wrapper.text()).toContain('Another Subject')
      expect(wrapper.text()).not.toContain('Test Mail 2') // This one is read
    })

    it('should handle filter dropdown click', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const filterItems = wrapper.findAll('.el-dropdown-item')
      const unreadFilterItem = filterItems.find((item: any) =>
        item.text().includes('Unread')
      )

      if (unreadFilterItem) {
        await unreadFilterItem.trigger('click')
        await nextTick()

        const vm = wrapper.vm as any
        expect(vm.filter).toBe('unread')
      }
    })
  })

  describe('Sort Functionality', () => {
    it('should sort by date ascending', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.sort = 'date'
      vm.sortOrder = 'asc'
      await nextTick()

      // Check that sorting affects the order
      expect(vm.currentMails[0].title).toBe('Test Mail 1') // Oldest first
    })

    it('should sort by date descending', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.sort = 'date'
      vm.sortOrder = 'desc'
      await nextTick()

      expect(vm.currentMails[0].title).toBe('Another Subject') // Newest first
    })

    it('should sort by subject alphabetically', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.sort = 'subject'
      vm.sortOrder = 'asc'
      await nextTick()

      expect(vm.currentMails[0].title).toBe('Another Subject') // 'A' comes first
    })
  })

  describe('Mail Selection and Detail View', () => {
    it('should select mail when clicked', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const mailContent = wrapper.find('.mail-content')
      await mailContent.trigger('click')
      await nextTick()

      const vm = wrapper.vm as any
      expect(vm.selectedMail).toBeTruthy()
      expect(vm.mailDetailVisible).toBe(true)
    })

    it('should mark mail as read when clicked', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const mailContent = wrapper.find('.mail-content')
      await mailContent.trigger('click')
      await nextTick()

      expect(readMailServiceMock).toHaveBeenCalledWith('user1', '1')
      expect(mockMailStore.getMails).toHaveBeenCalled()
    })

    it('should show mail detail on wide screen', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.isWideScreen = true
      vm.selectedMail = mockMails[0]
      await nextTick()

      expect(wrapper.find('.mail-detail-panel').exists()).toBe(true)
      expect(wrapper.text()).toContain('Test Mail 1')
    })

    it('should apply selected class to selected mail', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.selectedMail = mockMails[0]
      await nextTick()

      const selectedItem = wrapper.find('.mail-item.selected')
      expect(selectedItem.exists()).toBe(true)
    })
  })

  describe('Pagination', () => {
    it('should show pagination component', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const pagination = wrapper.find('.el-pagination')
      expect(pagination.exists()).toBe(true)
    })

    it('should handle page change', async () => {
      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.handlePageChange(2)

      expect(vm.currentPage).toBe(2)
    })

    it('should calculate current page mails correctly', async () => {
      // Add more mails to test pagination
      const manyMails = Array.from({ length: 25 }, (_, i) => ({
        ...mockMails[0],
        rowId: i + 1,
        title: `Mail ${i + 1}`
      }))
      mockMailStore.mails = manyMails

      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      expect(vm.currentPageMails.length).toBe(10) // pageSize is 10
    })
  })

  describe('Responsive Behavior', () => {
    it('should hide mail detail on small screen initially', async () => {
      mockUseResponsiveAction.mockImplementation(
        (callback: (_width: number) => void) => {
          callback(500) // Small screen
          return { screenWidth: ref(500) }
        }
      )

      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      expect(vm.isWideScreen).toBe(false)

      // Mail detail should not be visible initially on small screen
      expect(wrapper.find('.mail-detail-panel').exists()).toBe(false)
    })

    it('should show back button on small screen mail detail', async () => {
      // Configure small screen mock BEFORE creating wrapper
      mockUseResponsiveAction.mockImplementation(
        (callback: (_width: number) => void) => {
          callback(500) // Small screen
          return { screenWidth: ref(500) }
        }
      )

      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      // Ensure small screen state is set
      vm.isWideScreen = false
      vm.mailDetailVisible = true
      vm.selectedMail = mockMails[0]
      await nextTick()

      expect(wrapper.text()).toContain('Test Mail 1')

      // Check if back button exists
      const backButton = wrapper.find('[data-test="back-button"]')
      console.log(backButton)
      expect(backButton.exists()).toBe(true)
    })
  })

  describe('Delete Functionality', () => {
    it('should handle delete button click', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      const wrapper = createWrapper()
      await nextTick()

      const deleteButton = wrapper.find('.mail-actions .el-button')
      await deleteButton.trigger('click')

      expect(consoleSpy).toHaveBeenCalledWith(111)
      consoleSpy.mockRestore()
    })
  })

  describe('Error Handling', () => {
    it('should handle readMailService error', async () => {
      readMailServiceMock.mockRejectedValueOnce(new Error('API Error'))
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      const wrapper = createWrapper()
      await nextTick()

      const mailContent = wrapper.find('.mail-content')
      await mailContent.trigger('click')
      await nextTick()

      expect(consoleSpy).toHaveBeenCalled()
      consoleSpy.mockRestore()
    })

    it('should handle empty mail store', async () => {
      mockMailStore.mails = []

      const wrapper = createWrapper()
      await nextTick()

      expect(wrapper.exists()).toBe(true)
      expect(wrapper.find('.mail-item').exists()).toBe(false)
    })
  })

  describe('Edge Cases', () => {
    it('should handle mail without title', async () => {
      const mailWithoutTitle = {
        ...mockMails[0],
        title: ''
      }
      mockMailStore.mails = [mailWithoutTitle]

      const wrapper = createWrapper()
      await nextTick()

      expect(wrapper.exists()).toBe(true)
    })

    it('should handle mail without message', async () => {
      const mailWithoutMessage = {
        ...mockMails[0],
        message: ''
      }
      mockMailStore.mails = [mailWithoutMessage]

      const wrapper = createWrapper()
      await nextTick()

      const vm = wrapper.vm as any
      vm.searchKeyword = 'test'
      await nextTick()

      // Should still work with empty message
      expect(wrapper.exists()).toBe(true)
    })
  })
})
