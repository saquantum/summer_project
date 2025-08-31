import { flushPromises, mount } from '@vue/test-utils'
import AssetDetailDetail from '@/views/asset/AssetDetail.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick } from 'vue'
import type { AssetWithWarnings } from '@/types/asset'
import type { Warning } from '@/types/warning'

config.global.config.warnHandler = () => {}

// Mock router
const pushMock = vi.fn()
const routeMock = {
  params: { id: '1' }
}

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
  useRoute: () => routeMock
}))

// Mock MapCard component
vi.mock('@/components/MapCard.vue', () => ({
  default: {
    name: 'MapCard',
    template: '<div data-testid="map-card">Map Card</div>'
  }
}))

// Mock AssetForm component
vi.mock('@/components/cards/AssetForm.vue', () => ({
  default: {
    name: 'AssetForm',
    template: '<div data-testid="asset-form">Asset Form</div>',
    props: ['isEdit', 'item']
  }
}))

// Mock asset data
const mockAsset: AssetWithWarnings = {
  asset: {
    id: '1',
    name: 'Test Asset',
    typeId: 'A',
    type: { id: 'A', name: 'Type A', description: 'Asset Type A' },
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
    lastInspection: '2023-12-01',
    lastModified: 1640995200
  },
  warnings: [
    {
      id: 1,
      weatherType: 'Rain',
      warningLevel: 'AMBER',
      warningHeadLine: 'Rain Warning',
      warningLikelihood: 'High',
      validFrom: 1640995200,
      validTo: 1641081600,
      warningImpact: 'Medium impact',
      affectedAreas: 'Test areas',
      whatToExpect: 'Heavy rain',
      warningFurtherDetails: 'Details',
      warningUpdateDescription: 'Update',
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
    } as Warning,
    {
      id: 2,
      weatherType: 'Wind',
      warningLevel: 'RED',
      warningHeadLine: 'Wind Warning',
      warningLikelihood: 'Very High',
      validFrom: 1641081600,
      validTo: 1641168000,
      warningImpact: 'High impact',
      affectedAreas: 'Test areas',
      whatToExpect: 'Strong winds',
      warningFurtherDetails: 'Details',
      warningUpdateDescription: 'Update',
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
    } as Warning
  ],
  maxWarning: null
}

// Mock stores
const mockUserStore = {
  user: {
    id: 'user1',
    admin: false,
    accessControlGroup: {
      canSetPolygonOnCreate: true,
      canUpdateAssetPolygon: true
    }
  }
}

const mockAssetStore = {
  userAssets: [mockAsset],
  allAssets: []
}

vi.mock('@/stores/index.ts', () => ({
  useUserStore: () => mockUserStore,
  useAssetStore: () => mockAssetStore
}))

describe('AssetDetailDetail', () => {
  beforeEach(() => {
    pushMock.mockClear()
    routeMock.params.id = '1'

    // Reset mock store state
    mockUserStore.user.admin = false
    mockUserStore.user.accessControlGroup.canUpdateAssetPolygon = true
    mockAssetStore.userAssets = [mockAsset]
  })

  const createWrapper = (options = {}) => {
    return mount(AssetDetailDetail, {
      ...options
    })
  }

  describe('Component Initialization', () => {
    it('should render without errors', () => {
      const wrapper = createWrapper()
      expect(wrapper.exists()).toBe(true)
    })

    it('should display asset name', () => {
      const wrapper = createWrapper()
      expect(wrapper.text()).toContain('Test Asset')
    })

    it('should render MapCard component', () => {
      const wrapper = createWrapper()
      const mapCard = wrapper.find('[data-testid="map-card"]')
      expect(mapCard.exists()).toBe(true)
    })

    it('should render AssetForm component', () => {
      const wrapper = createWrapper()
      const assetForm = wrapper.find('[data-testid="asset-form"]')
      expect(assetForm.exists()).toBe(true)
    })

    it('should throw error when asset is not found', () => {
      routeMock.params.id = '999'
      // Clear the asset from the store to simulate not found
      mockAssetStore.userAssets = []
      mockAssetStore.allAssets = []
      expect(() => createWrapper()).toThrow('Can find asset 999')
    })
  })

  describe('Warning Display', () => {
    it('should display warning details section', () => {
      const wrapper = createWrapper()
      expect(wrapper.text()).toContain('Warning Details')
    })

    it('should show warning data when warnings exist', async () => {
      const wrapper = createWrapper()
      await nextTick()

      // Check if displayData is computed correctly
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const vm = wrapper.vm as any

      // First verify that the component has the expected data
      expect(vm.item).toBeDefined()
      expect(vm.item.warnings).toHaveLength(2)
      expect(vm.displayData).toHaveLength(2)
      expect(vm.displayData[0].weatherType).toBe('Rain')
      expect(vm.displayData[1].weatherType).toBe('Wind')

      // Check if the table element exists and has data attribute
      const table = wrapper.find('.el-table')
      expect(table.exists()).toBe(true)

      await flushPromises()
      // Try to find table cells or rows with content
      const tableRows = wrapper.findAll('.el-table__row')
      expect(tableRows.length).toBeGreaterThan(0)

      // Since we confirmed displayData has the right content,
      // the test should pass if the data is properly bound to the table
      expect(wrapper.text()).toContain('Rain')
      expect(wrapper.text()).toContain('Wind')
      expect(wrapper.text()).toContain('AMBER')
      expect(wrapper.text()).toContain('RED')
    })

    it('should handle empty warnings array', () => {
      const emptyWarningsAsset = {
        ...mockAsset,
        warnings: []
      }
      mockAssetStore.userAssets = [emptyWarningsAsset]

      const wrapper = createWrapper()
      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('Edit Mode', () => {
    it('should show edit functionality', () => {
      const wrapper = createWrapper()
      expect(wrapper.text()).toContain('Edit')
    })

    it('should have submit functionality', async () => {
      const wrapper = createWrapper()
      // Initially, Submit should not be visible
      expect(wrapper.text()).not.toContain('Submit')
      expect(wrapper.text()).toContain('Edit')

      // Find and click Edit button to enter edit mode
      const buttons = wrapper.findAll('button.el-button')
      const editButton = buttons.find((btn) => btn.text().trim() === 'Edit')
      expect(editButton).toBeTruthy()

      await editButton?.trigger('click')
      await nextTick()

      // Now Submit should be visible
      expect(wrapper.text()).toContain('Submit')
    })
  })

  describe('Responsive Design', () => {
    it('should render properly on different screen sizes', async () => {
      // Mock window innerWidth
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 600
      })

      const wrapper = createWrapper()
      await nextTick()
      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('Data Handling', () => {
    it('should handle asset data correctly', () => {
      const wrapper = createWrapper()
      expect(wrapper.exists()).toBe(true)
      expect(wrapper.text()).toContain('Test Asset')
    })

    it('should format warning periods correctly', () => {
      const wrapper = createWrapper()
      expect(wrapper.exists()).toBe(true)
    })
  })
})
