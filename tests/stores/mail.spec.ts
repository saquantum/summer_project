/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMailStore } from '@/stores/modules/mail'
import type { Mail } from '@/types'

// Mock the API services
vi.mock('@/api/mail', () => ({
  getMailService: vi.fn()
}))

// Mock user store
const mockUserStore = {
  user: null as any,
  proxyId: ''
}

vi.mock('@/stores/modules/user', () => ({
  useUserStore: vi.fn(() => mockUserStore)
}))

// Import the mocked functions
import { getMailService } from '@/api/mail'

describe('Mail Store', () => {
  let mailStore: ReturnType<typeof useMailStore>

  // Mock data
  const mockMail1: Mail = {
    rowId: 1,
    title: 'Test Mail 1',
    message: 'This is test mail 1',
    hasRead: false,
    issuedDate: '2023-01-01T10:00:00Z',
    validUntil: 1672617600000,
    userId: 'user1'
  }

  const mockMail2: Mail = {
    rowId: 2,
    title: 'Test Mail 2',
    message: 'This is test mail 2',
    hasRead: true,
    issuedDate: '2023-01-02T10:00:00Z',
    validUntil: 1672617600000,
    userId: 'user1'
  }

  const mockMail3: Mail = {
    rowId: 3,
    title: 'Test Mail 3',
    message: 'This is test mail 3',
    hasRead: false,
    issuedDate: '2023-01-03T10:00:00Z',
    validUntil: 1672617600000,
    userId: 'user1'
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    mailStore = useMailStore()

    // Reset all mocks
    vi.clearAllMocks()
    mockUserStore.user = null
    mockUserStore.proxyId = ''
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      expect(mailStore.mails).toEqual([])
      expect(mailStore.unreadMails).toEqual([])
    })
  })

  describe('unreadMails computed', () => {
    it('should return empty array when no mails', () => {
      mailStore.mails = []

      expect(mailStore.unreadMails).toEqual([])
    })

    it('should return only unread mails', () => {
      mailStore.mails = [mockMail1, mockMail2, mockMail3]

      expect(mailStore.unreadMails).toEqual([mockMail1, mockMail3])
    })

    it('should return empty array when all mails are read', () => {
      const readMails = [
        { ...mockMail1, hasRead: true },
        { ...mockMail2, hasRead: true },
        { ...mockMail3, hasRead: true }
      ]
      mailStore.mails = readMails

      expect(mailStore.unreadMails).toEqual([])
    })
  })

  describe('getMails', () => {
    it('should fetch mails for regular user', async () => {
      const mockUser = { id: 'user1', admin: false }
      const mockResponse = {
        data: [mockMail1, mockMail2, mockMail3],
        message: 'Success'
      }

      mockUserStore.user = mockUser
      vi.mocked(getMailService).mockResolvedValue(mockResponse as any)

      await mailStore.getMails()

      expect(getMailService).toHaveBeenCalledWith('user1')
      expect(mailStore.mails).toEqual([mockMail1, mockMail2, mockMail3])
    })

    it('should fetch mails for admin user using proxyId', async () => {
      const mockAdmin = { id: 'admin1', admin: true }
      const mockResponse = {
        data: [mockMail1, mockMail2],
        message: 'Success'
      }

      mockUserStore.user = mockAdmin
      mockUserStore.proxyId = 'proxy123'
      vi.mocked(getMailService).mockResolvedValue(mockResponse as any)

      await mailStore.getMails()

      expect(getMailService).toHaveBeenCalledWith('proxy123')
      expect(mailStore.mails).toEqual([mockMail1, mockMail2])
    })

    it('should not fetch mails when no user', async () => {
      mockUserStore.user = null

      await mailStore.getMails()

      expect(getMailService).not.toHaveBeenCalled()
      expect(mailStore.mails).toEqual([])
    })

    it('should handle null response data', async () => {
      const mockUser = { id: 'user1', admin: false }
      const mockResponse = {
        data: null,
        message: 'Success'
      }

      mockUserStore.user = mockUser
      vi.mocked(getMailService).mockResolvedValue(mockResponse as any)

      await mailStore.getMails()

      expect(mailStore.mails).toEqual([])
    })

    it('should handle error when fetching mails', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const mockUser = { id: 'user1', admin: false }

      mockUserStore.user = mockUser
      vi.mocked(getMailService).mockRejectedValue(new Error('API Error'))

      await mailStore.getMails()

      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error))

      consoleSpy.mockRestore()
    })
  })
})
