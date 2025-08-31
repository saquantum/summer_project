/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import DatePicker from '@/components/DatePicker.vue'

// Mock the useResponsiveAction composable
vi.mock('@/composables/useResponsiveAction', () => ({
  useResponsiveAction: vi.fn((_action: (_width: number) => void) => {
    // Call the action immediately with a default width
    _action(1024)
    return { screenWidth: { value: 1024 } }
  })
}))

// Global mocks for DOM APIs
const mockGetBoundingClientRect = vi.fn(() => ({
  bottom: 100,
  left: 50,
  top: 60,
  right: 200,
  width: 150,
  height: 40
}))

// Setup global mocks
global.window = Object.assign(global.window, {
  innerHeight: 768,
  innerWidth: 1024,
  addEventListener: vi.fn(),
  removeEventListener: vi.fn()
})

// Mock HTMLElement methods
Object.defineProperty(HTMLElement.prototype, 'getBoundingClientRect', {
  value: mockGetBoundingClientRect,
  writable: true
})

// Mock navigator for touch device detection
Object.defineProperty(navigator, 'maxTouchPoints', {
  writable: true,
  value: 0
})

describe('DatePicker', () => {
  let wrapper: VueWrapper<any>
  const defaultProps = {
    modelValue: null as [Date, Date] | null,
    teleported: false,
    appendToBody: false
  }

  const createWrapper = (props = {}) => {
    return mount(DatePicker, {
      props: { ...defaultProps, ...props },
      attachTo: document.body
    })
  }

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks()

    // Reset DOM
    document.body.innerHTML = ''

    // Mock touch detection
    Object.defineProperty(window, 'ontouchstart', {
      value: undefined,
      writable: true
    })

    // Reset navigator
    Object.defineProperty(navigator, 'maxTouchPoints', {
      value: 0,
      writable: true
    })
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Component Rendering', () => {
    it('renders correctly with default props', async () => {
      wrapper = createWrapper()
      await nextTick()

      expect(wrapper.find('.date-range-picker').exists()).toBe(true)
      expect(wrapper.find('.date-input').exists()).toBe(true)
      expect(wrapper.find('.picker-dropdown').exists()).toBe(true)
    })

    it('displays placeholder text when no date is selected', async () => {
      wrapper = createWrapper()
      await nextTick()

      const input = wrapper.find('.date-input')
      expect(input.attributes('placeholder')).toBe('Please select date range')
      expect((input.element as HTMLInputElement).value).toBe('')
    })

    it('displays formatted date range when modelValue is provided', async () => {
      const startDate = new Date(2024, 0, 1) // January 1, 2024
      const endDate = new Date(2024, 0, 15) // January 15, 2024

      wrapper = createWrapper({
        modelValue: [startDate, endDate]
      })
      await nextTick()

      const input = wrapper.find('.date-input')
      expect((input.element as HTMLInputElement).value).toBe(
        '2024-01-01 to 2024-01-15'
      )
    })

    it('shows clear button when dates are selected', async () => {
      wrapper = createWrapper()
      await nextTick()

      // Initially no clear button
      expect(wrapper.find('.clear-btn').exists()).toBe(false)

      // Set temp dates (which would happen when user selects dates)
      wrapper.vm.tempStartDate = new Date(2024, 0, 1)
      wrapper.vm.tempEndDate = new Date(2024, 0, 15)
      await nextTick()

      expect(wrapper.find('.clear-btn').exists()).toBe(true)
    })

    it('hides clear button when no dates are selected', async () => {
      wrapper = createWrapper()
      await nextTick()

      expect(wrapper.find('.clear-btn').exists()).toBe(false)
    })
  })

  describe('Date Picker Opening and Closing', () => {
    it('opens picker when input is clicked', async () => {
      wrapper = createWrapper()
      await nextTick()

      const input = wrapper.find('.date-input')
      await input.trigger('click')
      await nextTick()

      expect(wrapper.vm.isOpen).toBe(true)
      expect(wrapper.find('.picker-dropdown.show').exists()).toBe(true)
    })

    it('closes picker when cancel button is clicked', async () => {
      wrapper = createWrapper()
      await nextTick()

      // Open picker
      await wrapper.find('.date-input').trigger('click')
      await nextTick()
      expect(wrapper.vm.isOpen).toBe(true)

      // Close picker
      await wrapper.find('.btn-cancel').trigger('click')
      await nextTick()
      expect(wrapper.vm.isOpen).toBe(false)
    })

    it('initializes with current date range when opening with existing modelValue', async () => {
      const startDate = new Date(2024, 0, 1)
      const endDate = new Date(2024, 0, 15)

      wrapper = createWrapper({
        modelValue: [startDate, endDate]
      })
      await nextTick()

      await wrapper.find('.date-input').trigger('click')
      await nextTick()

      expect(wrapper.vm.tempStartDate).toEqual(startDate)
      expect(wrapper.vm.tempEndDate).toEqual(endDate)
    })
  })

  describe('Date Selection', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await nextTick()
      await wrapper.find('.date-input').trigger('click')
      await nextTick()
    })

    it('selects start date first', async () => {
      const targetDate = new Date(2024, 0, 15)

      // Mock selectDate method call
      wrapper.vm.selectDate(targetDate)
      await nextTick()

      expect(wrapper.vm.tempStartDate).toEqual(targetDate)
      expect(wrapper.vm.tempEndDate).toBe(null)
      expect(wrapper.vm.isSelectingStart).toBe(false)
    })

    it('selects end date after start date', async () => {
      const startDate = new Date(2024, 0, 10)
      const endDate = new Date(2024, 0, 20)

      // Select start date
      wrapper.vm.selectDate(startDate)
      await nextTick()

      // Select end date
      wrapper.vm.selectDate(endDate)
      await nextTick()

      expect(wrapper.vm.tempStartDate).toEqual(startDate)
      expect(wrapper.vm.tempEndDate).toEqual(endDate)
    })

    it('swaps dates when end date is before start date', async () => {
      const laterDate = new Date(2024, 0, 20)
      const earlierDate = new Date(2024, 0, 10)

      // Select later date first
      wrapper.vm.selectDate(laterDate)
      await nextTick()

      // Select earlier date as "end date"
      wrapper.vm.selectDate(earlierDate)
      await nextTick()

      expect(wrapper.vm.tempStartDate).toEqual(earlierDate)
      expect(wrapper.vm.tempEndDate).toEqual(laterDate)
    })

    it('can confirm selection when both dates are selected', async () => {
      const startDate = new Date(2024, 0, 10)
      const endDate = new Date(2024, 0, 20)

      wrapper.vm.selectDate(startDate)
      wrapper.vm.selectDate(endDate)
      await nextTick()

      // canConfirm should be a boolean true when both dates are set
      expect(!!wrapper.vm.canConfirm).toBe(true)
      expect(
        wrapper.find('.btn-confirm').attributes('disabled')
      ).toBeUndefined()
    })

    it('cannot confirm selection when only start date is selected', async () => {
      const startDate = new Date(2024, 0, 10)

      wrapper.vm.selectDate(startDate)
      await nextTick()

      // canConfirm should be falsy when only start date is set
      expect(!!wrapper.vm.canConfirm).toBe(false)
      expect(wrapper.find('.btn-confirm').attributes('disabled')).toBeDefined()
    })
  })

  describe('Calendar Navigation', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await nextTick()
      await wrapper.find('.date-input').trigger('click')
      await nextTick()
    })

    it('initializes calendars correctly', () => {
      const now = new Date()
      const leftExpected = new Date(now.getFullYear(), now.getMonth(), 1)
      const rightExpected = new Date(now.getFullYear(), now.getMonth() + 1, 1)

      expect(wrapper.vm.leftCalendar.getFullYear()).toBe(
        leftExpected.getFullYear()
      )
      expect(wrapper.vm.leftCalendar.getMonth()).toBe(leftExpected.getMonth())
      expect(wrapper.vm.rightCalendar.getFullYear()).toBe(
        rightExpected.getFullYear()
      )
      expect(wrapper.vm.rightCalendar.getMonth()).toBe(rightExpected.getMonth())
    })

    it('navigates to previous month on left calendar', async () => {
      const initialMonth = wrapper.vm.leftCalendar.getMonth()

      wrapper.vm.prevMonth(0)
      await nextTick()

      const expectedMonth = initialMonth === 0 ? 11 : initialMonth - 1
      expect(wrapper.vm.leftCalendar.getMonth()).toBe(expectedMonth)
    })

    it('navigates to next month on right calendar', async () => {
      const initialMonth = wrapper.vm.rightCalendar.getMonth()

      wrapper.vm.nextMonth(1)
      await nextTick()

      const expectedMonth = initialMonth === 11 ? 0 : initialMonth + 1
      expect(wrapper.vm.rightCalendar.getMonth()).toBe(expectedMonth)
    })

    it('navigates to previous year on left calendar', async () => {
      const initialYear = wrapper.vm.leftCalendar.getFullYear()

      wrapper.vm.prevYear(0)
      await nextTick()

      expect(wrapper.vm.leftCalendar.getFullYear()).toBe(initialYear - 1)
    })

    it('navigates to next year on right calendar', async () => {
      const initialYear = wrapper.vm.rightCalendar.getFullYear()

      wrapper.vm.nextYear(1)
      await nextTick()

      expect(wrapper.vm.rightCalendar.getFullYear()).toBe(initialYear + 1)
    })
  })

  describe('Quick Date Selection', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await nextTick()
      await wrapper.find('.date-input').trigger('click')
      await nextTick()
    })

    it('selects today when Today button is clicked', async () => {
      const today = new Date()

      await wrapper.find('button').trigger('click') // Assuming first button is Today
      wrapper.vm.selectQuickRange('today')
      await nextTick()

      expect(wrapper.vm.tempStartDate?.toDateString()).toBe(
        today.toDateString()
      )
      expect(wrapper.vm.tempEndDate?.toDateString()).toBe(today.toDateString())
    })

    it('selects yesterday when Yesterday button is clicked', async () => {
      const yesterday = new Date()
      yesterday.setDate(yesterday.getDate() - 1)

      wrapper.vm.selectQuickRange('yesterday')
      await nextTick()

      expect(wrapper.vm.tempStartDate?.toDateString()).toBe(
        yesterday.toDateString()
      )
      expect(wrapper.vm.tempEndDate?.toDateString()).toBe(
        yesterday.toDateString()
      )
    })

    it('selects last 7 days when Last 7 days button is clicked', async () => {
      const today = new Date()
      const sevenDaysAgo = new Date()
      sevenDaysAgo.setDate(today.getDate() - 6)

      wrapper.vm.selectQuickRange('last7days')
      await nextTick()

      expect(wrapper.vm.tempStartDate?.toDateString()).toBe(
        sevenDaysAgo.toDateString()
      )
      expect(wrapper.vm.tempEndDate?.toDateString()).toBe(today.toDateString())
    })

    it('selects last 30 days when Last 30 days button is clicked', async () => {
      const today = new Date()
      const thirtyDaysAgo = new Date()
      thirtyDaysAgo.setDate(today.getDate() - 29)

      wrapper.vm.selectQuickRange('last30days')
      await nextTick()

      expect(wrapper.vm.tempStartDate?.toDateString()).toBe(
        thirtyDaysAgo.toDateString()
      )
      expect(wrapper.vm.tempEndDate?.toDateString()).toBe(today.toDateString())
    })

    it('selects this month when This month button is clicked', async () => {
      const today = new Date()
      const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
      const lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0)

      wrapper.vm.selectQuickRange('thisMonth')
      await nextTick()

      expect(wrapper.vm.tempStartDate?.toDateString()).toBe(
        firstDay.toDateString()
      )
      expect(wrapper.vm.tempEndDate?.toDateString()).toBe(
        lastDay.toDateString()
      )
    })

    it('selects last month when Last month button is clicked', async () => {
      const today = new Date()
      const firstDay = new Date(today.getFullYear(), today.getMonth() - 1, 1)
      const lastDay = new Date(today.getFullYear(), today.getMonth(), 0)

      wrapper.vm.selectQuickRange('lastMonth')
      await nextTick()

      expect(wrapper.vm.tempStartDate?.toDateString()).toBe(
        firstDay.toDateString()
      )
      expect(wrapper.vm.tempEndDate?.toDateString()).toBe(
        lastDay.toDateString()
      )
    })
  })

  describe('Date Confirmation and Emission', () => {
    it('emits update:modelValue when confirm is clicked with valid dates', async () => {
      wrapper = createWrapper()
      await nextTick()

      const startDate = new Date(2024, 0, 10)
      const endDate = new Date(2024, 0, 20)

      await wrapper.find('.date-input').trigger('click')
      wrapper.vm.selectDate(startDate)
      wrapper.vm.selectDate(endDate)
      await nextTick()

      await wrapper.find('.btn-confirm').trigger('click')
      await nextTick()

      const emitted = wrapper.emitted('update:modelValue')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toEqual([startDate, endDate])
      expect(wrapper.vm.isOpen).toBe(false)
    })

    it('does not emit when confirm is clicked without valid dates', async () => {
      wrapper = createWrapper()
      await nextTick()

      await wrapper.find('.date-input').trigger('click')
      // Don't select any dates
      await wrapper.find('.btn-confirm').trigger('click')
      await nextTick()

      expect(wrapper.emitted('update:modelValue')).toBeFalsy()
    })
  })

  describe('Date Clearing', () => {
    it('clears dates when clear button is clicked', async () => {
      wrapper = createWrapper()
      await nextTick()

      // Set temp dates to show clear button
      wrapper.vm.tempStartDate = new Date(2024, 0, 1)
      wrapper.vm.tempEndDate = new Date(2024, 0, 15)
      await nextTick()

      // Ensure clear button exists
      expect(wrapper.find('.clear-btn').exists()).toBe(true)

      await wrapper.find('.clear-btn').trigger('click')
      await nextTick()

      const emitted = wrapper.emitted('update:modelValue')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toBe(null)
    })

    it('clears temporary dates when clearDates is called', async () => {
      wrapper = createWrapper()
      await nextTick()

      wrapper.vm.tempStartDate = new Date(2024, 0, 1)
      wrapper.vm.tempEndDate = new Date(2024, 0, 15)

      wrapper.vm.clearDates()
      await nextTick()

      expect(wrapper.vm.tempStartDate).toBe(null)
      expect(wrapper.vm.tempEndDate).toBe(null)
    })
  })

  describe('Date Formatting', () => {
    it('formats dates correctly', () => {
      const date = new Date(2024, 0, 15) // January 15, 2024
      const formatted = wrapper.vm.formatDate(date)
      expect(formatted).toBe('2024-01-15')
    })

    it('returns empty string for null date', () => {
      const formatted = wrapper.vm.formatDate(null)
      expect(formatted).toBe('')
    })

    it('displays correct formatted range', async () => {
      const startDate = new Date(2024, 0, 1)
      const endDate = new Date(2024, 0, 15)

      wrapper = createWrapper()
      wrapper.vm.tempStartDate = startDate
      wrapper.vm.tempEndDate = endDate
      await nextTick()

      expect(wrapper.vm.dateRangeFormatted).toBe('2024-01-01 to 2024-01-15')
    })

    it('displays partial selection message', async () => {
      const startDate = new Date(2024, 0, 1)

      wrapper = createWrapper()
      wrapper.vm.tempStartDate = startDate
      wrapper.vm.tempEndDate = null
      await nextTick()

      expect(wrapper.vm.dateRangeFormatted).toBe(
        '2024-01-01 to Please select end date'
      )
    })
  })

  describe('Calendar Helper Functions', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await nextTick()
    })

    it('checks if two dates are the same day correctly', () => {
      const date1 = new Date(2024, 0, 15, 10, 30) // Different time
      const date2 = new Date(2024, 0, 15, 20, 45) // Different time, same day
      const date3 = new Date(2024, 0, 16) // Different day

      expect(wrapper.vm.isSameDay(date1, date2)).toBe(true)
      expect(wrapper.vm.isSameDay(date1, date3)).toBe(false)
      expect(wrapper.vm.isSameDay(null, date1)).toBe(false)
      expect(wrapper.vm.isSameDay(date1, null)).toBe(false)
    })

    it('generates month year text correctly', () => {
      const date = new Date(2024, 0, 15) // January 2024
      const text = wrapper.vm.getMonthYearText(date)
      expect(text).toBe('January 2024')
    })

    it('generates calendar days correctly', () => {
      const date = new Date(2024, 0, 1) // January 2024
      const days = wrapper.vm.getCalendarDays(date)

      expect(days).toHaveLength(42) // 6 weeks * 7 days
      expect(days.some((day: any) => day.isCurrentMonth)).toBe(true)
      expect(days.some((day: any) => !day.isCurrentMonth)).toBe(true)
    })

    it('generates correct day classes', async () => {
      const today = new Date()
      const selectedStart = new Date(2024, 0, 10)
      const selectedEnd = new Date(2024, 0, 20)
      const inRangeDate = new Date(2024, 0, 15)

      wrapper.vm.tempStartDate = selectedStart
      wrapper.vm.tempEndDate = selectedEnd
      await nextTick()

      const todayDay = { date: today, isCurrentMonth: true, key: 'today' }
      const startDay = {
        date: selectedStart,
        isCurrentMonth: true,
        key: 'start'
      }
      const endDay = { date: selectedEnd, isCurrentMonth: true, key: 'end' }
      const inRangeDay = {
        date: inRangeDate,
        isCurrentMonth: true,
        key: 'range'
      }

      expect(wrapper.vm.getDayClasses(todayDay)).toContain('today')
      expect(wrapper.vm.getDayClasses(startDay)).toContain('start-date')
      expect(wrapper.vm.getDayClasses(endDay)).toContain('end-date')
      expect(wrapper.vm.getDayClasses(inRangeDay)).toContain('in-range')
    })
  })

  describe('Touch Device Detection', () => {
    it('detects touch device correctly', async () => {
      // Mock touch device
      Object.defineProperty(window, 'ontouchstart', {
        value: {},
        writable: true,
        configurable: true
      })

      wrapper = createWrapper()
      wrapper.vm.detectTouchDevice()
      await nextTick()

      expect(wrapper.vm.isTouchDevice).toBe(true)
    })

    it('detects non-touch device correctly', async () => {
      // Create a new window mock without ontouchstart
      const originalWindow = global.window
      const mockWindow = {
        ...originalWindow,
        innerHeight: 768,
        innerWidth: 1024,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn()
      }
      // Explicitly don't set ontouchstart
      global.window = mockWindow as any

      Object.defineProperty(navigator, 'maxTouchPoints', {
        value: 0,
        writable: true
      })

      wrapper = createWrapper()
      wrapper.vm.detectTouchDevice()
      await nextTick()

      expect(wrapper.vm.isTouchDevice).toBe(false)

      // Restore original window
      global.window = originalWindow
    })
  })

  describe('Teleport Functionality', () => {
    it('enables teleport when teleported prop is true', async () => {
      wrapper = createWrapper({ teleported: true })
      await nextTick()

      expect(wrapper.vm.shouldTeleport).toBe(true)
    })

    it('enables teleport when appendToBody prop is true', async () => {
      wrapper = createWrapper({ appendToBody: true })
      await nextTick()

      expect(wrapper.vm.shouldTeleport).toBe(true)
    })

    it('disables teleport when both props are false', async () => {
      wrapper = createWrapper({ teleported: false, appendToBody: false })
      await nextTick()

      expect(wrapper.vm.shouldTeleport).toBe(false)
    })
  })

  describe('Hover Functionality', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await nextTick()
      await wrapper.find('.date-input').trigger('click')

      // Set up for end date selection
      wrapper.vm.tempStartDate = new Date(2024, 0, 10)
      wrapper.vm.isSelectingStart = false
      wrapper.vm.isTouchDevice = false
      await nextTick()
    })

    it('sets hover date on non-touch devices during end date selection', async () => {
      const hoverDate = new Date(2024, 0, 15)

      wrapper.vm.onDayHover(hoverDate)
      await nextTick()

      expect(wrapper.vm.hoverDate).toEqual(hoverDate)
    })

    it('does not set hover date on touch devices', async () => {
      wrapper.vm.isTouchDevice = true
      const hoverDate = new Date(2024, 0, 15)

      wrapper.vm.onDayHover(hoverDate)
      await nextTick()

      expect(wrapper.vm.hoverDate).toBe(null)
    })

    it('clears hover date on touch start', async () => {
      wrapper.vm.hoverDate = new Date(2024, 0, 15)
      wrapper.vm.isTouchDevice = true

      wrapper.vm.onDayTouchStart()
      await nextTick()

      expect(wrapper.vm.hoverDate).toBe(null)
    })
  })

  describe('Computed Properties', () => {
    beforeEach(async () => {
      wrapper = createWrapper()
      await nextTick()
    })

    it('computes navigation availability correctly', async () => {
      // Set up calendars with specific dates
      wrapper.vm.leftCalendar = new Date(2024, 0, 1) // January 2024
      wrapper.vm.rightCalendar = new Date(2024, 1, 1) // February 2024
      await nextTick()

      expect(wrapper.vm.canGoPrevMonth).toBe(true)
      expect(wrapper.vm.canGoNextMonth).toBe(true)
      expect(wrapper.vm.canGoPrevYear).toBe(false)
      expect(wrapper.vm.canGoNextYear).toBe(false)
    })

    it('prevents navigation when calendars would overlap', async () => {
      // Set calendars to same month
      wrapper.vm.leftCalendar = new Date(2024, 0, 1) // January 2024
      wrapper.vm.rightCalendar = new Date(2024, 0, 1) // January 2024
      await nextTick()

      expect(wrapper.vm.canGoPrevMonth).toBe(false)
      expect(wrapper.vm.canGoNextMonth).toBe(false)
    })
  })

  describe('Watchers and Lifecycle', () => {
    it('updates position when dropdown opens in teleport mode', async () => {
      wrapper = createWrapper({ teleported: true })
      await nextTick()

      // Manually call updateDropdownPosition to test the display logic
      await wrapper.vm.updateDropdownPosition()
      wrapper.vm.isOpen = true
      await wrapper.vm.updateDropdownPosition()

      // Position should be updated when opening
      expect(wrapper.vm.dropdownStyle.display).toBe('block')
    })

    it('responds to modelValue changes', async () => {
      wrapper = createWrapper()
      await nextTick()

      const newDates: [Date, Date] = [
        new Date(2024, 0, 1),
        new Date(2024, 0, 15)
      ]
      await wrapper.setProps({ modelValue: newDates })

      expect(wrapper.vm.tempStartDate).toEqual(newDates[0])
      expect(wrapper.vm.tempEndDate).toEqual(newDates[1])
    })

    it('clears temp dates when modelValue is null', async () => {
      const initialDates: [Date, Date] = [
        new Date(2024, 0, 1),
        new Date(2024, 0, 15)
      ]
      wrapper = createWrapper({ modelValue: initialDates })
      await nextTick()

      await wrapper.setProps({ modelValue: null })

      expect(wrapper.vm.tempStartDate).toBe(null)
      expect(wrapper.vm.tempEndDate).toBe(null)
      expect(wrapper.vm.hoverDate).toBe(null)
    })
  })

  describe('Component Exposure', () => {
    it('exposes required methods and properties', async () => {
      wrapper = createWrapper()
      await nextTick()

      const exposedProps = [
        'isOpen',
        'isSelectingStart',
        'leftCalendar',
        'rightCalendar',
        'hoverDate',
        'tempStartDate',
        'tempEndDate',
        'weekdays',
        'dateRangeFormatted',
        'canConfirm',
        'getMonthYearText',
        'getCalendarDays',
        'getDayClasses',
        'prevMonth',
        'nextMonth',
        'openPicker',
        'selectDate',
        'onDayHover',
        'selectQuickRange',
        'confirm',
        'cancel'
      ]

      exposedProps.forEach((prop) => {
        expect(wrapper.vm[prop]).toBeDefined()
      })
    })
  })
})
