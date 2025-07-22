import { flushPromises, mount } from '@vue/test-utils'
import AssetPanel from '@/views/myassets/AssetPanel.vue'
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick } from 'vue'
config.global.config.warnHandler = () => {}

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

// Mock getUserAssets method
const getUserAssetsMock = vi.fn()

// Mock stores
vi.mock('@/stores/index.ts', () => ({
  useUserStore: () => ({
    user: { id: 'testuser', admin: false }
  }),
  useAssetStore: () => ({
    userAssets: [
      {
        asset: { id: '1', name: 'Asset 1', typeId: 'A', location: {} },
        warnings: [],
        maxWarning: null
      },
      {
        asset: { id: '2', name: 'Asset 2', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'AMBER' }],
        maxWarning: { warningLevel: 'AMBER' }
      },
      {
        asset: { id: '3', name: 'Asset 3', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'RED' }],
        maxWarning: { warningLevel: 'RED' }
      },
      {
        asset: { id: '4', name: 'Asset 4', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      },
      {
        asset: { id: '5', name: 'Asset 5', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      },
      {
        asset: { id: '6', name: 'Asset 6', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      },
      {
        asset: { id: '7', name: 'Asset 7', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      },
      {
        asset: { id: '8', name: 'Asset 8', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      },
      {
        asset: { id: '9', name: 'Asset 9', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      }
    ],
    typeOptions: [
      { value: 'A', label: 'Type A' },
      { value: 'B', label: 'Type B' }
    ],
    getUserAssets: getUserAssetsMock
  })
}))

// Mock useResponsiveAction
vi.mock('@/composables/useResponsiveAction', () => ({
  useResponsiveAction: vi.fn()
}))

// Mock components
vi.mock('@/components/MapCard.vue', () => ({
  default: { name: 'MapCard', template: '<div class="mapcard-mock"></div>' }
}))
vi.mock('@/components/StatusIndicator.vue', () => ({
  default: {
    name: 'StatusIndicator',
    template: '<span class="status-mock"></span>'
  }
}))
vi.mock('@/components/TestSearch.vue', () => ({
  default: {
    name: 'TestSearch',
    template: '<div class="test-search-mock"><slot></slot></div>',
    emits: ['update:input', 'update:visible', 'search', 'clearFilters']
  }
}))
vi.mock('@/views/myassets/AddAsset.vue', () => ({
  default: { name: 'AddAsset', template: '<div class="addasset-mock"></div>' }
}))

beforeAll(() => {
  vi.spyOn(console, 'warn').mockImplementation(() => {})
  vi.spyOn(console, 'log').mockImplementation(() => {})
})

describe('AssetPanel.vue', () => {
  beforeEach(() => {
    pushMock.mockClear()
    getUserAssetsMock.mockClear()
  })

  it('renders the component correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Check basic structure
    expect(wrapper.find('.search-bar').exists()).toBe(true)
    expect(wrapper.find('.legend').exists()).toBe(true)
    expect(wrapper.find('.assets-container').exists()).toBe(true)
  })

  it('displays legend items correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()

    const legendItems = wrapper.findAll('.legend-item')
    expect(legendItems).toHaveLength(4)

    expect(wrapper.text()).toContain('No Warning')
    expect(wrapper.text()).toContain('Yellow Warning')
    expect(wrapper.text()).toContain('Amber Warning')
    expect(wrapper.text()).toContain('Red Warning')
  })

  it('renders asset cards correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Should show 8 cards (pageSize = 8, first page)
    expect(wrapper.findAll('.asset-card')).toHaveLength(8)
  })

  it('displays asset names correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Check that asset names are displayed (sorted by warning level)
    expect(wrapper.text()).toContain('Asset 3') // RED warning (first)
    expect(wrapper.text()).toContain('Asset 2') // AMBER warning (second)
    expect(wrapper.text()).toContain('Asset 4') // YELLOW warnings
    expect(wrapper.text()).toContain('Asset 5')
    expect(wrapper.text()).not.toContain('Asset 1') // NO warning (last, not on first page)
  })

  it('navigates to asset detail when button is clicked', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    const detailButtons = wrapper.findAll('.view-details-btn')
    expect(detailButtons.length).toBeGreaterThan(0)

    await detailButtons[0].trigger('click')
    expect(pushMock).toHaveBeenCalledWith('/assets/3') // First asset should be Asset 3 (RED warning)
  })

  it('filters assets by warning level - NO warning', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Set filter to NO warning
    wrapper.vm.assetWarningLevel = 'NO'
    await nextTick()
    await flushPromises()

    // Should only show Asset 1 (no warning)
    const currentAssets = wrapper.vm.currentAssets
    expect(currentAssets).toHaveLength(1)
    expect(currentAssets[0].asset.name).toBe('Asset 1')
  })

  it('filters assets by warning level - YELLOW warning', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Set filter to YELLOW warning
    wrapper.vm.assetWarningLevel = 'YELLOW'
    await nextTick()
    await flushPromises()

    // Should show all YELLOW warning assets (Asset 4,5,6,7,8,9)
    const currentAssets = wrapper.vm.currentAssets
    expect(currentAssets).toHaveLength(6)
    expect(
      currentAssets.every(
        (asset) => asset.maxWarning?.warningLevel === 'YELLOW'
      )
    ).toBe(true)
  })

  it('filters assets by asset type', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Set filter to type A
    wrapper.vm.assetType = 'A'
    await nextTick()
    await flushPromises()

    // Should only show Asset 1 (type A)
    const currentAssets = wrapper.vm.currentAssets
    expect(currentAssets).toHaveLength(1)
    expect(currentAssets[0].asset.typeId).toBe('A')
  })

  it('filters assets by name', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Set name filter
    wrapper.vm.assetName = 'Asset 2'
    await nextTick()
    await flushPromises()

    // Should only show Asset 2
    const currentAssets = wrapper.vm.currentAssets
    expect(currentAssets).toHaveLength(1)
    expect(currentAssets[0].asset.name).toBe('Asset 2')
  })

  it('combines multiple filters correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Set multiple filters: type B and YELLOW warning
    wrapper.vm.assetType = 'B'
    wrapper.vm.assetWarningLevel = 'YELLOW'
    await nextTick()
    await flushPromises()

    // Should show YELLOW warning assets with type B
    const currentAssets = wrapper.vm.currentAssets
    expect(currentAssets).toHaveLength(6)
    expect(
      currentAssets.every(
        (asset) =>
          asset.asset.typeId === 'B' &&
          asset.maxWarning?.warningLevel === 'YELLOW'
      )
    ).toBe(true)
  })

  it('clears all filters', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Set some filters
    wrapper.vm.assetName = 'Asset 1'
    wrapper.vm.assetType = 'A'
    wrapper.vm.assetWarningLevel = 'NO'
    await nextTick()

    // Clear filters
    wrapper.vm.assetName = ''
    wrapper.vm.assetType = ''
    wrapper.vm.assetWarningLevel = ''
    await nextTick()
    await flushPromises()

    // Should show all assets again
    expect(wrapper.vm.currentAssets).toHaveLength(9)
  })

  it('handles pagination correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Check initial page assets (first 8 assets)
    const currentPageAssets = wrapper.vm.currentPageAssets
    expect(currentPageAssets).toHaveLength(8) // pageSize = 8

    // First page should have 8 assets (sorted by warning level)
    expect(currentPageAssets).toBeDefined()
    if (currentPageAssets) {
      expect(currentPageAssets[0].asset.name).toBe('Asset 3') // RED warning first
      expect(currentPageAssets[1].asset.name).toBe('Asset 2') // AMBER warning second
    }
  })

  it('sorts assets by warning level correctly', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    const currentAssets = wrapper.vm.currentAssets
    expect(currentAssets).toHaveLength(9)

    // Check order: RED > AMBER > YELLOW > NO warning
    expect(currentAssets[0].maxWarning?.warningLevel).toBe('RED')
    expect(currentAssets[1].maxWarning?.warningLevel).toBe('AMBER')
    expect(currentAssets[2].maxWarning?.warningLevel).toBe('YELLOW')
    expect(currentAssets[8].maxWarning).toBeNull() // Asset with no warning should be last
  })

  it('displays assets correctly when store has data', async () => {
    // This test verifies that the component shows assets when the store has data
    // Note: The empty state logic in the component checks assetStore.userAssets
    // instead of currentAssets (filtered), which may be a design issue
    const wrapper = mount(AssetPanel)
    await nextTick()
    await flushPromises()

    // Should show assets since store has data
    expect(wrapper.findAll('.asset-card')).toHaveLength(8) // First page
    expect(wrapper.text()).toContain('Asset 3') // RED warning asset
    expect(wrapper.text()).toContain('Asset 2') // AMBER warning asset
    expect(wrapper.text()).not.toContain("You don't have any asset")
  })

  it('calls getUserAssets on mount', () => {
    mount(AssetPanel)
    expect(getUserAssetsMock).toHaveBeenCalled()
  })

  it('exposes the correct properties', async () => {
    const wrapper = mount(AssetPanel)
    await nextTick()

    // Check that exposed properties are accessible
    expect(wrapper.vm.assetWarningLevel).toBeDefined()
    expect(wrapper.vm.assetName).toBeDefined()
    expect(wrapper.vm.assetType).toBeDefined()
    expect(wrapper.vm.currentAssets).toBeDefined()
    expect(wrapper.vm.currentPageAssets).toBeDefined()
  })
})
