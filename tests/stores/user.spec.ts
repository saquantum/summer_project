/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/modules/user'
import type { LoginForm, User, UserItem, UserSearchBody } from '@/types'

// Mock the API services
vi.mock('@/api/user', () => ({
  userGetInfoService: vi.fn(),
  userLoginService: vi.fn()
}))

vi.mock('@/api/admin', () => ({
  adminSearchUsersService: vi.fn()
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

// Import the mocked functions
import { userGetInfoService, userLoginService } from '@/api/user'
import { adminSearchUsersService } from '@/api/admin'
import { ElMessage } from 'element-plus'

describe('User Store', () => {
  let userStore: ReturnType<typeof useUserStore>

  // Mock data
  const mockUser: User = {
    id: 'user1',
    password: null,
    admin: false,
    adminLevel: 0,
    avatar: 'avatar.png',
    name: 'John Doe',
    accessControlGroup: {
      rowId: 'acg1',
      name: 'Standard User',
      description: 'Standard user permissions',
      canCreateAsset: true,
      canSetPolygonOnCreate: false,
      canUpdateAssetFields: true,
      canUpdateAssetPolygon: false,
      canDeleteAsset: false,
      canUpdateProfile: true
    },
    address: {
      street: '123 Main St',
      city: 'London',
      postcode: 'SW1A 1AA',
      country: 'UK'
    },
    contactDetails: {
      email: 'john.doe@example.com',
      phone: '+44 20 1234 5678'
    },
    contactPreferences: {
      whatsapp: false,
      discord: false,
      post: false,
      phone: true,
      telegram: false,
      email: true
    }
  }

  const mockLoginForm: LoginForm = {
    username: 'john.doe',
    password: 'password123',
    email: 'john.doe@example.com'
  }

  const mockUserItem: UserItem = {
    user: mockUser,
    accumulation: 0
  }

  const mockUserSearchBody: UserSearchBody = {
    filters: {
      user_name: {
        op: 'contains',
        val: 'john'
      }
    },
    orderList: '',
    limit: 10,
    offset: 0
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    userStore = useUserStore()

    // Reset all mocks
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      expect(userStore.user).toBeNull()
      expect(userStore.users).toEqual([])
      expect(userStore.searchHistory).toEqual([])
      expect(userStore.userSearchHistory).toEqual([])
      expect(userStore.proxyId).toBe('')
    })
  })

  describe('getUser', () => {
    it('should login and get user info successfully', async () => {
      const mockResponse = {
        data: mockUser,
        message: 'Success'
      }

      vi.mocked(userLoginService).mockResolvedValue(undefined as any)
      vi.mocked(userGetInfoService).mockResolvedValue(mockResponse as any)

      await userStore.getUser(mockLoginForm)

      expect(userLoginService).toHaveBeenCalledWith(mockLoginForm)
      expect(userGetInfoService).toHaveBeenCalledWith('john.doe')
      expect(userStore.user).toEqual(mockUser)
      expect(ElMessage.success).toHaveBeenCalledWith('Success')
    })

    it('should handle 401 authentication error', async () => {
      const authError = {
        response: {
          status: 401
        }
      }

      vi.mocked(userLoginService).mockRejectedValue(authError)

      await expect(userStore.getUser(mockLoginForm)).rejects.toThrow()

      expect(ElMessage.error).toHaveBeenCalledWith(
        'Username or password is incorrect.'
      )
    })

    it('should handle unknown error', async () => {
      const unknownError = {
        response: {
          status: 500
        }
      }

      vi.mocked(userLoginService).mockRejectedValue(unknownError)

      await expect(userStore.getUser(mockLoginForm)).rejects.toThrow()

      expect(ElMessage.error).toHaveBeenCalledWith('Unknown Error')
    })

    it('should handle error without response status', async () => {
      const genericError = new Error('Network error')

      vi.mocked(userLoginService).mockRejectedValue(genericError)

      await expect(userStore.getUser(mockLoginForm)).rejects.toThrow()

      expect(ElMessage.error).toHaveBeenCalledWith('Unknown Error')
    })

    it('should handle null response data', async () => {
      const mockResponse = {
        data: null,
        message: 'Success'
      }

      vi.mocked(userLoginService).mockResolvedValue(undefined as any)
      vi.mocked(userGetInfoService).mockResolvedValue(mockResponse as any)

      await userStore.getUser(mockLoginForm)

      expect(userStore.user).toBeNull()
      expect(ElMessage.success).toHaveBeenCalledWith('Success')
    })
  })

  describe('getUsers', () => {
    it('should fetch users successfully', async () => {
      const mockResponse = {
        data: [mockUserItem],
        message: 'Success'
      }

      vi.mocked(adminSearchUsersService).mockResolvedValue(mockResponse as any)

      await userStore.getUsers('search', mockUserSearchBody)

      expect(adminSearchUsersService).toHaveBeenCalledWith(
        'search',
        mockUserSearchBody
      )
      expect(userStore.users).toEqual([mockUserItem])
    })

    it('should handle error when fetching users', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(adminSearchUsersService).mockRejectedValue(
        new Error('API Error')
      )

      await userStore.getUsers('search', mockUserSearchBody)

      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error))
      expect(userStore.users).toEqual([])

      consoleSpy.mockRestore()
    })

    it('should handle null response data', async () => {
      const mockResponse = {
        data: null,
        message: 'Success'
      }

      vi.mocked(adminSearchUsersService).mockResolvedValue(mockResponse as any)

      await userStore.getUsers('search', mockUserSearchBody)

      expect(userStore.users).toEqual([])
    })
  })

  describe('getUserInfo', () => {
    it('should fetch user info successfully when user exists', async () => {
      const updatedUser = { ...mockUser, name: 'John Updated' }
      const mockResponse = {
        data: updatedUser,
        message: 'Success'
      }

      // Set initial user
      userStore.user = mockUser
      vi.mocked(userGetInfoService).mockResolvedValue(mockResponse as any)

      await userStore.getUserInfo()

      expect(userGetInfoService).toHaveBeenCalledWith('user1')
      expect(userStore.user).toEqual(updatedUser)
    })

    it('should not fetch user info when no user exists', async () => {
      userStore.user = null

      await userStore.getUserInfo()

      expect(userGetInfoService).not.toHaveBeenCalled()
      expect(userStore.user).toBeNull()
    })

    it('should handle error when fetching user info', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      userStore.user = mockUser
      vi.mocked(userGetInfoService).mockRejectedValue(new Error('API Error'))

      await userStore.getUserInfo()

      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error))

      consoleSpy.mockRestore()
    })
  })

  describe('updateUser', () => {
    it('should update user successfully', () => {
      const updatedUser = { ...mockUser, name: 'Jane Doe' }

      userStore.updateUser(updatedUser)

      expect(userStore.user).toEqual(updatedUser)
    })
  })

  describe('setProxyId', () => {
    it('should set proxy id successfully', () => {
      const proxyId = 'proxy123'

      userStore.setProxyId(proxyId)

      expect(userStore.proxyId).toBe(proxyId)
    })
  })

  describe('clearUserSearchHistory', () => {
    it('should clear user search history', () => {
      // Set some search history
      userStore.userSearchHistory = ['search1', 'search2', 'search3']

      userStore.clearUserSearchHistory()

      expect(userStore.userSearchHistory).toEqual([])
    })
  })
})
