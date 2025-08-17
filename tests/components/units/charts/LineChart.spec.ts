import { render, screen } from '@testing-library/vue'
import { describe, it, vi, beforeEach, afterEach, expect } from 'vitest'
import LineChart from '@/components/charts/LineChart.vue'

// Mock vue-countup-v3
vi.mock('vue-countup-v3', () => ({
  default: {
    name: 'ICountUp',
    props: ['endVal'],
    template: '<span data-testid="count">{{ endVal }}</span>'
  }
}))

// 🔹 Mock echarts
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

describe('LineChart.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  it('renders the title and count', () => {
    render(LineChart, {
      props: { id: 'line1', title: 'Revenue', count: 123 }
    })

    expect(screen.getByText('Revenue')).toBeTruthy()
    expect(screen.getByTestId('count').textContent).toBe('123')
  })

  it('initializes echarts on mount', () => {
    render(LineChart, {
      props: { id: 'line2', title: 'Users', count: 50 }
    })

    expect(mockSetOption).toHaveBeenCalled()
  })

  it('resizes chart when window resizes', () => {
    render(LineChart, {
      props: { id: 'line3', title: 'Active Users', count: 77 }
    })

    window.dispatchEvent(new Event('resize'))
    expect(mockResize).toHaveBeenCalled()
  })

  it('disposes chart on unmount', () => {
    const { unmount } = render(LineChart, {
      props: { id: 'line4', title: 'Disposed Chart', count: 999 }
    })

    unmount()
    expect(mockDispose).toHaveBeenCalled()
  })
})
