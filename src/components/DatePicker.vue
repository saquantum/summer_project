<script setup lang="ts">
/**
 * usage:
 * <DatePicker :teleported="true" v-model="form.lastInspection" />
 *
 * When teleported is true, it will enable Vue teleport function to pass the
 * component to the body. Use v-model to bind the variable, the component will pass
 * a [Date, Date] array to its parent component.
 */
import { useResponsiveAction } from '@/composables/useResponsiveAction'
import {
  DArrowRight,
  DArrowLeft,
  ArrowLeft,
  ArrowRight,
  Close
} from '@element-plus/icons-vue'
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import type { CSSProperties } from 'vue'

interface DatePickerProps {
  modelValue: [Date, Date] | null
  teleported?: boolean
  appendToBody?: boolean
}

const props = withDefaults(defineProps<DatePickerProps>(), {
  teleported: false,
  appendToBody: false
})

const emit = defineEmits([
  'update:startDate',
  'update:endDate',
  'change',
  'update:modelValue'
])

const isOpen = ref(false)
const isSelectingStart = ref(true)
const leftCalendar = ref(new Date())
const rightCalendar = ref(new Date())
const hoverDate = ref<Date | null>(null)
const tempStartDate = ref<Date | null>(null)
const tempEndDate = ref<Date | null>(null)
const isTouchDevice = ref(false)
const isMobile = ref(false)

const inputRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const dropdownStyle = ref<CSSProperties>({
  position: 'absolute',
  top: '0px',
  left: '0px',
  width: 'auto',
  zIndex: 2000,
  display: 'none'
})

const shouldTeleport = computed(() => {
  return props.teleported || props.appendToBody
})

const months = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December'
]
const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

const detectTouchDevice = () => {
  isTouchDevice.value = 'ontouchstart' in window || navigator.maxTouchPoints > 0
}

// Initialize calendars
const initCalendars = () => {
  const now = new Date()
  leftCalendar.value = new Date(now.getFullYear(), now.getMonth(), 1)
  rightCalendar.value = new Date(now.getFullYear(), now.getMonth() + 1, 1)
}

const updateDropdownPosition = async () => {
  if (!shouldTeleport.value) return

  await nextTick()
  if (!inputRef.value || !dropdownRef.value) return

  // Mobile devices use full screen, no need to calculate position
  if (isMobile.value) {
    dropdownStyle.value = {
      position: 'fixed',
      top: '0px',
      left: '0px',
      width: '100vw',
      height: '100vh',
      zIndex: 2000,
      display: isOpen.value ? 'block' : 'none'
    }
    return
  }

  const inputRect = inputRef.value.getBoundingClientRect()
  const viewportHeight = window.innerHeight
  const viewportWidth = window.innerWidth

  // Set the maximum size of the dropdown
  const maxWidth = Math.min(800, viewportWidth - 40)
  const maxHeight = Math.min(600, viewportHeight * 0.8)

  // Calculate the best display position
  let top = inputRect.bottom + 8
  let left = inputRect.left

  // Check if it needs to be displayed upward
  if (top + maxHeight > viewportHeight - 20) {
    const spaceAbove = inputRect.top - 20
    if (
      spaceAbove >= maxHeight ||
      spaceAbove > viewportHeight - inputRect.bottom
    ) {
      top = inputRect.top - maxHeight - 8
    } else {
      // Center display
      top = Math.max(20, (viewportHeight - maxHeight) / 2)
    }
  }

  // Check horizontal position
  if (left + maxWidth > viewportWidth - 20) {
    left = viewportWidth - maxWidth - 20
  }
  if (left < 20) {
    left = 20
  }

  dropdownStyle.value = {
    position: 'fixed',
    top: `${Math.max(20, top)}px`,
    left: `${left}px`,
    width: `${maxWidth}px`,
    maxHeight: `${maxHeight}px`,
    zIndex: 2000,
    display: isOpen.value ? 'block' : 'none'
  }
}

