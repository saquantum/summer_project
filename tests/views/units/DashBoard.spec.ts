/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import DashBoard from '@/views/admin/DashBoard.vue'
import {
  adminGetAssetDistributionService,
  adminGetAssetsTotalService,
  adminGetContactPreferenceService,
  adminGetMetaDateService,
  adminGetUserDistributionService
} from '@/api/admin'

// Mock the API services
vi.mock('@/api/admin', () => ({
  adminGetAssetDistributionService: vi.fn(),
  adminGetAssetsTotalService: vi.fn(),
  adminGetContactPreferenceService: vi.fn(),
  adminGetMetaDateService: vi.fn(),
  adminGetUserDistributionService: vi.fn()
}))

// Mock chart components
vi.mock('@/components/charts/LineChart.vue', () => ({
  default: {
    name: 'LineChart',
    template: `
      <div data-testid="line-chart" :data-count="count" :data-id="id" :data-title="title">
        LineChart: {{ title }}
      </div>
    `,
    props: ['count', 'id', 'title', 'color']
  }
}))

vi.mock('@/components/charts/BarChart.vue', () => ({
  default: {
    name: 'BarChart',
    template: `
      <div data-testid="bar-chart" :data-id="id" :data-title="title">
        BarChart: {{ title }}
      </div>
    `,
    props: ['id', 'title', 'data']
  }
}))

vi.mock('@/components/charts/PieChart.vue', () => ({
  default: {
    name: 'PieChart',
    template: `
      <div data-testid="pie-chart" :data-id="id" :data-title="title">
        PieChart: {{ title }}
      </div>
    `,
    props: ['id', 'title', 'data', 'chartType']
  }
}))

vi.mock('@/components/charts/MapChart.vue', () => ({
  default: {
    name: 'MapChart',
    template: `
      <div data-testid="map-chart" :data-id="id" :data-title="title">
        MapChart: {{ title }}
      </div>
    `,
    props: ['id', 'title', 'data']
  }
}))

// Mock stores
const mockWarningStore = {
  getAllLiveWarnings: vi.fn().mockResolvedValue(undefined),
  liveWarnings: []
}

vi.mock('@/stores', () => ({
  useWarningStore: vi.fn(() => mockWarningStore)
}))

// Mock data for API responses
const mockMetaData = {
  data: [
    { tableName: 'users', totalCount: 150 },
    { tableName: 'assets', totalCount: 300 }
  ]
}

const mockUserDistribution = {
  data: {
    London: 50,
    Manchester: 30,
    Birmingham: 25,
    Liverpool: 20,
    Bristol: 15,
    Leeds: 10
  }
}

const mockContactPreference = {
  data: {
    Email: 80,
    SMS: 45,
    Phone: 25
  }
}

const mockAssetDistribution = {
  data: {
    London: 120,
    Manchester: 80,
    Birmingham: 60,
    Liverpool: 40
  }
}

const mockAssetsInDanger = {
  data: 25
}

