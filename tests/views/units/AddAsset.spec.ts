/* eslint-disable @typescript-eslint/no-explicit-any */
import { flushPromises, mount } from '@vue/test-utils'
import AddAsset from '@/views/asset/AddAsset.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore, useAssetStore } from '@/stores/index.ts'
import { adminGetUserInfoService, adminInsertAssetService } from '@/api/admin'
import { userInsertAssetService } from '@/api/user'

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
  useRoute: () => ({ path: '/assets/add' })
}))

// Mock request utility
vi.mock('@/utils/request', () => ({
  default: vi.fn()
}))

// Mock API services
vi.mock('@/api/admin', () => ({
  adminGetUserInfoService: vi.fn(),
  adminInsertAssetService: vi.fn()
}))

vi.mock('@/api/user', () => ({
  userInsertAssetService: vi.fn()
}))

// // Mock Element Plus - partial mock approach
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn()
    }
  }
})

// Mock MapCard component since it's complex
vi.mock('@/components/MapCard.vue', () => ({
  default: {
    name: 'MapCard',
    template: '<div data-testid="map-card-mock">Mock Map Card</div>',
    props: ['map-id', 'locations', 'mode'],
    emits: ['update:locations', 'update:mode'],
    methods: {
      beginDrawing: vi.fn(),
      endDrawing: vi.fn(),
      cancelDrawing: vi.fn()
    }
  }
}))

// Mock stores with different user configurations
const createMockUserStore = (overrides = {}) =>
  ({
    user: {
      id: 'testuser123',
      admin: false,
      assetHolderId: 'holder123',
      accessControlGroup: {
        canCreateAsset: true,
        canSetPolygonOnCreate: true
      },
      ...overrides
    }
  }) as any

const mockAssetStore = {
  typeOptions: [
    { label: 'Tank', value: 'tank' },
    { label: 'Pipeline', value: 'pipeline' },
    { label: 'Valve', value: 'valve' }
  ]
}

// Mock stores
vi.mock('@/stores/index.ts', () => ({
  useUserStore: vi.fn(),
  useAssetStore: vi.fn()
}))

// Mock form utils
vi.mock('@/utils/formUtils', () => ({
  createUserRules: () => [
    { required: true, message: 'Username is required', trigger: 'blur' }
  ],
  trimForm: (form: any) => {
    // Simple trim mock
    if (form.name) form.name = form.name.trim()
    if (form.username) form.username = form.username.trim()
  }
}))