// Format date
const formatDate = (date: Date) => {
  if (!date) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// Computed properties
const dateRangeFormatted = computed(() => {
  const start =
    props.modelValue && props.modelValue[0]
      ? props.modelValue[0]
      : tempStartDate.value
  const end =
    props.modelValue && props.modelValue[1]
      ? props.modelValue[1]
      : tempEndDate.value

  if (start && end) {
    return `${formatDate(start as Date)} to ${formatDate(end as Date)}`
  } else if (start) {
    return `${formatDate(start as Date)} to Please select end date`
  } else {
    return ''
  }
})

const canConfirm = computed(() => {
  return tempStartDate.value && tempEndDate.value
})

// left is smaller than right
const canGoPrevMonth = computed(() => {
  const left = leftCalendar.value
  const right = rightCalendar.value
  return (
    left.getFullYear() < right.getFullYear() ||
    (left.getFullYear() === right.getFullYear() &&
      left.getMonth() < right.getMonth())
  )
})

const canGoNextMonth = computed(() => {
  const left = leftCalendar.value
  const right = rightCalendar.value
  return (
    right.getFullYear() > left.getFullYear() ||
    (right.getFullYear() === left.getFullYear() &&
      right.getMonth() > left.getMonth())
  )
})

// left is smaller or equal than right
const canGoPrevYear = computed(() => {
  const left = leftCalendar.value
  const right = rightCalendar.value
  return left.getFullYear() < right.getFullYear()
})

const canGoNextYear = computed(() => {
  const left = leftCalendar.value
  const right = rightCalendar.value
  return right.getFullYear() > left.getFullYear()
})

// Get canlender title
const getMonthYearText = (date: Date) => {
  return `${months[date.getMonth()]} ${date.getFullYear()}`
}

interface Day {
  date: Date
  isCurrentMonth: boolean
  key: string
}
// Get calendar days data
const getCalendarDays = (calendarDate: Date) => {
  const days: Day[] = []
  const firstDay = new Date(
    calendarDate.getFullYear(),
    calendarDate.getMonth(),
    1
  )
  const lastDay = new Date(
    calendarDate.getFullYear(),
    calendarDate.getMonth() + 1,
    0
  )
  const prevLastDay = new Date(
    calendarDate.getFullYear(),
    calendarDate.getMonth(),
    0
  )

  // Last days of previous month
  const startDay = firstDay.getDay()
  for (let i = startDay - 1; i >= 0; i--) {
    const date = new Date(prevLastDay)
    date.setDate(prevLastDay.getDate() - i)
    days.push({
      date,
      isCurrentMonth: false,
      key: `prev-${date.getDate()}-${date.getMonth()}`
    })
  }

  // Days of current month
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(
      calendarDate.getFullYear(),
      calendarDate.getMonth(),
      day
    )
    days.push({
      date,
      isCurrentMonth: true,
      key: `current-${day}-${date.getMonth()}`
    })
  }

  // First days of next month
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(
      calendarDate.getFullYear(),
      calendarDate.getMonth() + 1,
      day
    )
    days.push({
      date,
      isCurrentMonth: false,
      key: `next-${day}-${date.getMonth()}`
    })
  }

  return days
}

// Get date style classes
const getDayClasses = (day: Day) => {
  const classes = []
  const { date, isCurrentMonth } = day

  if (!isCurrentMonth) {
    classes.push('other-month')
  }

  // Today
  const today = new Date()
  if (isSameDay(date, today)) {
    classes.push('today')
  }

  // Start date
  if (tempStartDate.value && isSameDay(date, tempStartDate.value)) {
    classes.push('start-date')
  }

  // End date
  if (tempEndDate.value && isSameDay(date, tempEndDate.value)) {
    classes.push('end-date')
  }

  // Within range
  if (
    tempStartDate.value &&
    tempEndDate.value &&
    date > tempStartDate.value &&
    date < tempEndDate.value
  ) {
    classes.push('in-range')
  }

  // Hover range for untouchable screen
  if (
    !isTouchDevice.value &&
    hoverDate.value &&
    tempStartDate.value &&
    !tempEndDate.value
  ) {
    const start = tempStartDate.value
    const end = hoverDate.value
    if ((date > start && date < end) || (date > end && date < start)) {
      classes.push('hover-range')
    }
  }

  return classes
}

