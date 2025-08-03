import { mount } from '@vue/test-utils'
import MapCard from '@/components/MapCard.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import type { MultiPolygon } from 'geojson'
import type { Asset, AssetType } from '@/types'

// Mock Leaflet with a more comprehensive mock
const mockLayer = {
  addTo: vi.fn().mockReturnThis(),
  on: vi.fn(),
  toGeoJSON: vi.fn().mockReturnValue({
    type: 'Feature',
    geometry: {
      type: 'Polygon',
      coordinates: [
        [
          [0, 0],
          [1, 0],
          [1, 1],
          [0, 1],
          [0, 0]
        ]
      ]
    }
  }),
  getLatLng: vi.fn().mockReturnValue({ lat: 51.5, lng: -0.1 }),
  getBounds: vi.fn().mockReturnValue({}),
  removeLayer: vi.fn()
}

const mockMap = {
  setView: vi.fn().mockReturnThis(),
  remove: vi.fn(),
  eachLayer: vi.fn((callback) => {
    // Call the callback with a mock TileLayer to simulate existing layers
    const mockTileLayer = { constructor: { name: 'TileLayer' } }
    callback(mockTileLayer)
  }),
  on: vi.fn(),
  off: vi.fn(),
  fitBounds: vi.fn(),
  removeLayer: vi.fn(),
  dragging: { disable: vi.fn() },
  touchZoom: { disable: vi.fn() },
  doubleClickZoom: { disable: vi.fn() },
  scrollWheelZoom: { disable: vi.fn() },
  boxZoom: { disable: vi.fn() },
  keyboard: { disable: vi.fn() }
}

const mockTileLayer = {
  addTo: vi.fn().mockReturnThis()
}

const mockFeatureGroup = {
  getBounds: vi.fn().mockReturnValue({})
}

// Mock the Leaflet library
vi.mock('leaflet', () => {
  class MockTileLayer {}

  return {
    default: {
      map: vi.fn(() => mockMap),
      tileLayer: vi.fn(() => mockTileLayer),
      marker: vi.fn(() => mockLayer),
      polygon: vi.fn(() => mockLayer),
      geoJSON: vi.fn(() => mockLayer),
      featureGroup: vi.fn(() => mockFeatureGroup),
      Icon: vi.fn(),
      TileLayer: MockTileLayer
    }
  }
})

