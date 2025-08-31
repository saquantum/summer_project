/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAssetStore } from '@/stores/modules/asset'
import type {
  AssetWithWarnings,
  AssetType,
  Warning,
  AssetSearchBody
} from '@/types'

// Mock the API services
vi.mock('@/api/assets', () => ({
  assetsGetInfoService: vi.fn(),
  getAssetByIdService: vi.fn(),
  getAssetTypesService: vi.fn()
}))

vi.mock('@/api/admin', () => ({
  adminGetUserAssetsService: vi.fn(),
  adminSearchAssetService: vi.fn(),
  adminGetAssetByIdService: vi.fn()
}))

// Mock user data

const mockUser = {
  id: 'user1',
  name: 'John Doe' as string,
  admin: false,
  accessControlGroup: {
    userId: 'user1',
    canCreateAsset: true,
    canSetPolygonOnCreate: false,
    canUpdateAssetFields: true,
    canUpdateAssetPolygon: false,
    canDeleteAsset: false,
    canUpdateProfile: true
  }
}

const mockAdmin = {
  id: 'admin1',
  name: 'Admin User' as string,
  admin: true,
  accessControlGroup: {
    userId: 'admin1',
    canCreateAsset: true,
    canSetPolygonOnCreate: true,
    canUpdateAssetFields: true,
    canUpdateAssetPolygon: true,
    canDeleteAsset: true,
    canUpdateProfile: true
  }
}

// Mock stores
const mockUserStore = {
  user: {}
}

// Mock useUserStore
vi.mock('@/stores/modules/user', () => ({
  useUserStore: vi.fn(() => mockUserStore)
}))

// Import the mocked functions
import {
  assetsGetInfoService,
  getAssetByIdService,
  getAssetTypesService
} from '@/api/assets'
import {
  adminGetUserAssetsService,
  adminSearchAssetService,
  adminGetAssetByIdService
} from '@/api/admin'

