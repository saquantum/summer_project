import { render, screen } from '@testing-library/vue'
import { describe, it, vi, beforeEach, afterEach, expect } from 'vitest'
import BarChart from '@/components/charts/BarChart.vue'

// mock echarts
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

describe('BarChart.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    // cleanup DOM between tests
    document.body.innerHTML = ''
  })

  it('renders the title', () => {
    render(BarChart, {
      props: { id: 'test-chart', title: 'My Chart' }
    })

    expect(screen.getByText('My Chart')).toBeTruthy()
  })

  it('renders empty state when no data', () => {
    render(BarChart, {
      props: { id: 'test-chart', title: 'Empty Chart' }
    })

    expect(screen.getByText('No data available')).toBeTruthy()
  })

  it('initializes echarts on mount', async () => {
    render(BarChart, {
      props: {
        id: 'chart-1',
        title: 'Init Chart',
        data: { A: 10, B: 20 }
      }
    })

    // wait for nextTick + setTimeout
    await new Promise((resolve) => setTimeout(resolve, 150))

    expect(mockSetOption).toHaveBeenCalled()
  })

  it('updates chart when props.data changes', async () => {
    const { rerender } = render(BarChart, {
      props: {
        id: 'chart-2',
        title: 'Update Chart',
        data: { A: 10 }
      }
    })

    await new Promise((resolve) => setTimeout(resolve, 150))
    expect(mockSetOption).toHaveBeenCalledTimes(1)

    await rerender({
      id: 'chart-2',
      title: 'Update Chart',
      data: { A: 10, B: 20 }
    })

    expect(mockSetOption).toHaveBeenCalledTimes(2)
  })

  it('resizes chart on window resize', async () => {
    render(BarChart, {
      props: { id: 'chart-3', title: 'Resize Chart', data: { A: 5 } }
    })

    await new Promise((resolve) => setTimeout(resolve, 150))
    window.dispatchEvent(new Event('resize'))

    await new Promise((resolve) => setTimeout(resolve, 150))
    expect(mockResize).toHaveBeenCalled()
  })

  it('disposes chart on unmount', async () => {
    const { unmount } = render(BarChart, {
      props: { id: 'chart-4', title: 'Dispose Chart', data: { X: 42 } }
    })

    await new Promise((resolve) => setTimeout(resolve, 150))
    unmount()

    expect(mockDispose).toHaveBeenCalled()
  })
})