// Check if two dates are the same day
const isSameDay = (date1: Date, date2: Date) => {
  if (!date1 || !date2) return false
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  )
}

// Navigation methods
// Switch year
const prevYear = (calendarIndex: number) => {
  if (calendarIndex === 0) {
    leftCalendar.value = new Date(
      leftCalendar.value.getFullYear() - 1,
      leftCalendar.value.getMonth(),
      1
    )
  } else {
    rightCalendar.value = new Date(
      rightCalendar.value.getFullYear() - 1,
      rightCalendar.value.getMonth(),
      1
    )
  }
}

const nextYear = (calendarIndex: number) => {
  if (calendarIndex === 0) {
    leftCalendar.value = new Date(
      leftCalendar.value.getFullYear() + 1,
      leftCalendar.value.getMonth(),
      1
    )
  } else {
    rightCalendar.value = new Date(
      rightCalendar.value.getFullYear() + 1,
      rightCalendar.value.getMonth(),
      1
    )
  }
}
const prevMonth = (calendarIndex: number) => {
  if (calendarIndex === 0) {
    leftCalendar.value = new Date(
      leftCalendar.value.getFullYear(),
      leftCalendar.value.getMonth() - 1,
      1
    )
  } else {
    rightCalendar.value = new Date(
      rightCalendar.value.getFullYear(),
      rightCalendar.value.getMonth() - 1,
      1
    )
  }
}

const nextMonth = (calendarIndex: number) => {
  if (calendarIndex === 0) {
    leftCalendar.value = new Date(
      leftCalendar.value.getFullYear(),
      leftCalendar.value.getMonth() + 1,
      1
    )
  } else {
    rightCalendar.value = new Date(
      rightCalendar.value.getFullYear(),
      rightCalendar.value.getMonth() + 1,
      1
    )
  }
}

// Open picker
const openPicker = async () => {
  if (!isOpen.value) {
    if (props.modelValue) {
      tempStartDate.value = props.modelValue[0]
      tempEndDate.value = props.modelValue[1]
    } else {
      tempStartDate.value = null
      tempEndDate.value = null
    }

    isSelectingStart.value = !tempStartDate.value
    isOpen.value = true
    await updateDropdownPosition()
  }
}

// Select date
const selectDate = (date: Date) => {
  // Clear hover state
  hoverDate.value = null

  if (isSelectingStart.value) {
    tempStartDate.value = new Date(date)
    tempEndDate.value = null
    isSelectingStart.value = false
  } else {
    if (tempStartDate.value && date < tempStartDate.value) {
      tempEndDate.value = tempStartDate.value
      tempStartDate.value = new Date(date)
    } else {
      tempEndDate.value = new Date(date)
    }
  }
}

// Date hover
const onDayHover = (date: Date) => {
  if (
    !isTouchDevice.value &&
    !isSelectingStart.value &&
    tempStartDate.value &&
    !tempEndDate.value
  ) {
    hoverDate.value = new Date(date)
  }
}

// clear hover date for touchable device
const onDayTouchStart = () => {
  if (isTouchDevice.value) {
    hoverDate.value = null
  }
}

// Quick select
const selectQuickRange = (type: string) => {
  const today = new Date()
  const start = new Date()

  switch (type) {
    case 'today':
      tempStartDate.value = new Date(today)
      tempEndDate.value = new Date(today)
      break
    case 'yesterday':
      start.setDate(today.getDate() - 1)
      tempStartDate.value = new Date(start)
      tempEndDate.value = new Date(start)
      break
    case 'last7days':
      start.setDate(today.getDate() - 6)
      tempStartDate.value = new Date(start)
      tempEndDate.value = new Date(today)
      break
    case 'last30days':
      start.setDate(today.getDate() - 29)
      tempStartDate.value = new Date(start)
      tempEndDate.value = new Date(today)
      break
    case 'thisMonth':
      tempStartDate.value = new Date(today.getFullYear(), today.getMonth(), 1)
      tempEndDate.value = new Date(today.getFullYear(), today.getMonth() + 1, 0)
      break
    case 'lastMonth':
      tempStartDate.value = new Date(
        today.getFullYear(),
        today.getMonth() - 1,
        1
      )
      tempEndDate.value = new Date(today.getFullYear(), today.getMonth(), 0)
      break
  }
}