describe('Asset Store', () => {
  let assetStore: ReturnType<typeof useAssetStore>

  // Mock data
  const mockAssetType: AssetType = {
    id: '1',
    name: 'Water Tank',
    description: 'Storage tank for water'
  }

  const mockWarning: Warning = {
    id: 1,
    weatherType: 'Heavy Rain',
    warningLevel: 'RED',
    warningHeadLine: 'Critical Issue',
    validFrom: 1672531200000,
    validTo: 1672617600000,
    warningImpact: 'High',
    warningLikelihood: 'Likely',
    affectedAreas: 'Test Area',
    whatToExpect: 'Heavy rainfall expected',
    warningFurtherDetails: 'Additional details',
    warningUpdateDescription: 'Updated warning',
    area: {
      type: 'MultiPolygon',
      coordinates: [
        [
          [
            [0, 0],
            [1, 0],
            [1, 1],
            [0, 1],
            [0, 0]
          ]
        ]
      ]
    }
  }

  const mockAssetWithWarnings: AssetWithWarnings = {
    asset: {
      id: 'asset1',
      name: 'Tank A',
      typeId: '1',
      type: mockAssetType,
      ownerId: 'user1',
      location: {
        type: 'MultiPolygon',
        coordinates: [
          [
            [
              [0, 0],
              [1, 0],
              [1, 1],
              [0, 1],
              [0, 0]
            ]
          ]
        ]
      },
      capacityLitres: 1000,
      material: 'Steel',
      status: 'Active',
      installedAt: '2023-01-01',
      lastInspection: '2023-06-01',
      lastModified: 1672531200000
    },
    warnings: [mockWarning]
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    assetStore = useAssetStore()

    // Reset all mocks
    vi.clearAllMocks()
    // Reset mockUserStore
    mockUserStore.user = {}
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getMaxWarningLevel', () => {
    it('should return null for empty warnings array', () => {
      const result = assetStore.getMaxWarningLevel([])
      expect(result).toBeNull()
    })

    it('should return the warning with highest level', () => {
      const warnings: Warning[] = [
        { ...mockWarning, warningLevel: 'YELLOW' },
        { ...mockWarning, id: 2, warningLevel: 'RED' },
        { ...mockWarning, id: 3, warningLevel: 'AMBER' }
      ]

      const result = assetStore.getMaxWarningLevel(warnings)
      expect(result?.id).toBe(2)
      expect(result?.warningLevel).toBe('RED')
    })
  })

  describe('getAssetTypes', () => {
    it('should fetch and store asset types successfully', async () => {
      const mockResponse = {
        data: [mockAssetType],
        message: 'Success'
      }

      vi.mocked(getAssetTypesService).mockResolvedValue(mockResponse as any)

      await assetStore.getAssetTypes()

      expect(getAssetTypesService).toHaveBeenCalled()
      expect(assetStore.assetTypes).toEqual([mockAssetType])
    })

    it('should handle error when fetching asset types', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getAssetTypesService).mockRejectedValue(new Error('API Error'))

      await assetStore.getAssetTypes()

      expect(getAssetTypesService).toHaveBeenCalledOnce()
      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error))
      expect(assetStore.assetTypes).toEqual([])

      consoleSpy.mockRestore()
    })

    it('should handle null response data', async () => {
      const mockResponse = {
        code: 200,
        data: null,
        message: 'Success'
      }

      vi.mocked(getAssetTypesService).mockResolvedValue(mockResponse as any)

      await assetStore.getAssetTypes()

      expect(assetStore.assetTypes).toEqual([])
    })
  })

  describe('typeOptions computed', () => {
    it('should return empty array when no asset types', () => {
      expect(assetStore.typeOptions).toEqual([])
    })

    it('should transform asset types to options format', async () => {
      const mockResponse = {
        code: 200,
        data: [
          { id: '1', name: 'Water Tank', description: 'Tank for water' },
          { id: '2', name: 'Pump', description: 'Water pump' }
        ],

        message: 'Success'
      }

      vi.mocked(getAssetTypesService).mockResolvedValue(mockResponse)
      await assetStore.getAssetTypes()

      expect(assetStore.typeOptions).toEqual([
        { label: 'Water Tank', value: '1' },
        { label: 'Pump', value: '2' }
      ])
    })
  })

  describe('getUserAssets', () => {
    it('should fetch user assets for non-admin user', async () => {
      const mockResponse = {
        code: 200,
        data: [mockAssetWithWarnings],

        message: 'Success'
      }

      vi.mocked(assetsGetInfoService).mockResolvedValue(mockResponse)

      await assetStore.getUserAssets(false, 'user1')

      expect(assetsGetInfoService).toHaveBeenCalledWith('user1')
      expect(adminGetUserAssetsService).not.toHaveBeenCalled()
      expect(assetStore.userAssets).toHaveLength(1)
      expect(assetStore.userAssets[0].maxWarning).toBeDefined()
    })

    it('should fetch user assets for admin user', async () => {
      const mockResponse = {
        code: 200,
        data: [mockAssetWithWarnings],

        message: 'Success'
      }

      vi.mocked(adminGetUserAssetsService).mockResolvedValue(mockResponse)

      await assetStore.getUserAssets(true, 'user1')

      expect(adminGetUserAssetsService).toHaveBeenCalledWith('user1')
      expect(assetsGetInfoService).not.toHaveBeenCalled()
      expect(assetStore.userAssets).toHaveLength(1)
    })

    it('should set empty array when no data returned', async () => {
      const mockResponse = {
        code: 200,
        data: [] as AssetWithWarnings[],
        message: 'Success'
      }

      vi.mocked(assetsGetInfoService).mockResolvedValue(mockResponse)

      await assetStore.getUserAssets(false, 'user1')

      expect(assetStore.userAssets).toEqual([])
    })

    it('should handle error when fetching user assets', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(assetsGetInfoService).mockRejectedValue(new Error('API Error'))

      await assetStore.getUserAssets(false, 'user1')

      expect(consoleSpy).toHaveBeenCalledWith(
        'fail to get user asset',
        expect.any(Error)
      )

      consoleSpy.mockRestore()
    })
  })

  describe('getAllAssets', () => {
    const mockSearchBody: AssetSearchBody = {
      filters: {
        asset_name: {
          op: 'contains',
          val: 'test'
        }
      },
      orderList: '',
      limit: 10,
      offset: 0
    }

    it('should fetch all assets successfully', async () => {
      const mockResponse = {
        data: [mockAssetWithWarnings]
      }

      vi.mocked(adminSearchAssetService).mockResolvedValue(mockResponse as any)

      await assetStore.getAllAssets(mockSearchBody)

      expect(adminSearchAssetService).toHaveBeenCalledWith(mockSearchBody)
      expect(assetStore.allAssets).toHaveLength(1)
      expect(assetStore.allAssets[0].maxWarning).toBeDefined()
    })

    it('should handle empty response', async () => {
      const mockResponse = {
        data: []
      }

      vi.mocked(adminSearchAssetService).mockResolvedValue(mockResponse as any)

      await assetStore.getAllAssets(mockSearchBody)

      expect(assetStore.allAssets).toEqual([])
    })

    it('should handle error when fetching all assets', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(adminSearchAssetService).mockRejectedValue(
        new Error('API Error')
      )

      await assetStore.getAllAssets(mockSearchBody)

      expect(consoleSpy).toHaveBeenCalledWith(
        'fail to get all asset',
        expect.any(Error)
      )

      consoleSpy.mockRestore()
    })
  })

  describe('updateAssetById', () => {
    beforeEach(() => {
      // Set up initial assets in store
      assetStore.userAssets = [mockAssetWithWarnings]
      assetStore.allAssets = [mockAssetWithWarnings]
    })

    it('should update asset for admin user', async () => {
      const updatedAsset = {
        ...mockAssetWithWarnings,
        asset: { ...mockAssetWithWarnings.asset, name: 'Updated Tank' }
      }

      const mockResponse = {
        data: [updatedAsset],
        message: 'Success'
      }

      // Mock user store to return admin user
      mockUserStore.user = mockAdmin
      vi.mocked(adminGetAssetByIdService).mockResolvedValue(mockResponse as any)

      await assetStore.updateAssetById('asset1')

      expect(adminGetAssetByIdService).toHaveBeenCalledWith('asset1')
      expect(getAssetByIdService).not.toHaveBeenCalled()
      expect(assetStore.userAssets[0].asset.name).toBe('Updated Tank')
      expect(assetStore.allAssets[0].asset.name).toBe('Updated Tank')
    })

    it('should update asset for regular user', async () => {
      const updatedAsset = {
        ...mockAssetWithWarnings,
        asset: { ...mockAssetWithWarnings.asset, name: 'Updated Tank' }
      }

      const mockResponse = {
        data: [updatedAsset],

        message: 'Success'
      }

      // Mock user store to return regular user
      mockUserStore.user = mockUser
      vi.mocked(getAssetByIdService).mockResolvedValue(mockResponse as any)

      await assetStore.updateAssetById('asset1')

      expect(getAssetByIdService).toHaveBeenCalledWith('user1', 'asset1')
      expect(adminGetAssetByIdService).not.toHaveBeenCalled()
      expect(assetStore.userAssets[0].asset.name).toBe('Updated Tank')
    })

    it('should handle asset not found in arrays', async () => {
      const mockResponse = {
        data: [mockAssetWithWarnings],

        message: 'Success'
      }

      mockUserStore.user = mockUser
      vi.mocked(getAssetByIdService).mockResolvedValue(mockResponse as any)

      await assetStore.updateAssetById('nonexistent-asset')

      expect(getAssetByIdService).toHaveBeenCalledWith(
        'user1',
        'nonexistent-asset'
      )
      // Original assets should remain unchanged
      expect(assetStore.userAssets[0].asset.name).toBe('Tank A')
    })

    it('should handle no response data', async () => {
      const mockResponse = {
        data: null,
        message: 'Success'
      }

      mockUserStore.user = mockUser
      vi.mocked(getAssetByIdService).mockResolvedValue(mockResponse as any)

      await assetStore.updateAssetById('asset1')

      // Assets should remain unchanged
      expect(assetStore.userAssets[0].asset.name).toBe('Tank A')
    })

    it('should handle error when updating asset', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      mockUserStore.user = mockUser
      vi.mocked(getAssetByIdService).mockRejectedValue(new Error('API Error'))

      await assetStore.updateAssetById('asset1')

      expect(consoleSpy).toHaveBeenCalledWith(
        'fail to update asset',
        expect.any(Error)
      )
      consoleSpy.mockRestore()
    })
  })
})