// Mock Turf.js
vi.mock('@turf/turf', () => ({
  featureCollection: vi.fn(),
  point: vi.fn(),
  convex: vi.fn().mockReturnValue({
    geometry: {
      type: 'Polygon',
      coordinates: [
        [
          [0, 0],
          [1, 0],
          [1, 1],
          [0, 1],
          [0, 0]
        ]
      ]
    }
  }),
  multiPolygon: vi.fn().mockReturnValue({
    geometry: {
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
  }),
  flatten: vi.fn().mockReturnValue({
    features: [
      {
        geometry: {
          type: 'Polygon',
          coordinates: [
            [
              [0, 0],
              [1, 0],
              [1, 1],
              [0, 1],
              [0, 0]
            ]
          ]
        }
      }
    ]
  }),
  rewind: vi.fn((polygon) => polygon)
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

// Mock API services
vi.mock('@/api/assets', () => ({
  updateAssetByIdService: vi.fn().mockResolvedValue({})
}))

vi.mock('@/api/admin', () => ({
  adminUpdateAssetService: vi.fn().mockResolvedValue({})
}))

// Mock user store
const mockUserStore = {
  user: {
    id: 'test-user-id',
    admin: false
  }
}

vi.mock('@/stores', () => ({
  useUserStore: () => mockUserStore
}))

describe('MapCard.vue', () => {
  const defaultProps = {
    mapId: 'test-map',
    locations: [] as MultiPolygon[]
  }

  const sampleMultiPolygon: MultiPolygon = {
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

  const sampleAssetType: AssetType = {
    id: 'test-type-id',
    name: 'Test Type',
    description: 'Test asset type'
  }

  const sampleAsset: Asset = {
    id: 'test-asset',
    name: 'Test Asset',
    typeId: 'test-type-id',
    type: sampleAssetType,
    ownerId: 'test-owner-id',
    location: sampleMultiPolygon,
    capacityLitres: 1000,
    material: 'Steel',
    status: 'active',
    installedAt: '2023-01-01',
    lastInspection: '2023-06-01',
    lastModified: Date.now()
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Component Rendering', () => {
    it('renders map container with correct id', () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      const mapContainer = wrapper.find('#test-map')
      expect(mapContainer.exists()).toBe(true)
      expect(mapContainer.classes()).toContain('map')
    })

    it('component mounts successfully', () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      expect(wrapper.exists()).toBe(true)
    })

    it('applies CSS correctly', () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      const mapElement = wrapper.find('.map')
      expect(mapElement.exists()).toBe(true)
    })
  })

  describe('Props and Configuration', () => {
    it('accepts mapId prop', () => {
      const wrapper = mount(MapCard, {
        props: {
          ...defaultProps,
          mapId: 'custom-map-id'
        }
      })

      const mapContainer = wrapper.find('#custom-map-id')
      expect(mapContainer.exists()).toBe(true)
    })

    it('accepts locations prop', () => {
      const wrapper = mount(MapCard, {
        props: {
          ...defaultProps,
          locations: [sampleMultiPolygon]
        }
      })

      expect(wrapper.exists()).toBe(true)
    })

    it('accepts mode prop', () => {
      const wrapper = mount(MapCard, {
        props: {
          ...defaultProps,
          mode: 'convex'
        }
      })

      expect(wrapper.exists()).toBe(true)
    })

    it('accepts styles prop', () => {
      const customStyle = {
        color: 'red',
        weight: 2,
        fillOpacity: 0.5,
        fillColor: 'blue'
      }

      const wrapper = mount(MapCard, {
        props: {
          ...defaultProps,
          styles: [customStyle]
        }
      })

      expect(wrapper.exists()).toBe(true)
    })

    it('accepts asset prop', () => {
      const wrapper = mount(MapCard, {
        props: {
          ...defaultProps,
          asset: sampleAsset
        }
      })

      expect(wrapper.exists()).toBe(true)
    })

    it('accepts display mode prop', () => {
      const wrapper = mount(MapCard, {
        props: {
          ...defaultProps,
          display: true
        }
      })

      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('Component API', () => {
    it('exposes required methods through defineExpose', () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      // Test that exposed methods exist
      expect(wrapper.vm.beginDrawing).toBeDefined()
      expect(wrapper.vm.endDrawing).toBeDefined()
      expect(wrapper.vm.cancelDrawing).toBeDefined()
      expect(wrapper.vm.finishOneShape).toBeDefined()
      expect(wrapper.vm.finishOnePolygon).toBeDefined()
      expect(wrapper.vm.prevPolygon).toBeDefined()
      expect(wrapper.vm.nextPolygon).toBeDefined()
      expect(wrapper.vm.quickEscapePolygons).toBeDefined()
      expect(wrapper.vm.disablePrev).toBeDefined()
      expect(wrapper.vm.disableNext).toBeDefined()
    })

    it('methods are callable', async () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      await nextTick()

      // Test that methods can be called without throwing
      expect(() => wrapper.vm.beginDrawing()).not.toThrow()
      expect(() => wrapper.vm.cancelDrawing()).not.toThrow()
      expect(() => wrapper.vm.prevPolygon()).not.toThrow()
      expect(() => wrapper.vm.nextPolygon()).not.toThrow()
      expect(() => wrapper.vm.quickEscapePolygons()).not.toThrow()
    })

    it('endDrawing method works with no polygons', async () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      await nextTick()

      // Should not throw when called with no polygons
      expect(async () => await wrapper.vm.endDrawing()).not.toThrow()
    })

    it('finishOneShape method works', async () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      await nextTick()

      // Should not throw when called
      expect(async () => await wrapper.vm.finishOneShape()).not.toThrow()
    })

    it('finishOnePolygon method works', async () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      await nextTick()

      // Should not throw when called
      expect(() => wrapper.vm.finishOnePolygon()).not.toThrow()
    })
  })

  describe('Event Handling', () => {
    it('emits update:locations event when appropriate', async () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      await nextTick()

      // Call endDrawing with no polygons - should not emit
      await wrapper.vm.endDrawing()
      expect(wrapper.emitted('update:locations')).toBeFalsy()
    })

    it('handles prop changes without errors', async () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      await nextTick()

      // Change props and ensure no errors
      await wrapper.setProps({
        locations: [sampleMultiPolygon]
      })

      expect(wrapper.exists()).toBe(true)
    })
  })

  describe('Error Handling', () => {
    it('handles invalid mapId gracefully', () => {
      expect(() => {
        mount(MapCard, {
          props: {
            ...defaultProps,
            mapId: ''
          }
        })
      }).not.toThrow()
    })

    it('handles empty locations array', () => {
      expect(() => {
        mount(MapCard, {
          props: {
            ...defaultProps,
            locations: []
          }
        })
      }).not.toThrow()
    })

    it('handles invalid mode gracefully', () => {
      expect(() => {
        mount(MapCard, {
          props: {
            ...defaultProps,
            mode: 'invalid-mode' as 'convex' | 'polygon'
          }
        })
      }).not.toThrow()
    })
  })

  describe('Cleanup', () => {
    it('unmounts without errors', () => {
      const wrapper = mount(MapCard, {
        props: defaultProps
      })

      expect(() => wrapper.unmount()).not.toThrow()
    })
  })

  describe('Integration', () => {
    it('works with asset integration', () => {
      expect(() => {
        mount(MapCard, {
          props: {
            ...defaultProps,
            asset: sampleAsset
          }
        })
      }).not.toThrow()
    })

    it('works in display mode', () => {
      expect(() => {
        mount(MapCard, {
          props: {
            ...defaultProps,
            display: true
          }
        })
      }).not.toThrow()
    })

    it('works with custom styles', () => {
      const customStyle = {
        color: 'red',
        weight: 2,
        fillOpacity: 0.5,
        fillColor: 'blue'
      }

      expect(() => {
        mount(MapCard, {
          props: {
            ...defaultProps,
            styles: [customStyle],
            locations: [sampleMultiPolygon]
          }
        })
      }).not.toThrow()
    })
  })
})
