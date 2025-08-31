/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useWarningStore } from '@/stores/modules/warning'
import type { Warning } from '@/types'

// Mock the API services
vi.mock('@/api/admin', () => ({
  adminGetAllWarningsService: vi.fn(),
  adminGetAllLiveWarningsService: vi.fn()
}))

// Import the mocked functions
import {
  adminGetAllWarningsService,
  adminGetAllLiveWarningsService
} from '@/api/admin'

describe('Warning Store', () => {
  let warningStore: ReturnType<typeof useWarningStore>

  // Mock data
  const mockWarning1: Warning = {
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

  const mockWarning2: Warning = {
    id: 2,
    weatherType: 'Snow',
    warningLevel: 'AMBER',
    warningHeadLine: 'Snow Warning',
    validFrom: 1672531200000,
    validTo: 1672617600000,
    warningImpact: 'Medium',
    warningLikelihood: 'Possible',
    affectedAreas: 'North Area',
    whatToExpect: 'Heavy snowfall expected',
    warningFurtherDetails: 'Additional snow details',
    warningUpdateDescription: 'Updated snow warning',
    area: {
      type: 'MultiPolygon',
      coordinates: [
        [
          [
            [2, 2],
            [3, 2],
            [3, 3],
            [2, 3],
            [2, 2]
          ]
        ]
      ]
    }
  }

  const mockWarning3: Warning = {
    id: 3,
    weatherType: 'Wind',
    warningLevel: 'YELLOW',
    warningHeadLine: 'Wind Warning',
    validFrom: 1672531200000,
    validTo: 1672617600000,
    warningImpact: 'Low',
    warningLikelihood: 'Unlikely',
    affectedAreas: 'South Area',
    whatToExpect: 'Strong winds expected',
    warningFurtherDetails: 'Additional wind details',
    warningUpdateDescription: 'Updated wind warning',
    area: {
      type: 'MultiPolygon',
      coordinates: [
        [
          [
            [4, 4],
            [5, 4],
            [5, 5],
            [4, 5],
            [4, 4]
          ]
        ]
      ]
    }
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    warningStore = useWarningStore()

    // Reset all mocks
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initial state', () => {
    it('should have correct initial state', () => {
      expect(warningStore.allWarnings).toEqual([])
      expect(warningStore.liveWarnings).toEqual([])
      expect(warningStore.outdatedWarnings).toEqual([])
    })
  })

  describe('getAllWarnings', () => {
    it('should fetch and store all warnings successfully', async () => {
      const mockResponse = {
        data: [mockWarning1, mockWarning2, mockWarning3],
        message: 'Success'
      }

      vi.mocked(adminGetAllWarningsService).mockResolvedValue(
        mockResponse as any
      )

      await warningStore.getAllWarnings()

      expect(adminGetAllWarningsService).toHaveBeenCalled()
      expect(warningStore.allWarnings).toEqual([
        mockWarning1,
        mockWarning2,
        mockWarning3
      ])
    })

    it('should not update warnings when response has no data', async () => {
      const mockResponse = {
        data: null,
        message: 'Success'
      }

      vi.mocked(adminGetAllWarningsService).mockResolvedValue(
        mockResponse as any
      )

      await warningStore.getAllWarnings()

      expect(warningStore.allWarnings).toEqual([])
    })

    it('should not update warnings when response has empty array', async () => {
      const mockResponse = {
        data: [],
        message: 'Success'
      }

      vi.mocked(adminGetAllWarningsService).mockResolvedValue(
        mockResponse as any
      )

      await warningStore.getAllWarnings()

      expect(warningStore.allWarnings).toEqual([])
    })
  })

  describe('getAllLiveWarnings', () => {
    it('should fetch and store live warnings successfully', async () => {
      const mockResponse = {
        data: [mockWarning1, mockWarning2],
        message: 'Success'
      }

      vi.mocked(adminGetAllLiveWarningsService).mockResolvedValue(
        mockResponse as any
      )

      await warningStore.getAllLiveWarnings()

      expect(adminGetAllLiveWarningsService).toHaveBeenCalled()
      expect(warningStore.liveWarnings).toEqual([mockWarning1, mockWarning2])
    })

    it('should not update live warnings when response has no data', async () => {
      const mockResponse = {
        data: null,
        message: 'Success'
      }

      vi.mocked(adminGetAllLiveWarningsService).mockResolvedValue(
        mockResponse as any
      )

      await warningStore.getAllLiveWarnings()

      expect(warningStore.liveWarnings).toEqual([])
    })

    it('should not update live warnings when response has empty array', async () => {
      const mockResponse = {
        data: [],
        message: 'Success'
      }

      vi.mocked(adminGetAllLiveWarningsService).mockResolvedValue(
        mockResponse as any
      )

      await warningStore.getAllLiveWarnings()

      expect(warningStore.liveWarnings).toEqual([])
    })
  })

  describe('outdatedWarnings computed', () => {
    it('should return empty array when no all warnings', () => {
      warningStore.allWarnings = []
      warningStore.liveWarnings = [mockWarning1]

      expect(warningStore.outdatedWarnings).toEqual([])
    })

    it('should return all warnings when no live warnings', () => {
      warningStore.allWarnings = [mockWarning1, mockWarning2, mockWarning3]
      warningStore.liveWarnings = []

      expect(warningStore.outdatedWarnings).toEqual([
        mockWarning1,
        mockWarning2,
        mockWarning3
      ])
    })

    it('should return warnings that are not in live warnings', () => {
      warningStore.allWarnings = [mockWarning1, mockWarning2, mockWarning3]
      warningStore.liveWarnings = [mockWarning1, mockWarning2]

      expect(warningStore.outdatedWarnings).toEqual([mockWarning3])
    })

    it('should return empty array when all warnings are live', () => {
      warningStore.allWarnings = [mockWarning1, mockWarning2]
      warningStore.liveWarnings = [mockWarning1, mockWarning2]

      expect(warningStore.outdatedWarnings).toEqual([])
    })
  })
})
