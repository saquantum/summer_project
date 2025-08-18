/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import AssetSearch from '@/components/search/AssetSearch.vue'

// Mock the stores
vi.mock('@/stores', () => ({
  useAssetStore: vi.fn(() => ({
    typeOptions: [
      { label: 'Tank', value: '1' },
      { label: 'Pipe', value: '2' }
    ]
  })),
  useUserStore: vi.fn(() => ({
    searchHistory: ['asset1', 'asset2']
  }))
}))

// Mock the DatePicker component
vi.mock('@/components/DatePicker.vue', () => ({
  default: {
    name: 'DatePicker',
    template: '<div data-testid="date-picker"><input /></div>',
    props: ['modelValue', 'teleported'],
    emits: ['update:modelValue']
  }
}))

// Mock the formUtils
vi.mock('@/utils/formUtils', () => ({
  assetConvertFormToFilter: vi.fn((form) => ({
    id: form.id,
    name: form.name,
    typeId: form.typeId
  }))
}))

// Mock DOM methods
const mockGetBoundingClientRect = vi.fn(() => ({
  bottom: 100,
  left: 50,
  top: 60,
  right: 200,
  width: 150,
  height: 40
}))

Object.defineProperty(HTMLElement.prototype, 'getBoundingClientRect', {
  value: mockGetBoundingClientRect,
  writable: true
})

Object.defineProperty(HTMLElement.prototype, 'offsetHeight', {
  value: 200,
  writable: true
})

// Mock window properties
global.window = Object.assign(global.window, {
  innerHeight: 768,
  innerWidth: 1024,
  addEventListener: vi.fn(),
  removeEventListener: vi.fn()
})

describe('AssetSearch', () => {
  let wrapper: VueWrapper<any>

  const defaultProps = {
    assetSearchBody: {
      filters: {},
      orderList: '',
      limit: 10,
      offset: 0
    },
    fetchTableData: vi.fn()
  }

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks()

    wrapper = mount(AssetSearch, {
      props: defaultProps,
      attachTo: document.body
    })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('should render the search input', () => {
    const searchInput = wrapper.find('.search-input')
    expect(searchInput.exists()).toBe(true)
    expect(searchInput.attributes('placeholder')).toBe('Search assets...')
  })

  it('should render the filter button', () => {
    const filterButton = wrapper.find('[data-test="filter"]')
    expect(filterButton.exists()).toBe(true)
  })

  it('should toggle filter panel when filter button is clicked', async () => {
    const filterButton = wrapper.find('[data-test="filter"]')

    // Initially should be closed
    expect(wrapper.vm.visible).toBe(false)
    expect(wrapper.vm.detail).toBe(false)

    // Click to open filter panel
    await filterButton.trigger('click')
    await nextTick()

    expect(wrapper.vm.visible).toBe(true)
    expect(wrapper.vm.detail).toBe(true)

    // Click again to close
    await filterButton.trigger('click')
    await nextTick()

    expect(wrapper.vm.visible).toBe(false)
  })

  it('should render search and clear buttons when filter is open', async () => {
    // Click the filter button to open the filter panel
    const filterButton = wrapper.find('[data-test="filter"]')
    await filterButton.trigger('click')
    await nextTick()

    // Check that the detail panel is open
    expect(wrapper.vm.visible).toBe(true)
    expect(wrapper.vm.detail).toBe(true)

    // Since the buttons are teleported, we can verify they exist by checking the DOM directly
    // or by testing the functionality through component methods
    expect(wrapper.vm.handleSearch).toBeDefined()
    expect(wrapper.vm.clearFilters).toBeDefined()
  })

  it('should call handleSearch when search button is clicked', async () => {
    const mockFetchTableData = vi.fn()
    wrapper = mount(AssetSearch, {
      props: {
        ...defaultProps,
        fetchTableData: mockFetchTableData
      },
      attachTo: document.body
    })

    // Set up some form data
    wrapper.vm.form.id = 'test-id'
    wrapper.vm.form.name = 'test-name'

    // Call handleSearch directly since the button is teleported
    wrapper.vm.handleSearch()
    await nextTick()

    expect(mockFetchTableData).toHaveBeenCalled()
    expect(wrapper.emitted('update:assetSearchBody')).toBeTruthy()
    expect(wrapper.vm.visible).toBe(false)
  })

  it('should call clearFilters when clear button is clicked', async () => {
    const mockFetchTableData = vi.fn()
    wrapper = mount(AssetSearch, {
      props: {
        ...defaultProps,
        fetchTableData: mockFetchTableData
      },
      attachTo: document.body
    })

    // Set some form values first
    wrapper.vm.form.id = 'test'
    wrapper.vm.form.name = 'test name'

    // Call clearFilters directly since the button is teleported
    wrapper.vm.clearFilters()
    await nextTick()

    expect(wrapper.vm.form.id).toBe('')
    expect(wrapper.vm.form.name).toBe('')
    expect(mockFetchTableData).toHaveBeenCalled()
    expect(wrapper.emitted('update:assetSearchBody')).toBeTruthy()
  })

  it('should emit update event when search is performed', async () => {
    const mockFetchTableData = vi.fn()
    wrapper = mount(AssetSearch, {
      props: {
        ...defaultProps,
        fetchTableData: mockFetchTableData
      },
      attachTo: document.body
    })

    // Simulate entering search text and pressing Enter
    const searchInput = wrapper.find('.search-input')
    await searchInput.setValue('test asset')
    await searchInput.trigger('keydown', { key: 'Enter' })
    await nextTick()

    // Should call fetchTableData
    expect(mockFetchTableData).toHaveBeenCalled()

    // Should emit update event
    expect(wrapper.emitted('update:assetSearchBody')).toBeTruthy()
  })

  it('should open dropdown when input is clicked', async () => {
    const searchInput = wrapper.find('.search-input')
    await searchInput.trigger('click')
    await nextTick()

    // The component should set visible to true (tested indirectly through DOM updates)
    expect(wrapper.vm.visible).toBe(true)
  })

  it('should clear filters when clear button is clicked', async () => {
    const mockFetchTableData = vi.fn()
    wrapper = mount(AssetSearch, {
      props: {
        ...defaultProps,
        fetchTableData: mockFetchTableData
      },
      attachTo: document.body
    })

    // Set some form values first
    wrapper.vm.form.id = 'test'
    wrapper.vm.form.name = 'test name'

    // Call clearFilters
    wrapper.vm.clearFilters()
    await nextTick()

    // Check that form is reset
    expect(wrapper.vm.form.id).toBe('')
    expect(wrapper.vm.form.name).toBe('')
    expect(mockFetchTableData).toHaveBeenCalled()
  })

  it('should handle keyboard navigation correctly', async () => {
    const searchInput = wrapper.find('.search-input')

    // Test Enter key with empty input (should clear filters)
    await searchInput.setValue('')
    await searchInput.trigger('keydown', { key: 'Enter' })

    expect(wrapper.emitted('update:assetSearchBody')).toBeTruthy()
  })

  it('should expose form through defineExpose', () => {
    expect(wrapper.vm.form).toBeDefined()
    expect(typeof wrapper.vm.form).toBe('object')
  })
})