// Confirm selection
const confirm = () => {
  if (tempStartDate.value && tempEndDate.value) {
    emit('update:modelValue', [tempStartDate.value, tempEndDate.value])
    isOpen.value = false
  }
}

// clear date
const clearDates = () => {
  tempStartDate.value = null
  tempEndDate.value = null
  emit('update:modelValue', null)
}

// Cancel selection
const cancel = () => {
  if (props.modelValue) {
    tempStartDate.value = props.modelValue[0]
    tempEndDate.value = props.modelValue[1]
  } else {
    tempStartDate.value = null
    tempEndDate.value = null
  }
  hoverDate.value = null
  isOpen.value = false
}

// Click outside to close
const handleClickOutside = (event: MouseEvent) => {
  if (!isOpen.value) return

  const dropdown = shouldTeleport.value
    ? dropdownRef.value
    : document.querySelector('.picker-dropdown')
  const input = inputRef.value

  if (
    dropdown &&
    input &&
    !dropdown.contains(event.target as Node) &&
    !input.contains(event.target as Node)
  ) {
    cancel()
  }
}

const handleScroll = () => {
  if (isOpen.value && shouldTeleport.value) {
    updateDropdownPosition()
  }
}

useResponsiveAction((width) => {
  if (width < 576) {
    isMobile.value = true
  } else if (width >= 576 && width < 768) {
  } else if (width >= 768 && width < 992) {
  } else {
  }
})

// Watch isOpen changes to update dropdown position
watch(isOpen, async (newVal) => {
  if (shouldTeleport.value) {
    if (newVal) {
      await updateDropdownPosition()
    } else {
      dropdownStyle.value.display = 'none'
    }
  }
})

watch(
  () => props.modelValue,
  (newVal) => {
    if (!newVal || !newVal[0]) {
      tempStartDate.value = null
      tempEndDate.value = null
      hoverDate.value = null
    } else {
      tempStartDate.value = newVal[0]
      tempEndDate.value = newVal[1]
    }
  }
)

onMounted(() => {
  detectTouchDevice()
  initCalendars()
  document.addEventListener('click', handleClickOutside)

  if (shouldTeleport.value) {
    window.addEventListener('scroll', handleScroll, true)
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)

  if (shouldTeleport.value) {
    window.removeEventListener('scroll', handleScroll, true)
  }
})

defineExpose({
  isOpen,
  isSelectingStart,
  leftCalendar,
  rightCalendar,
  hoverDate,
  tempStartDate,
  tempEndDate,
  weekdays,
  dateRangeFormatted,
  canConfirm,
  getMonthYearText,
  getCalendarDays,
  getDayClasses,
  prevMonth,
  nextMonth,
  openPicker,
  selectDate,
  onDayHover,
  selectQuickRange,
  confirm,
  cancel
})
</script>