describe('AddAsset.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    pushMock.mockClear()
    vi.mocked(ElMessage.success).mockClear()
    vi.mocked(ElMessage.error).mockClear()
    vi.mocked(adminGetUserInfoService).mockClear()
    vi.mocked(userInsertAssetService).mockClear()
    vi.mocked(adminInsertAssetService).mockClear()
    vi.mocked(request).mockClear()

    // Default mock setup
    ;(vi.mocked(useUserStore) as any).mockReturnValue(createMockUserStore())
    ;(vi.mocked(useAssetStore) as any).mockReturnValue(mockAssetStore)
  })

  describe('Component Rendering', () => {
    it('renders the form correctly for regular users', () => {
      const wrapper = mount(AddAsset)

      // Check specifically for the restriction message, not just any h3
      expect(wrapper.find('h3').text()).not.toBe(
        'You can not add asset right now'
      )
      expect(wrapper.find('[data-testid="map-card-mock"]').exists()).toBe(true)
      expect(wrapper.find('.form-button').exists()).toBe(true)
    })

    it('shows restriction message for users without create permission', () => {
      ;(vi.mocked(useUserStore) as any).mockReturnValue(
        createMockUserStore({
          accessControlGroup: {
            canCreateAsset: false,
            canSetPolygonOnCreate: false
          }
        })
      )

      const wrapper = mount(AddAsset)
      expect(wrapper.find('h3').text()).toBe('You can not add asset right now')
    })

    it('renders form for admin users', () => {
      ;(vi.mocked(useUserStore) as any).mockReturnValue(
        createMockUserStore({
          admin: true,
          id: 'admin123'
        })
      )

      const wrapper = mount(AddAsset)
      expect(wrapper.find('[data-testid="map-card-mock"]').exists()).toBe(true)
    })
  })

  describe('Form Validation', () => {
    it('validates required fields', async () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Mock form validation to fail (throw error like Element Plus does)
      vm.formRef = {
        validate: vi.fn().mockRejectedValue({ name: 'Name is required' }),
        scrollToField: vi.fn()
      }

      await vm.userSubmit()
      await flushPromises()

      // Should not call insert service without valid form
      expect(vi.mocked(userInsertAssetService)).not.toHaveBeenCalled()
    })

    it('accepts valid form data', async () => {
      vi.mocked(userInsertAssetService).mockResolvedValue({
        code: 200,
        message: 'Success',
        data: undefined
      })

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Set up valid form data
      vm.form = {
        username: 'testuser123',
        name: 'Test Asset',
        typeId: 'tank',
        ownerId: 'holder123',
        address: 'Test Address',
        locations: [
          {
            type: 'MultiPolygon',
            coordinates: []
          }
        ],
        capacityLitres: 1000,
        material: 'Steel',
        status: 'active',
        installedAt: '2024-01-01',
        lastInspection: '2024-01-01',
        location: {
          type: 'MultiPolygon',
          coordinates: []
        }
      }

      // Mock form validation to pass
      vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }

      await vm.userSubmit()
      await flushPromises()

      expect(vi.mocked(userInsertAssetService)).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith(
        'Successfully add an asset'
      )
    })
  })

  describe('Location Search', () => {
    it('searches for location and updates form', async () => {
      const mockLocationData = [
        {
          place_id: '123',
          name: 'Test Location',
          display_name: 'Test Location, Test City',
          class: 'place',
          type: 'house',
          importance: 0.5,
          lat: '51.5074',
          lon: '-0.1278',
          boundingbox: ['51.5', '51.6', '-0.2', '-0.1']
        }
      ]

      vi.mocked(request).mockResolvedValue(mockLocationData)

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Test the searchLocation method
      await vm.searchLocation('Test Address')
      await flushPromises()

      expect(vi.mocked(request)).toHaveBeenCalledWith(
        expect.stringContaining(
          'nominatim.openstreetmap.org/search?format=json&q=Test%20Address'
        )
      )
      expect(vm.form.locations).toHaveLength(1)
      expect(vm.form.locations[0]).toHaveProperty('type', 'MultiPolygon')
    })

    it('handles empty search results gracefully', async () => {
      vi.mocked(request).mockResolvedValue([])

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      await vm.searchLocation('Nonexistent Place')
      await flushPromises()

      // Should not crash or modify locations if no results
      expect(vm.form.locations).toHaveLength(1) // Default location should remain
    })
  })

  describe('GeoJSON Conversion', () => {
    it('converts to Point geometry correctly', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      const mockData = {
        place_id: '123',
        name: 'Test',
        display_name: 'Test Location',
        class: 'place',
        type: 'house',
        importance: 0.5,
        lat: '51.5074',
        lon: '-0.1278',
        boundingbox: ['51.5', '51.6', '-0.2', '-0.1']
      }

      const result = vm.convertToGeoJSON(mockData, 'point')

      expect(result.type).toBe('Feature')
      expect(result.geometry.type).toBe('Point')
      expect(result.geometry.coordinates).toEqual([-0.1278, 51.5074])
    })

    it('converts to Polygon geometry correctly', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      const mockData = {
        place_id: '123',
        name: 'Test',
        display_name: 'Test Location',
        class: 'place',
        type: 'house',
        importance: 0.5,
        lat: '51.5074',
        lon: '-0.1278',
        boundingbox: ['51.5', '51.6', '-0.2', '-0.1']
      }

      const result = vm.convertToGeoJSON(mockData, 'polygon')

      expect(result.type).toBe('Feature')
      expect(result.geometry.type).toBe('Polygon')
      expect(result.geometry.coordinates).toHaveLength(1)
      expect(result.geometry.coordinates[0]).toHaveLength(5) // Closed polygon
    })

    it('converts to MultiPolygon geometry correctly', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      const mockData = {
        place_id: '123',
        name: 'Test',
        display_name: 'Test Location',
        class: 'place',
        type: 'house',
        importance: 0.5,
        lat: '51.5074',
        lon: '-0.1278',
        boundingbox: ['51.5', '51.6', '-0.2', '-0.1']
      }

      const result = vm.convertToGeoJSON(mockData, 'multipolygon')

      expect(result.type).toBe('Feature')
      expect(result.geometry.type).toBe('MultiPolygon')
      expect(result.geometry.coordinates).toHaveLength(1)
      expect(result.geometry.coordinates[0]).toHaveLength(1)
      expect(result.geometry.coordinates[0][0]).toHaveLength(5) // Closed polygon
    })

    it('throws error for invalid data', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      expect(() => {
        vm.convertToGeoJSON({}, 'point')
      }).toThrow('Invalid input data: missing lat/lon')

      expect(() => {
        vm.convertToGeoJSON({ lat: '51.5', lon: '-0.1' }, 'point')
      }).toThrow('Invalid boundingbox')
    })
  })

  describe('Form Submission', () => {
    it('submits user form successfully', async () => {
      vi.mocked(userInsertAssetService).mockResolvedValue({} as any)

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Set up valid form data
      vm.form = {
        username: 'testuser123',
        name: 'Test Asset',
        typeId: 'tank',
        ownerId: 'testuser123',
        address: 'Test Address',
        locations: [
          {
            type: 'MultiPolygon',
            coordinates: []
          }
        ],
        capacityLitres: 1000,
        material: 'Steel',
        status: 'active',
        installedAt: '2024-01-01',
        lastInspection: '2024-01-01',
        location: {
          type: 'MultiPolygon',
          coordinates: []
        }
      }

      // Mock form validation
      vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }

      await vm.userSubmit()
      await flushPromises()

      expect(vi.mocked(userInsertAssetService)).toHaveBeenCalledWith(
        'testuser123',
        expect.objectContaining({
          name: 'Test Asset',
          capacityLitres: 1000, // Should be converted to number
          ownerId: 'testuser123'
        })
      )
      expect(ElMessage.success).toHaveBeenCalledWith(
        'Successfully add an asset'
      )
    })

    it('handles user submission error', async () => {
      vi.mocked(userInsertAssetService).mockRejectedValue(
        new Error('API Error')
      )

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      vm.form = {
        username: 'testuser123',
        name: 'Test Asset',
        typeId: 'tank',
        ownerId: 'holder123',
        address: 'Test Address',
        locations: [
          {
            type: 'MultiPolygon',
            coordinates: []
          }
        ],
        capacityLitres: 1000,
        material: 'Steel',
        status: 'active',
        installedAt: '2024-01-01',
        lastInspection: '2024-01-01',
        location: {
          type: 'MultiPolygon',
          coordinates: []
        }
      }

      vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }

      await vm.userSubmit()
      await flushPromises()

      expect(ElMessage.error).toHaveBeenCalledWith(
        'An error occurs during adding an asset'
      )
    })

    it('submits admin form successfully', async () => {
      vi.mocked(adminGetUserInfoService).mockResolvedValue({
        code: 200,
        message: 'Success',
        data: {
          assetHolderId: 'targetUser123'
        } as any
      })
      vi.mocked(adminInsertAssetService).mockResolvedValue({} as any)
      ;(vi.mocked(useUserStore) as any).mockReturnValue(
        createMockUserStore({
          admin: true,
          id: 'admin123'
        })
      )

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      vm.form = {
        username: 'targetuser',
        name: 'Admin Test Asset',
        typeId: 'pipeline',
        ownerId: 'targetuser',
        address: 'Admin Test Address',
        locations: [
          {
            type: 'MultiPolygon',
            coordinates: []
          }
        ],
        capacityLitres: 2000,
        material: 'Concrete',
        status: 'maintenance',
        installedAt: '2024-01-01',
        lastInspection: '2024-01-01',
        location: {
          type: 'MultiPolygon',
          coordinates: []
        }
      }

      vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }

      await vm.adminSubmit()
      await flushPromises()

      expect(vi.mocked(adminInsertAssetService)).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Admin Test Asset',
          ownerId: 'targetuser',
          capacityLitres: 2000
        })
      )
      expect(ElMessage.success).toHaveBeenCalledWith(
        'Successfully add an asset'
      )
    })
  })

  describe('Drawing Controls', () => {
    it('manages drawing state correctly', async () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Mock the mapCardRef methods
      vm.mapCardRef = {
        beginDrawing: vi.fn(),
        endDrawing: vi.fn(),
        cancelDrawing: vi.fn()
      }

      expect(vm.isDrawing).toBe(false)

      vm.beginDrawing()
      expect(vm.isDrawing).toBe(true)

      vm.endDrawing()
      expect(vm.isDrawing).toBe(false)

      vm.beginDrawing()
      vm.cancelDrawing()
      expect(vm.isDrawing).toBe(false)
    })
  })

  describe('Form Reset', () => {
    it('resets form to initial state', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Modify form
      vm.form.name = 'Modified Asset'
      vm.form.material = 'Modified Material'

      vm.reset()

      expect(vm.form.name).toBe('')
      expect(vm.form.material).toBe('')
      expect(vm.form.locations).toHaveLength(1)
      expect(vm.form.locations[0]).toEqual({
        type: 'MultiPolygon',
        coordinates: []
      })
    })
  })

  describe('Date Validation', () => {
    it('disables future dates', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      const futureDate = new Date()
      futureDate.setDate(futureDate.getDate() + 1)

      const pastDate = new Date()
      pastDate.setDate(pastDate.getDate() - 1)

      expect(vm.disabledAfterToday(futureDate)).toBe(true)
      expect(vm.disabledAfterToday(pastDate)).toBe(false)
    })
  })

  describe('Permission Checks', () => {
    it('disables add asset for users without permission', () => {
      ;(vi.mocked(useUserStore) as any).mockReturnValue(
        createMockUserStore({
          accessControlGroup: {
            canCreateAsset: false,
            canSetPolygonOnCreate: false
          }
        })
      )

      const wrapper = mount(AddAsset)

      const vm = wrapper.vm as any

      expect(vm.disableAddAsset).toBe(true)
    })

    it('disables polygon setting for users without permission', () => {
      ;(vi.mocked(useUserStore) as any).mockReturnValue(
        createMockUserStore({
          accessControlGroup: {
            canCreateAsset: true,
            canSetPolygonOnCreate: false
          }
        })
      )

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      expect(vm.disableSetPolygon).toBe(true)
    })

    it('enables features for users with permission', () => {
      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      expect(vm.disableAddAsset).toBe(false)
      expect(vm.disableSetPolygon).toBe(false)
    })
  })

  describe('Default Polygon Handling', () => {
    it('clears default polygon when submitting', async () => {
      vi.mocked(userInsertAssetService).mockResolvedValue({} as any)

      const wrapper = mount(AddAsset)
      const vm = wrapper.vm as any

      // Set form with default polygon
      vm.form = {
        username: 'testuser123',
        name: 'Test Asset',
        typeId: 'tank',
        ownerId: 'holder123',
        address: 'Test Address',
        locations: [
          {
            type: 'MultiPolygon',
            coordinates: []
          }
        ],
        capacityLitres: 1000,
        material: 'Steel',
        status: 'active',
        installedAt: '2024-01-01',
        lastInspection: '2024-01-01',
        location: {
          type: 'MultiPolygon',
          coordinates: []
        }
      }

      vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }

      await vm.userSubmit()
      await flushPromises()

      expect(vi.mocked(userInsertAssetService)).toHaveBeenCalledWith(
        'testuser123',
        expect.objectContaining({
          location: {
            type: 'MultiPolygon',
            coordinates: []
          }
        })
      )
    })
  })
})
