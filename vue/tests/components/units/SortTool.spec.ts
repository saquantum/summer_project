/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import SortTool from '@/components/search/SortTool.vue'

// Define types for testing
interface SortItem {
  prop: string
  order: 'ascending' | 'descending'
}

interface ColumnItem {
  prop: string
  label: string
}

describe('SortTool', () => {
  let wrapper: VueWrapper<any>
  const mockFetchTableData = vi.fn()

  const defaultColumns: ColumnItem[] = [
    { prop: 'name', label: 'Name' },
    { prop: 'date', label: 'Date' },
    { prop: 'status', label: 'Status' },
    { prop: 'category', label: 'Category' }
  ]

  const createWrapper = (props = {}) => {
    return mount(SortTool, {
      props: {
        multiSort: [] as SortItem[],
        columns: defaultColumns,
        fetchTableData: mockFetchTableData,
        ...props
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Component Rendering', () => {
    it('renders nothing when multiSort is empty', async () => {
      wrapper = createWrapper()
      await nextTick()

      expect(wrapper.find('.sort-status').exists()).toBe(false)
    })

    it('renders sort status when multiSort has items', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      expect(wrapper.find('.sort-status').exists()).toBe(true)
      expect(wrapper.text()).toContain('Current Sort:')
    })

    it('displays correct number of sort tags', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' },
        { prop: 'status', order: 'ascending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      const tags = wrapper.findAll('.el-tag')
      expect(tags).toHaveLength(3)
    })

    it('shows clear sort button when sorts are present', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      const clearButton = wrapper.find('.el-button')
      expect(clearButton.exists()).toBe(true)
      expect(clearButton.text()).toBe('Clear Sort')
    })
  })

  describe('Sort Tag Display', () => {
    it('displays correct column labels for sort tags', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      expect(wrapper.text()).toContain('Name')
      expect(wrapper.text()).toContain('Date')
    })

    it('handles missing column labels gracefully', async () => {
      const multiSort: SortItem[] = [
        { prop: 'nonexistent', order: 'ascending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      // Should still render the tag even if label is undefined
      const tags = wrapper.findAll('.el-tag')
      expect(tags).toHaveLength(1)
    })

    it('applies primary type to first sort item and info type to others', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      const tags = wrapper.findAll('.el-tag')

      // element plus use class rather than attribute for tag type
      expect(tags[0].classes('el-tag--primary')).toBe(true)
      expect(tags[0].classes('el-tag--info')).toBe(false)
      expect(tags[1].classes('el-tag--info')).toBe(true)
      expect(tags[1].classes('el-tag--primary')).toBe(false)
    })

    it('displays correct icons for ascending and descending order', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      const arrowUpIcon = wrapper.findComponent({ name: 'ArrowUp' })
      const arrowDownIcon = wrapper.findComponent({ name: 'ArrowDown' })

      expect(arrowUpIcon.exists()).toBe(true)
      expect(arrowDownIcon.exists()).toBe(true)
    })
  })

  describe('Sort Removal', () => {
    it('removes individual sort column when tag is closed', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      // 直接调用组件的删除方法，模拟用户点击关闭按钮的行为
      wrapper.vm.removeSortColumn('name')
      await nextTick()

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toEqual([{ prop: 'date', order: 'descending' }])
      expect(mockFetchTableData).toHaveBeenCalledTimes(1)
    })

    it('removes specific sort column correctly', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' },
        { prop: 'status', order: 'ascending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      // Remove the middle item
      wrapper.vm.removeSortColumn('date')
      await nextTick()

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toEqual([
        { prop: 'name', order: 'ascending' },
        { prop: 'status', order: 'ascending' }
      ])
      expect(mockFetchTableData).toHaveBeenCalledTimes(1)
    })

    it('does nothing when trying to remove non-existent sort column', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.removeSortColumn('nonexistent')
      await nextTick()

      // Should not emit update or call fetchTableData
      expect(wrapper.emitted('update:multiSort')).toBeFalsy()
      expect(mockFetchTableData).not.toHaveBeenCalled()
    })
  })

  describe('Clear All Sort', () => {
    it('clears all sorts when clear button is clicked', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      const clearButton = wrapper.find('.el-button')
      await clearButton.trigger('click')

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toEqual([])
      expect(mockFetchTableData).toHaveBeenCalledTimes(1)
    })

    it('calls clearAllSort method correctly', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.clearAllSort()
      await nextTick()

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toEqual([])
      expect(mockFetchTableData).toHaveBeenCalledTimes(1)
    })
  })

  describe('Helper Functions', () => {
    it('gets correct column label for existing prop', async () => {
      wrapper = createWrapper()
      await nextTick()

      expect(wrapper.vm.getColumnLabel('name')).toBe('Name')
      expect(wrapper.vm.getColumnLabel('date')).toBe('Date')
      expect(wrapper.vm.getColumnLabel('status')).toBe('Status')
      expect(wrapper.vm.getColumnLabel('category')).toBe('Category')
    })

    it('returns undefined for non-existent prop', async () => {
      wrapper = createWrapper()
      await nextTick()

      expect(wrapper.vm.getColumnLabel('nonexistent')).toBeUndefined()
    })
  })

  describe('Props Validation', () => {
    it('handles empty columns array', async () => {
      wrapper = createWrapper({ columns: [] })
      await nextTick()

      expect(wrapper.vm.getColumnLabel('name')).toBeUndefined()
    })
  })

  describe('Event Emission', () => {
    it('emits correct data structure on sort removal', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.removeSortColumn('name')
      await nextTick()

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(Array.isArray(emitted![0][0])).toBe(true)
      expect(emitted![0][0]).toEqual([{ prop: 'date', order: 'descending' }])
    })

    it('emits empty array on clear all', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.clearAllSort()
      await nextTick()

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(emitted![0][0]).toEqual([])
    })
  })

  describe('Function Calls', () => {
    it('calls fetchTableData after removing sort', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.removeSortColumn('name')
      await nextTick()

      expect(mockFetchTableData).toHaveBeenCalledTimes(1)
    })

    it('calls fetchTableData after clearing all sorts', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.clearAllSort()
      await nextTick()

      expect(mockFetchTableData).toHaveBeenCalledTimes(1)
    })

    it('does not call fetchTableData when removing non-existent sort', async () => {
      const multiSort: SortItem[] = [{ prop: 'name', order: 'ascending' }]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      wrapper.vm.removeSortColumn('nonexistent')
      await nextTick()

      expect(mockFetchTableData).not.toHaveBeenCalled()
    })
  })

  describe('UI Interactions', () => {
    it('supports multiple sequential operations', async () => {
      const multiSort: SortItem[] = [
        { prop: 'name', order: 'ascending' },
        { prop: 'date', order: 'descending' },
        { prop: 'status', order: 'ascending' }
      ]

      wrapper = createWrapper({ multiSort })
      await nextTick()

      // Remove one sort
      wrapper.vm.removeSortColumn('date')
      await nextTick()

      // Then clear all
      wrapper.vm.clearAllSort()
      await nextTick()

      const emitted = wrapper.emitted('update:multiSort')
      expect(emitted).toBeTruthy()
      expect(emitted).toHaveLength(2) // Two emissions
      expect(emitted![1][0]).toEqual([]) // Second emission should be empty array
      expect(mockFetchTableData).toHaveBeenCalledTimes(2)
    })
  })
})
