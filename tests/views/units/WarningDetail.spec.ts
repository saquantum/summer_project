import { mount } from '@vue/test-utils'
import WarningDetail from '@/views/warning/index.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import type { Warning } from '@/types'

// Mock router
const mockRoute = {
  params: { id: '123' }
}

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute
}))

// Mock warning store
const mockWarningStore = {
  allWarnings: [] as Warning[],
  getAllWarnings: vi.fn()
}

vi.mock('@/stores', () => ({
  useWarningStore: () => mockWarningStore
}))

// Mock MapCard component
vi.mock('@/components/MapCard.vue', () => ({
  default: {
    name: 'MapCard',
    props: ['mapId', 'locations', 'style'],
    template: '<div class="map-card-mock">Map Card</div>'
  }
}))

// Mock Element Plus - partial mock approach
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElDescriptions: {
      name: 'ElDescriptions',
      props: ['title', 'column', 'direction'],
      template: '<div class="el-descriptions"><slot /></div>'
    },
    ElDescriptionsItem: {
      name: 'ElDescriptionsItem',
      props: ['label'],
      template:
        '<div class="el-descriptions-item"><span class="label">{{ label }}:</span> <span class="value"><slot /></span></div>'
    }
  }
})

describe('WarningDetail', () => {
  const mockWarning: Warning = {
    id: 123,
    weatherType: 'Rain',
    warningLevel: 'Yellow',
    warningHeadLine: 'Heavy rain expected',
    validFrom: 1640995200, // 2022-01-01 00:00:00 UTC
    validTo: 1641081600, // 2022-01-02 00:00:00 UTC
    warningImpact: 'Flooding of some roads',
    warningLikelihood: 'Likely',
    affectedAreas: 'London\\nSurrey\\nKent',
    whatToExpect: 'Heavy rain and flooding',
    warningFurtherDetails: 'Please be careful\\nAvoid unnecessary travel',
    warningUpdateDescription: 'Updated with latest forecast',
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
  }

  const createWrapper = () => {
    return mount(WarningDetail)
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockWarningStore.allWarnings = []
    mockRoute.params.id = '123'
  })

  it('renders without crashing when no warning is found', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    expect(wrapper.exists()).toBe(true)
  })

  it('displays warning details when warning is found', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const descriptions = wrapper.findAll('.el-descriptions-item')
    expect(descriptions.length).toBeGreaterThan(0)

    // Check if warning details are displayed
    const text = wrapper.text()
    expect(text).toContain('123')
    expect(text).toContain('Rain')
    expect(text).toContain('Yellow')
    expect(text).toContain('Heavy rain expected')
  })

  it('renders MapCard with correct props when warning exists', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    expect(mapCard.exists()).toBe(true)
    expect(mapCard.props('mapId')).toBe('map-123')
    expect(mapCard.props('locations')).toEqual([mockWarning.area])
    expect(mapCard.props('style')).toBeDefined()
  })

  it('sets correct style for yellow warning level', async () => {
    const yellowWarning = { ...mockWarning, warningLevel: 'Yellow' }
    mockWarningStore.allWarnings = [yellowWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    const style = mapCard.props('style')
    expect(style.color).toBe('#cc9900')
    expect(style.fillColor).toBe('#ffff00')
    expect(style.weight).toBe(2)
    expect(style.fillOpacity).toBe(0.4)
  })

  it('sets correct style for amber warning level', async () => {
    const amberWarning = { ...mockWarning, warningLevel: 'Amber' }
    mockWarningStore.allWarnings = [amberWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    const style = mapCard.props('style')
    expect(style.color).toBe('#cc6600')
    expect(style.fillColor).toBe('#ffcc00')
  })

  it('sets correct style for red warning level', async () => {
    const redWarning = { ...mockWarning, warningLevel: 'Red' }
    mockWarningStore.allWarnings = [redWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    const style = mapCard.props('style')
    expect(style.color).toBe('#800000')
    expect(style.fillColor).toBe('#ff0000')
  })

  it('handles case-insensitive warning levels', async () => {
    const upperCaseWarning = { ...mockWarning, warningLevel: 'YELLOW' }
    mockWarningStore.allWarnings = [upperCaseWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    const style = mapCard.props('style')
    expect(style.color).toBe('#cc9900')
    expect(style.fillColor).toBe('#ffff00')
  })

  it('formats dates correctly in display data', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const text = wrapper.text()
    // Should contain formatted dates (exact format may vary by locale)
    expect(text).toMatch(/Valid From.*2022/)
    expect(text).toMatch(/Valid To.*2022/)
  })

  it('replaces newline characters in multiline text fields', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    const wrapper = createWrapper()
    await flushPromises()

    // Check for multiline text elements
    const multilineElements = wrapper.findAll('.multiline-text')
    expect(multilineElements.length).toBeGreaterThan(0)

    // The actual newlines should be preserved (\\n should become \n)
    const text = wrapper.text()
    expect(text).toContain('London')
    expect(text).toContain('Surrey')
    expect(text).toContain('Kent')
  })

  it('displays all warning data fields', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const text = wrapper.text()
    expect(text).toContain('Warning ID')
    expect(text).toContain('Weather Type')
    expect(text).toContain('Warning Level')
    expect(text).toContain('Warning HeadLine')
    expect(text).toContain('Valid From')
    expect(text).toContain('Valid To')
    expect(text).toContain('Warning Impact')
    expect(text).toContain('Warning Likelihood')
    expect(text).toContain('Affected Areas')
    expect(text).toContain('Further Details')
    expect(text).toContain('Update Description')
  })

  it('calls getAllWarnings when store is empty on mount', async () => {
    mockWarningStore.allWarnings = []
    createWrapper()
    await flushPromises()

    expect(mockWarningStore.getAllWarnings).toHaveBeenCalled()
  })

  it('does not call getAllWarnings when store already has warnings', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    createWrapper()
    await flushPromises()

    expect(mockWarningStore.getAllWarnings).not.toHaveBeenCalled()
  })

  it('handles different route parameters', async () => {
    mockRoute.params.id = '456'
    const anotherWarning = { ...mockWarning, id: 456 }
    mockWarningStore.allWarnings = [mockWarning, anotherWarning]

    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    expect(mapCard.props('mapId')).toBe('map-456')
  })

  it('handles missing warning gracefully', async () => {
    mockRoute.params.id = '999'
    mockWarningStore.allWarnings = [mockWarning]

    const wrapper = createWrapper()
    await flushPromises()

    // Should render without errors even when warning is not found
    expect(wrapper.exists()).toBe(true)
    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    expect(mapCard.props('locations')).toEqual([undefined])
  })

  it('applies multiline-text class to appropriate fields', async () => {
    mockWarningStore.allWarnings = [mockWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const multilineElements = wrapper.findAll('.multiline-text')
    expect(multilineElements.length).toBeGreaterThan(0)

    // Check that multiline elements contain the expected content
    const multilineTexts = multilineElements.map((el) => el.text())
    const hasAffectedAreas = multilineTexts.some((text) =>
      text.includes('London')
    )
    const hasFurtherDetails = multilineTexts.some((text) =>
      text.includes('Please be careful')
    )

    expect(hasAffectedAreas).toBe(true)
    expect(hasFurtherDetails).toBe(true)
  })

  it('returns default style when no warning is found', async () => {
    mockRoute.params.id = '999'
    mockWarningStore.allWarnings = [mockWarning]

    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    const style = mapCard.props('style')

    // Should have default style values
    expect(style.weight).toBe(1)
    expect(style.fillOpacity).toBe(0)
    expect(style.color).toBe('')
    expect(style.fillColor).toBe('')
  })

  it('handles unknown warning levels gracefully', async () => {
    const unknownWarning = { ...mockWarning, warningLevel: 'Unknown' }
    mockWarningStore.allWarnings = [unknownWarning]
    const wrapper = createWrapper()
    await flushPromises()

    const mapCard = wrapper.findComponent({ name: 'MapCard' })
    const style = mapCard.props('style')

    // Should have default style colors for unknown levels
    expect(style.weight).toBe(2)
    expect(style.fillOpacity).toBe(0.4)
    expect(style.color).toBe('')
    expect(style.fillColor).toBe('')
  })
})
