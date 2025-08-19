import { flushPromises, mount } from '@vue/test-utils'
import ConfirmDialog from '@/components/dialog/ConfirmDialog.vue'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { config } from '@vue/test-utils'
import { nextTick } from 'vue'

config.global.config.warnHandler = () => {}

// Mock Element Plus Message
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn()
    }
  }
})

describe('ConfirmDialog.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'warn').mockImplementation(() => {})
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Complete User Flow', () => {
    it('displays dialog with correct content and handles user actions', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          title: 'Delete Asset',
          content: 'Are you sure you want to delete this asset?',
          confirmText: 'Delete',
          cancelText: 'Keep',
          countdownDuration: 3
        }
      })
      await nextTick()

      // User sees dialog with correct content
      expect(wrapper.text()).toContain('Delete Asset')
      expect(wrapper.text()).toContain(
        'Are you sure you want to delete this asset?'
      )

      // User sees both buttons
      const confirmButton = wrapper.find('[data-test="confirm"]')
      const cancelButton = wrapper.find('[data-test="cancel"]')

      expect(confirmButton.exists()).toBe(true)
      expect(cancelButton.exists()).toBe(true)
      expect(confirmButton.text()).toContain('Delete')
      expect(cancelButton.text()).toBe('Keep')

      // User can cancel
      await cancelButton.trigger('click')
      expect(wrapper.emitted('cancel')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
    })

    it('allows confirmation after countdown completes', async () => {
      vi.useFakeTimers()

      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: false,
          title: 'Confirm Action',
          confirmText: 'delete',
          countdownDuration: 5
        }
      })
      await nextTick()

      await wrapper.setProps({ modelValue: true })
      await nextTick()

      const confirmButton = wrapper.find('[data-test="confirm"]')

      vi.advanceTimersByTime(5000)
      await nextTick()
      await flushPromises()

      // User can confirm
      await confirmButton.trigger('click')
      await nextTick()

      expect(wrapper.emitted('confirm')).toBeTruthy()

      vi.useRealTimers()
    })

    it('shows countdown when duration is greater than zero', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          countdownDuration: 5
        }
      })
      await nextTick()

      const confirmButton = wrapper.find('[data-test="confirm"]')

      // Should show countdown in button text
      expect(confirmButton.text()).toContain('Confirm (5)')
      expect(confirmButton.attributes('disabled')).toBe('')
    })
  })

  describe('Customization Features', () => {
    it('uses default text when no custom text provided', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          countdownDuration: 0
        }
      })
      await nextTick()

      const confirmButton = wrapper.find('[data-test="confirm"]')
      const cancelButton = wrapper.find('[data-test="cancel"]')

      expect(confirmButton.text()).toBe('Confirm (0)')
      expect(cancelButton.text()).toBe('Cancel')
    })

    it('displays custom slot content instead of content prop', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          content: 'This should not be shown',
          countdownDuration: 0
        },
        slots: {
          default:
            '<div data-test="custom-content">Custom slot content with <strong>HTML</strong></div>'
        }
      })
      await nextTick()

      // Should show slot content, not prop content
      expect(wrapper.find('[data-test="custom-content"]').exists()).toBe(true)
      expect(wrapper.text()).toContain('Custom slot content with HTML')
      expect(wrapper.text()).not.toContain('This should not be shown')
    })

    it('displays custom button texts correctly', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          confirmText: 'Yes, Delete',
          cancelText: 'No, Keep',
          countdownDuration: 0
        }
      })
      await nextTick()

      const confirmButton = wrapper.find('[data-test="confirm"]')
      const cancelButton = wrapper.find('[data-test="cancel"]')

      expect(confirmButton.text()).toBe('Yes, Delete (0)')
      expect(cancelButton.text()).toBe('No, Keep')
    })

    it('displays custom title correctly', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          title: 'Warning: Destructive Action',
          countdownDuration: 0
        }
      })
      await nextTick()

      expect(wrapper.text()).toContain('Warning: Destructive Action')
    })
  })

  describe('Dialog Visibility Management', () => {
    it('opens and closes dialog correctly via modelValue', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: false,
          countdownDuration: 0
        }
      })
      await nextTick()

      // Dialog should be closed initially
      expect(wrapper.find('[data-test="confirm"]').exists()).toBe(false)

      // Open dialog
      await wrapper.setProps({ modelValue: true })
      await nextTick()

      const confirmButton = wrapper.find('[data-test="confirm"]')
      expect(confirmButton.exists()).toBe(true)

      // Close dialog via cancel
      const cancelButton = wrapper.find('[data-test="cancel"]')
      await cancelButton.trigger('click')

      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      const events = wrapper.emitted('update:modelValue')!
      expect(events[events.length - 1]).toEqual([false])
    })
  })

  describe('Edge Cases', () => {
    it('handles missing optional props gracefully', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true
          // All other props are optional
        }
      })
      await nextTick()

      // Should work with default values
      expect(wrapper.find('[data-test="confirm"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="cancel"]').exists()).toBe(true)

      // Default countdown should be 5 seconds
      const confirmButton = wrapper.find('[data-test="confirm"]')
      expect(confirmButton.text()).toContain('Confirm (5)')
    })

    it('prevents confirm action when countdown is active', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          countdownDuration: 3
        }
      })
      await nextTick()

      const confirmButton = wrapper.find('[data-test="confirm"]')

      // Button should be disabled
      expect(confirmButton.attributes('disabled')).toBe('')

      // Clicking should not emit confirm event (browser will prevent click)
      await confirmButton.trigger('click')
      expect(wrapper.emitted('confirm')).toBeFalsy()
    })

    it('handles rapid prop changes gracefully', async () => {
      const wrapper = mount(ConfirmDialog, {
        props: {
          modelValue: true,
          countdownDuration: 5
        }
      })
      await nextTick()

      // Rapidly change props
      await wrapper.setProps({ countdownDuration: 3 })
      await wrapper.setProps({ countdownDuration: 1 })
      await wrapper.setProps({ countdownDuration: 0 })
      await nextTick()

      // Should still work correctly
      const confirmButton = wrapper.find('[data-test="confirm"]')
      expect(confirmButton.exists()).toBe(true)
    })
  })
})