<template>
  <div class="date-range-picker">
    <div class="input-group">
      <div class="input-wrapper">
        <input
          ref="inputRef"
          type="text"
          class="date-input single-input"
          :value="dateRangeFormatted"
          placeholder="Please select date range"
          readonly
          @click="openPicker()"
        />
        <button
          v-if="tempStartDate || tempEndDate"
          class="clear-btn"
          type="button"
          @click="clearDates"
          aria-label="Clear"
        >
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </div>

    <component
      :is="shouldTeleport ? 'Teleport' : 'div'"
      :to="shouldTeleport ? 'body' : undefined"
    >
      <div
        ref="dropdownRef"
        class="picker-dropdown"
        :class="{
          show: isOpen,
          teleported: shouldTeleport,
          'mobile-mode': isMobile
        }"
        :style="shouldTeleport ? dropdownStyle : {}"
        v-show="isOpen"
      >
        <!-- Mobile header -->
        <div v-if="isMobile && shouldTeleport" class="mobile-header">
          <button type="button" class="mobile-close-btn" @click="cancel">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M18 6L6 18M6 6l12 12"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>
          <h3 class="mobile-title">Select Date Range</h3>
          <div style="width: 24px"></div>
        </div>

        <!-- Scroll container -->
        <div class="dropdown-content">
          <div class="calendar-container">
            <div class="calendar">
              <div class="calendar-header">
                <button type="button" class="nav-btn" @click="prevYear(0)">
                  <el-icon><DArrowLeft /></el-icon>
                </button>
                <button type="button" class="nav-btn" @click="prevMonth(0)">
                  <el-icon><ArrowLeft /></el-icon>
                </button>
                <div class="month-year">
                  {{ getMonthYearText(leftCalendar) }}
                </div>
                <button
                  type="button"
                  class="nav-btn"
                  @click="nextMonth(0)"
                  :disabled="!canGoNextMonth"
                >
                  <el-icon><ArrowRight /></el-icon>
                </button>
                <button
                  type="button"
                  class="nav-btn"
                  @click="nextYear(0)"
                  :disabled="!canGoNextYear"
                >
                  <el-icon><DArrowRight /></el-icon>
                </button>
              </div>

              <div class="weekdays">
                <div class="weekday" v-for="day in weekdays" :key="day">
                  {{ day }}
                </div>
              </div>

              <div class="days">
                <div
                  v-for="day in getCalendarDays(leftCalendar)"
                  :key="day.key"
                  class="day"
                  :class="getDayClasses(day)"
                  @click="selectDate(day.date)"
                  @mouseenter="onDayHover(day.date)"
                  @touchstart="onDayTouchStart"
                >
                  {{ day.date.getDate() }}
                </div>
              </div>
            </div>

            <div class="calendar">
              <div class="calendar-header">
                <button
                  type="button"
                  class="nav-btn"
                  @click="prevYear(1)"
                  :disabled="!canGoPrevYear"
                >
                  <el-icon><DArrowLeft /></el-icon>
                </button>
                <button
                  type="button"
                  class="nav-btn"
                  @click="prevMonth(1)"
                  :disabled="!canGoPrevMonth"
                >
                  <el-icon><ArrowLeft /></el-icon>
                </button>
                <div class="month-year">
                  {{ getMonthYearText(rightCalendar) }}
                </div>
                <button type="button" class="nav-btn" @click="nextMonth(1)">
                  <el-icon><ArrowRight /></el-icon>
                </button>
                <button type="button" class="nav-btn" @click="nextYear(1)">
                  <el-icon><DArrowRight /></el-icon>
                </button>
              </div>

              <div class="weekdays">
                <div class="weekday" v-for="day in weekdays" :key="day">
                  {{ day }}
                </div>
              </div>

              <div class="days">
                <div
                  v-for="day in getCalendarDays(rightCalendar)"
                  :key="day.key"
                  class="day"
                  :class="getDayClasses(day)"
                  @click="selectDate(day.date)"
                  @mouseenter="onDayHover(day.date)"
                  @touchstart="onDayTouchStart"
                >
                  {{ day.date.getDate() }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Fixed bottom area -->
        <div class="dropdown-footer">
          <div class="quick-select">
            <button
              type="button"
              class="quick-btn"
              @click="selectQuickRange('today')"
            >
              Today
            </button>
            <button
              type="button"
              class="quick-btn"
              @click="selectQuickRange('yesterday')"
            >
              Yesterday
            </button>
            <button
              type="button"
              class="quick-btn"
              @click="selectQuickRange('last7days')"
            >
              Last 7 days
            </button>
            <button
              type="button"
              class="quick-btn"
              @click="selectQuickRange('last30days')"
            >
              Last 30 days
            </button>
            <button
              type="button"
              class="quick-btn"
              @click="selectQuickRange('thisMonth')"
            >
              This month
            </button>
            <button
              type="button"
              class="quick-btn"
              @click="selectQuickRange('lastMonth')"
            >
              Last month
            </button>
          </div>

          <div class="actions">
            <button class="btn btn-cancel" type="button" @click="cancel">
              Cancel
            </button>
            <button
              class="btn btn-confirm"
              type="button"
              @click="confirm"
              :disabled="!canConfirm"
            >
              Confirm
            </button>
          </div>
        </div>
      </div>
    </component>
  </div>
</template>

<style scoped>
.date-range-picker {
  position: relative;
}

.input-group {
  margin-bottom: 20px;
}
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.clear-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 0;
  margin-left: 4px;
  cursor: pointer;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}
