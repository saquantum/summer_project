import { render, screen } from '@testing-library/vue'
import { describe, it, vi, beforeEach, afterEach, expect } from 'vitest'
import PieChart from '@/components/charts/PieChart.vue'

// Mock echarts
const mockSetOption = vi.fn()
const mockResize = vi.fn()
const mockDispose = vi.fn()

vi.mock('echarts', () => ({
  init: vi.fn(() => ({
    setOption: mockSetOption,
    resize: mockResize,
    dispose: mockDispose
  }))
}))

describe('PieChart.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders the title', () => {
    render(PieChart, {
      props: { id: 'pie1', title: 'Regional Data' }
    })

    expect(screen.getByText('Regional Data')).toBeTruthy()
  })

  it('renders empty state when no data', () => {
    render(PieChart, {
      props: { id: 'pie2', title: 'Empty Pie' }
    })

    expect(screen.getByText('No data available')).toBeTruthy()
  })

  it('initializes echarts on mount', async () => {
    render(PieChart, {
      props: { id: 'pie3', title: 'Pie Chart', data: { A: 10, B: 20 } }
    })

    // wait for nextTick + setTimeout in initChart
    await new Promise((resolve) => setTimeout(resolve, 150))
    expect(mockSetOption).toHaveBeenCalled()
  })

  it('updates chart when props.data changes', async () => {
    const { rerender } = render(PieChart, {
      props: { id: 'pie4', title: 'Update Pie', data: { A: 5 } }
    })

    await new Promise((resolve) => setTimeout(resolve, 150))
    expect(mockSetOption).toHaveBeenCalledTimes(1)

    await rerender({
      id: 'pie4',
      title: 'Update Pie',
      data: { A: 5, B: 15 }
    })

    expect(mockSetOption).toHaveBeenCalledTimes(2)
  })

  it('resizes chart on window resize', async () => {
    render(PieChart, {
      props: { id: 'pie5', title: 'Resize Pie', data: { A: 10 } }
    })

    await new Promise((resolve) => setTimeout(resolve, 150))
    window.dispatchEvent(new Event('resize'))

    await new Promise((resolve) => setTimeout(resolve, 150))
    expect(mockResize).toHaveBeenCalled()
  })

  it('disposes chart on unmount', async () => {
    const { unmount } = render(PieChart, {
      props: { id: 'pie6', title: 'Dispose Pie', data: { X: 42 } }
    })

    await new Promise((resolve) => setTimeout(resolve, 150))
    unmount()

    expect(mockDispose).toHaveBeenCalled()
  })
})
