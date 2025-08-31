/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import AllAssets from '@/views/admin/AllAssets.vue'
import {
  adminDeleteAssetService,
  adminGetAssetsTotalService
} from '@/api/admin'

import { nextTick } from 'vue'

// Mock router
const pushMock = vi.fn()
vi.mock('vue-router', async (importOriginal) => {
  const actual = (await importOriginal()) as Record<string, unknown>
  return {
    ...actual,
    useRouter: () => ({ push: pushMock })
  }
})

// Mock the API services
vi.mock('@/api/admin', () => ({
  adminDeleteAssetService: vi.fn(),
  adminGetAssetsTotalService: vi.fn()
}))

// Mock Element Plus
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

// Mock child components
vi.mock('@/components/search/AssetSearch.vue', () => ({
  default: {
    name: 'AssetSearch',
    template: '<div data-testid="asset-search">AssetSearch</div>',
    props: ['fetchTableData', 'assetSearchBody'],
    emits: ['update:assetSearchBody']
  }
}))

vi.mock('@/components/search/SortTool.vue', () => ({
  default: {
    name: 'SortTool',
    template: '<div data-testid="sort-tool">SortTool</div>',
    props: ['multiSort', 'columns', 'fetchTableData'],
    emits: ['update:multiSort']
  }
}))

vi.mock('@/components/cards/AssetCard.vue', () => ({
  default: {
    name: 'AssetCard',
    template: '<div data-testid="asset-card">AssetCard</div>',
    props: ['item']
  }
}))

vi.mock('@/components/dialog/ConfirmDialog.vue', () => ({
  default: {
    name: 'ConfirmDialog',
    template:
      '<div data-testid="confirm-dialog" v-if="modelValue">Confirm<div><button data-test="confirm" @click="$emit(\'confirm\')">OK</button></div></div>',
    props: ['modelValue', 'title', 'content', 'countdownDuration'],
    emits: ['update:modelValue', 'confirm', 'cancel']
  }
}))

// Mock assets data
const mockAssets = [
  {
    asset: {
      id: 'asset1',
      name: 'Pump 1' as string,
      type: { name: 'Pump' },
      capacityLitres: 1000,
      material: 'Steel',
      status: 'Active',
      installedAt: '2020-01-01',
      lastInspection: '2024-01-01',
      ownerId: 'owner1'
    },
    warnings: [{ warningLevel: 'RED' }]
  },
  {
    asset: {
      id: 'asset2',
      name: 'Tank A' as string,
      type: { name: 'Tank' },
      capacityLitres: 5000,
      material: 'Concrete',
      status: 'Inactive',
      installedAt: '2019-05-10',
      lastInspection: '2023-06-01',
      ownerId: 'owner2'
    },
    warnings: [{ warningLevel: 'YELLOW' }]
  }
]

// Mock stores
const mockAssetStore = {
  allAssets: mockAssets,
  getAllAssets: vi.fn().mockResolvedValue(undefined)
}

// Mock useAssetStore
vi.mock('@/stores', () => ({
  useAssetStore: vi.fn(() => mockAssetStore)
}))

describe('AllAssets', () => {
  beforeEach(() => {
    vi.mocked(adminGetAssetsTotalService).mockResolvedValue({
      data: 2
    } as any)

    vi.mocked(adminDeleteAssetService).mockResolvedValue({
      data: undefined
    } as any)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly', async () => {
    const wrapper = mount(AllAssets, { attachTo: document.body })

    await nextTick()

    expect(wrapper.find('[data-testid="asset-search"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="sort-tool"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="asset-card"]').exists()).toBe(true)
  })

  it('computes assets table correctly', async () => {
    const wrapper = mount(AllAssets, { attachTo: document.body })
    await nextTick()

    const assets = wrapper.vm.assets
    expect(assets).toHaveLength(2)

    expect(assets[0]).toEqual({
      id: 'asset1',
      name: 'Pump 1',
      type: 'Pump',
      capacityLitres: 1000,
      material: 'Steel',
      status: 'Active',
      installedAt: '2020-01-01',
      lastInspection: '2024-01-01',
      assetHolderId: 'owner1',
      warningLevel: 'RED'
    })

    expect(assets[1]).toEqual({
      id: 'asset2',
      name: 'Tank A',
      type: 'Tank',
      capacityLitres: 5000,
      material: 'Concrete',
      status: 'Inactive',
      installedAt: '2019-05-10',
      lastInspection: '2023-06-01',
      assetHolderId: 'owner2',
      warningLevel: 'YELLOW'
    })
  })

  it('navigates to detail when show detail is called', async () => {
    const wrapper = mount(AllAssets, { attachTo: document.body })
    await nextTick()

    wrapper.vm.handleShowDetail(wrapper.vm.assets[0])

    expect(pushMock).toHaveBeenCalledWith(`/assets/asset1`)
  })

  it('deletes an asset', async () => {
    vi.useFakeTimers()
    const deleteSpy = vi
      .mocked(adminDeleteAssetService)
      .mockResolvedValue({} as any)

    const wrapper = mount(AllAssets, { attachTo: document.body })
    await nextTick()
    await nextTick()

    // Trigger delete for first asset
    wrapper.vm.triggerDelete([wrapper.vm.assets[0]])
    await nextTick()

    // Advance timer to complete countdown
    vi.advanceTimersByTime(5000)
    await nextTick()

    // Verify dialog state
    const confirmDialog = wrapper.findComponent({ name: 'ConfirmDialog' })
    expect(confirmDialog.isVisible()).toBe(true)

    // Find confirm button within the component
    const confirmButton = confirmDialog.find('[data-test="confirm"]')
    expect(confirmButton.exists()).toBe(true)

    // Click confirm button
    await confirmButton.trigger('click')

    // Wait for async operations to complete
    await flushPromises()

    await nextTick()
    await nextTick()

    // Verify results
    expect(deleteSpy).toHaveBeenCalledWith(['asset1'])
    expect(mockAssetStore.getAllAssets).toHaveBeenCalled()
    expect(confirmDialog.props('modelValue')).toBe(false)

    vi.useRealTimers()
  })

  it('handles API errors gracefully', async () => {
    const consoleErrorSpy = vi
      .spyOn(console, 'error')
      .mockImplementation(() => {})
    vi.mocked(adminGetAssetsTotalService).mockRejectedValueOnce(
      new Error('API Error')
    )

    mount(AllAssets, { attachTo: document.body })
    await nextTick()

    // Wait for the async operation to complete
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(ElMessage.error).toHaveBeenCalledWith('Failed to fetch table data')
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Fail to load assets:',
      expect.any(Error)
    )

    consoleErrorSpy.mockRestore()
  })

  it('handles null type and warning correctly', async () => {
    const mockAssetsWithNull: typeof mockAssets = [
      {
        asset: {
          id: 'asset3',
          name: 'Unknown' as string,
          type: null as unknown as { name: string },
          capacityLitres: 0,
          material: '',
          status: '',
          installedAt: '',
          lastInspection: '',
          ownerId: ''
        },
        warnings: []
      }
    ]

    Object.assign(mockAssetStore, { allAssets: mockAssetsWithNull })

    const wrapper = mount(AllAssets, { attachTo: document.body })
    await nextTick()

    const assets = wrapper.vm.assets
    expect(assets[0].type).toBe('NULL')
    expect(assets[0].warningLevel).toBe('NULL')
  })
})