.clear-btn:hover {
  color: #409eff;
}

.clear-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 0;
  margin-left: 4px;
  cursor: pointer;
  color: #a8abb2;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}
.clear-btn:hover {
  color: #409eff;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #303133;
}

.single-input {
  width: 100%;
  text-align: center;
  font-weight: 500;
}

.single-input::placeholder {
  text-align: center;
}

.date-input {
  padding: 12px 16px;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.date-input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.date-input.active {
  border-color: #409eff;
  background: #f0f9ff;
}

.picker-dropdown {
  background: white;
  border: 2px solid #dcdfe6;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
}

/* Non-teleport mode positioning */
.picker-dropdown:not(.teleported) {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  margin-top: 8px;
}

/* Teleport mode styles - Important modification */
.picker-dropdown.teleported {
  position: fixed;
  z-index: 2000;
  min-width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* Changed to hidden, let internal elements control scrolling */
}

/* Added: Content area */
.dropdown-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0; /* Important: Allow flex items to shrink */
}

/* Added: Fixed bottom area */
.dropdown-footer {
  flex-shrink: 0;
  background: white;
  border-top: 1px solid #f0f0f0;
}

/* Mobile full-screen styles */
@media (max-width: 768px) {
  .picker-dropdown.teleported {
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    max-width: 100vw !important;
    max-height: 100vh !important;
    min-width: 100vw !important;
    border-radius: 0 !important;
    border: none !important;
    overflow-y: auto;
  }

  .mobile-header {
    display: flex !important;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
    background: white;
    flex-shrink: 0;
  }

  .mobile-close-btn {
    background: none;
    border: none;
    padding: 4px;
    cursor: pointer;
    color: #606266;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .mobile-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .dropdown-content {
    padding: 0 16px;
  }

  .calendar-container {
    grid-template-columns: 1fr !important;
    gap: 20px;
    padding: 20px 0;
  }

  .dropdown-footer .quick-select {
    padding: 16px;
    background: #fafafa;
    justify-content: center;
  }

  .dropdown-footer .actions {
    padding: 16px;
    background: white;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  }
}

.mobile-header {
  display: none;
}

.picker-dropdown.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.calendar-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 20px;
}

.calendar {
  min-width: 280px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 0 4px;
}

.nav-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s ease;
  color: #606266;
}

.nav-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.month-year {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  padding: 8px 4px;
}

.days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s ease;
  position: relative;
  -webkit-tap-highlight-color: transparent;
}

@media (hover: hover) and (pointer: fine) {
  .day:hover:not(.disabled):not(.start-date):not(.end-date) {
    background: #ecf5ff;
  }
}

.day.other-month {
  color: #c0c4cc;
}

.day.disabled {
  color: #e4e7ed;
  cursor: not-allowed;
}

.day.today {
  background: #fdf6ec;
  border: 2px solid #e6a23c;
  font-weight: 600;
}

.day.start-date {
  background: #409eff !important;
  color: white !important;
  font-weight: 600;
}

.day.end-date {
  background: #409eff !important;
  color: white !important;
  font-weight: 600;
}

.day.in-range {
  background: rgba(64, 158, 255, 0.15) !important;
  color: #409eff !important;
}

.day.hover-range {
  background: rgba(64, 158, 255, 0.1) !important;
}

.quick-select {
  padding: 16px 20px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-btn {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s ease;
  -webkit-tap-highlight-color: transparent;
}

.quick-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.actions {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  -webkit-tap-highlight-color: transparent;
}

.btn-cancel {
  background: #f5f7fa;
  color: #606266;
}

.btn-cancel:hover {
  background: #ecf5ff;
  color: #409eff;
}

.btn-confirm {
  background: #409eff;
  color: white;
}

.btn-confirm:hover {
  background: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Large screen adaptation */
@media (min-width: 769px) {
  .calendar-container {
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  .picker-dropdown.teleported {
    min-width: 600px;
    max-width: 800px;
  }
}
</style>