describe('DashBoard', () => {
  beforeEach(() => {
    // Setup API mocks with default successful responses
    vi.mocked(adminGetMetaDateService).mockResolvedValue(mockMetaData as any)
    vi.mocked(adminGetUserDistributionService).mockResolvedValue(
      mockUserDistribution as any
    )
    vi.mocked(adminGetContactPreferenceService).mockResolvedValue(
      mockContactPreference as any
    )
    vi.mocked(adminGetAssetDistributionService).mockResolvedValue(
      mockAssetDistribution as any
    )
    vi.mocked(adminGetAssetsTotalService).mockResolvedValue(
      mockAssetsInDanger as any
    )
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with all chart components', async () => {
    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    // Check that all chart components are rendered
    const lineCharts = wrapper.findAllComponents({ name: 'LineChart' })
    expect(lineCharts).toHaveLength(4)

    expect(wrapper.findComponent({ name: 'BarChart' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'PieChart' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'MapChart' }).exists()).toBe(true)
  })

  it('displays correct chart titles', async () => {
    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    const lineCharts = wrapper.findAll('[data-testid="line-chart"]')
    expect(lineCharts[0].attributes('data-title')).toBe('User Statistics')
    expect(lineCharts[1].attributes('data-title')).toBe('Asset Statistics')
    expect(lineCharts[2].attributes('data-title')).toBe('Asset Statistics')
    expect(lineCharts[3].attributes('data-title')).toBe('Assets in danger')

    expect(
      wrapper.find('[data-testid="bar-chart"]').attributes('data-title')
    ).toBe('Contact preference')
    expect(
      wrapper.find('[data-testid="pie-chart"]').attributes('data-title')
    ).toBe('User Distribution')
    expect(
      wrapper.find('[data-testid="map-chart"]').attributes('data-title')
    ).toBe('Asset Distribution Map')
  })

  it('fetches and displays dashboard data correctly', async () => {
    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    // Verify API calls were made
    expect(adminGetMetaDateService).toHaveBeenCalled()
    expect(adminGetUserDistributionService).toHaveBeenCalled()
    expect(adminGetContactPreferenceService).toHaveBeenCalled()
    expect(adminGetAssetDistributionService).toHaveBeenCalled()
    expect(adminGetAssetsTotalService).toHaveBeenCalledWith({
      filters: { warning_id: { op: 'notNull' } }
    })

    // Verify warning store method was called
    expect(mockWarningStore.getAllLiveWarnings).toHaveBeenCalled()

    // Check that data is correctly passed to chart components through props
    const lineCharts = wrapper.findAllComponents({ name: 'LineChart' })
    expect(lineCharts[0].props('count')).toBe(150) // userCount
    expect(lineCharts[1].props('count')).toBe(300) // assetCount
    expect(lineCharts[3].props('count')).toBe(25) // assetsInDanger

    const barChart = wrapper.findComponent({ name: 'BarChart' })
    expect(barChart.props('data')).toEqual(mockContactPreference.data)

    const mapChart = wrapper.findComponent({ name: 'MapChart' })
    expect(mapChart.props('data')).toEqual(mockAssetDistribution.data)
  })

  it('computes pieChartData correctly with top 5 regions', async () => {
    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    const pieChart = wrapper.findComponent({ name: 'PieChart' })
    const pieChartData = pieChart.props('data')

    // Should have top 5 regions plus "Others"
    expect(Object.keys(pieChartData)).toEqual([
      'London',
      'Manchester',
      'Birmingham',
      'Liverpool',
      'Bristol',
      'Others'
    ])

    // Check values
    expect(pieChartData.London).toBe(50)
    expect(pieChartData.Manchester).toBe(30)
    expect(pieChartData.Birmingham).toBe(25)
    expect(pieChartData.Liverpool).toBe(20)
    expect(pieChartData.Bristol).toBe(15)
    expect(pieChartData.Others).toBe(10) // Leeds value
  })

  it('handles pieChartData when there are 5 or fewer regions', async () => {
    // Mock with only 3 regions
    const limitedUserDistribution = {
      data: {
        London: 50,
        Manchester: 30,
        Birmingham: 25
      }
    }

    vi.mocked(adminGetUserDistributionService).mockResolvedValue(
      limitedUserDistribution as any
    )

    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    const pieChart = wrapper.findComponent({ name: 'PieChart' })
    const pieChartData = pieChart.props('data')

    // Should not have "Others" category
    expect(Object.keys(pieChartData)).toEqual([
      'London',
      'Manchester',
      'Birmingham'
    ])
    expect(pieChartData.Others).toBeUndefined()
  })

  it('handles API errors gracefully', async () => {
    const consoleErrorSpy = vi
      .spyOn(console, 'error')
      .mockImplementation(() => {})

    // Mock API failure
    vi.mocked(adminGetMetaDateService).mockRejectedValueOnce(
      new Error('API Error')
    )

    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      'Failed to fetch dashboard data:',
      expect.any(Error)
    )

    // Component should still render with default values
    const lineCharts = wrapper.findAllComponents({ name: 'LineChart' })
    expect(lineCharts[0].props('count')).toBe(0) // userCount should be 0
    expect(lineCharts[1].props('count')).toBe(0) // assetCount should be 0
    expect(lineCharts[3].props('count')).toBe(0) // assetsInDanger should be 0

    consoleErrorSpy.mockRestore()
  })

  it('handles partial API response data correctly', async () => {
    // Mock partial responses
    vi.mocked(adminGetUserDistributionService).mockResolvedValue({
      data: null
    } as any)
    vi.mocked(adminGetContactPreferenceService).mockResolvedValue({
      data: undefined
    } as any)

    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    // Should handle null/undefined data gracefully
    const barChart = wrapper.findComponent({ name: 'BarChart' })
    expect(barChart.props('data')).toEqual({})

    const pieChart = wrapper.findComponent({ name: 'PieChart' })
    expect(pieChart.props('data')).toEqual({})

    // Other data should still be set
    const mapChart = wrapper.findComponent({ name: 'MapChart' })
    expect(mapChart.props('data')).toEqual(mockAssetDistribution.data)
  })

  it('handles missing table data in meta response', async () => {
    // Mock meta data without user/asset tables
    const incompleteMeta = {
      data: [{ tableName: 'other', totalCount: 100 }]
    }

    vi.mocked(adminGetMetaDateService).mockResolvedValue(incompleteMeta as any)

    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    // Should default to 0 when table data is not found
    const lineCharts = wrapper.findAllComponents({ name: 'LineChart' })
    expect(lineCharts[0].props('count')).toBe(0) // userCount
    expect(lineCharts[1].props('count')).toBe(0) // assetCount
  })

  it('passes correct props to chart components', async () => {
    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    // Check LineChart props
    const lineCharts = wrapper.findAllComponents({ name: 'LineChart' })
    expect(lineCharts[0].props()).toMatchObject({
      count: 150,
      id: 'user',
      title: 'User Statistics'
    })
    expect(lineCharts[3].props()).toMatchObject({
      count: 25,
      id: 'asset-in-danger',
      title: 'Assets in danger',
      color: 'orange'
    })

    // Check BarChart props
    const barChart = wrapper.findComponent({ name: 'BarChart' })
    expect(barChart.props()).toMatchObject({
      id: 'contact-preference',
      title: 'Contact preference',
      data: mockContactPreference.data
    })

    // Check PieChart props
    const pieChart = wrapper.findComponent({ name: 'PieChart' })
    expect(pieChart.props()).toMatchObject({
      id: 'regional-pie-chart',
      title: 'User Distribution',
      chartType: 'normal'
    })

    // Check MapChart props
    const mapChart = wrapper.findComponent({ name: 'MapChart' })
    expect(mapChart.props()).toMatchObject({
      id: 'asset-distribution-map',
      title: 'Asset Distribution Map',
      data: mockAssetDistribution.data
    })
  })

  it('renders proper layout structure', async () => {
    const wrapper = mount(DashBoard, { attachTo: document.body })

    await flushPromises()
    await nextTick()

    // Check main layout containers
    expect(wrapper.find('.stats-container').exists()).toBe(true)
    expect(wrapper.find('.stats-cards').exists()).toBe(true)
    expect(wrapper.find('.map-dashboard-container').exists()).toBe(true)
    expect(wrapper.find('.map-section').exists()).toBe(true)
    expect(wrapper.find('.dashboard-section').exists()).toBe(true)
  })
})
