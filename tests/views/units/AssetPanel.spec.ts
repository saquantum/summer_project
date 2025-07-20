import { flushPromises, mount } from '@vue/test-utils'
import AssetPanel from '@/views/myassets/AssetPanel.vue'
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest'
import { config } from '@vue/test-utils'
config.global.config.warnHandler = () => {}

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
}))

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
        asset: { id: '9', name: 'Asset 8', typeId: 'B', location: {} },
        warnings: [{ warningLevel: 'YELLOW' }],
        maxWarning: { warningLevel: 'YELLOW' }
      }
    ],
    typeOptions: [
      { value: 'A', label: 'Type A' },
      { value: 'B', label: 'Type B' }
    ],
    getUserAssets: vi.fn()
  })
}))

// Mock MapCard/StatusIndicator/TestSearch/AddAsset
vi.mock('@/components/MapCard.vue', () => ({
  default: { name: 'MapCard', template: '<div class="mapcard-mock"></div>' }
}))
vi.mock('@/components/StatusIndicator.vue', () => ({
  default: {
    name: 'StatusIndicator',
    template: '<span class="status-mock"></span>'
  }
}))
vi.mock('@/views/myassets/AddAsset.vue', () => ({
  default: { name: 'AddAsset', template: '<div class="addasset-mock"></div>' }
}))

beforeAll(() => {
  vi.spyOn(console, 'warn').mockImplementation(() => {})
})

describe('AssetPanel.vue', () => {
  beforeEach(() => {
    pushMock.mockClear()
  })

  it('renders asset cards and legend', async () => {
    const wrapper = mount(AssetPanel)

    // asset card number
    expect(wrapper.findAll('.asset-card').length).toBe(8)
    // legend
    expect(wrapper.find('.legend')).toBeTruthy()
    // asset name
    expect(wrapper.text()).toContain('Asset 2')
    expect(wrapper.text()).toContain('Asset 3')
    expect(wrapper.text()).toContain('Asset 4')
    expect(wrapper.text()).toContain('Asset 5')
    expect(wrapper.text()).toContain('Asset 6')
    expect(wrapper.text()).toContain('Asset 7')
    expect(wrapper.text()).toContain('Asset 8')
    expect(wrapper.text()).not.toContain('Asset 1')
  })

  it('jump to asset 3 on buttton 1', async () => {
    const wrapper = mount(AssetPanel)
    const btns = wrapper.findAll('.view-details-btn')
    await btns[0].trigger('click')
    expect(pushMock).toHaveBeenCalledWith('/assets/3')
  })

  it('jump to asset 2 on buttton 2', async () => {
    const wrapper = mount(AssetPanel)
    const btns = wrapper.findAll('.view-details-btn')
    await btns[1].trigger('click')
    expect(pushMock).toHaveBeenCalledWith('/assets/2')
  })

  it('jump to asset 4 on buttton 3', async () => {
    const wrapper = mount(AssetPanel)
    const btns = wrapper.findAll('.view-details-btn')
    await btns[2].trigger('click')
    expect(pushMock).toHaveBeenCalledWith('/assets/4')
  })

  it('filter correctly by warning level', async () => {
    const wrapper = mount(AssetPanel)
    wrapper.vm.assetWarningLevel = 'NO'
    await flushPromises()
    // asset card number
    expect(wrapper.findAll('.asset-card').length).toBe(1)
    expect(wrapper.text()).toContain('Asset 1')
  })
})
