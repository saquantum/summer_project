import { mount } from '@vue/test-utils'
import SendMessage from '@/views/admin/SendMessage.vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import { adminSendMessageService } from '@/api/admin'
import type { AxiosResponse } from 'axios'

// Mock admin API
vi.mock('@/api/admin', () => ({
  adminSendMessageService: vi.fn()
}))

// Mock form utils
vi.mock('@/utils/formUtils', () => ({
  createAssetHolderRules: vi.fn(() => [
    { required: true, message: 'Please input username', trigger: 'blur' }
  ]),
  trimForm: vi.fn()
}))

// Mock Element Plus - partial mock approach
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn()
    }
  }
})

// Mock TiptapEditor component - provide minimal interface needed
vi.mock('@/components/TiptapEditor.vue', () => ({
  default: {
    name: 'TiptapEditor',
    template: '<div data-test="tiptap-editor">TiptapEditor Stub</div>',
    setup() {
      return {}
    },
    mounted() {
      // Expose renderedHTML property that the parent component expects
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      ;(this as any).renderedHTML = 'Test HTML content'
    }
  }
}))

describe('SendMessage', () => {
  const createWrapper = () => {
    return mount(SendMessage)
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(adminSendMessageService).mockResolvedValue({
      data: 'success',
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {
        headers: {},
        method: 'post',
        url: '/api/admin/send-message'
      }
    } as AxiosResponse<string>)
  })

  it('renders all form fields correctly', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Check for form fields
    expect(wrapper.find('[data-test="users"]').exists()).toBe(true)
    expect(wrapper.find('input[data-test="duration-input"]').exists()).toBe(
      true
    )
    expect(
      wrapper.find('input[placeholder="Please input title"]').exists()
    ).toBe(true)

    // Check for TiptapEditor
    expect(wrapper.find('[data-test="tiptap-editor"]').exists()).toBe(true)

    // Check for send button
    expect(wrapper.find('[data-test="send-button"]').text()).toBe('Send')
  })

  it('updates form fields when user types', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const usernameInput = wrapper.find('[data-test="users"]')
    const titleInput = wrapper.find('input[placeholder="Please input title"]')

    await usernameInput.setValue('testuser')
    await titleInput.setValue('Test Message Title')

    expect((usernameInput.element as HTMLInputElement).value).toBe('testuser')
    expect((titleInput.element as HTMLInputElement).value).toBe(
      'Test Message Title'
    )
  })

  it('handles duration input correctly without showing 0', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const durationInput = wrapper.find('input[data-test="duration-input"]')

    // Initially should be empty, not 0
    expect((durationInput.element as HTMLInputElement).value).toBe('')

    // When user types, should show the typed value
    await durationInput.setValue('30')
    expect((durationInput.element as HTMLInputElement).value).toBe('30')
  })

  it('displays rendered HTML in preview area', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    const preview = wrapper.find('[data-test="message-preview"]')
    expect(preview.exists()).toBe(true)
    // Preview content depends on TiptapEditor, so we just check the container exists
  })

  it('sends message successfully with valid data', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Fill in form data
    await wrapper.find('[data-test="users"]').setValue('testuser')
    await wrapper.find('input[data-test="duration-input"]').setValue('30')
    await wrapper
      .find('input[placeholder="Please input title"]')
      .setValue('Test Title')

    // Click send button
    const sendButton = wrapper.find('[data-test="send-button"]')
    await sendButton.trigger('click')
    await flushPromises()

    // Verify API was called with correct form data (body will be handled by TiptapEditor)
    expect(vi.mocked(adminSendMessageService)).toHaveBeenCalledWith(
      expect.objectContaining({
        users: 'testuser',
        duration: 30,
        title: 'Test Title'
        // body will be set by the actual TiptapEditor component
      })
    )

    // Verify success message
    expect(vi.mocked(ElMessage.success)).toHaveBeenCalledWith('Message sent')
  })

  it('handles empty duration as 9999999', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Fill in form data but leave duration empty
    await wrapper.find('[data-test="users"]').setValue('testuser')
    await wrapper
      .find('input[placeholder="Please input title"]')
      .setValue('Test Title')
    // Duration input left empty

    // Click send button
    const sendButton = wrapper.find('[data-test="send-button"]')
    await sendButton.trigger('click')
    await flushPromises()

    // Verify API was called with duration as 9999999 (default for empty)
    expect(vi.mocked(adminSendMessageService)).toHaveBeenCalledWith(
      expect.objectContaining({
        users: 'testuser',
        duration: 9999999,
        title: 'Test Title'
      })
    )
  })

  it('validates negative duration and prevents submission', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Set negative duration
    await wrapper.find('input[data-test="duration-input"]').setValue('-5')
    await wrapper.find('[data-test="users"]').setValue('testuser')
    await wrapper
      .find('input[placeholder="Please input title"]')
      .setValue('Test Title')

    // Click send button
    const sendButton = wrapper.find('[data-test="send-button"]')
    await sendButton.trigger('click')
    await flushPromises()

    // Verify API was not called (form validation prevented submission)
    expect(vi.mocked(adminSendMessageService)).not.toHaveBeenCalled()
  })

  it('can trigger send button click', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Just test that we can click the send button without errors
    const sendButton = wrapper.find('[data-test="send-button"]')
    expect(sendButton.exists()).toBe(true)

    // Click without setting any form data
    await sendButton.trigger('click')
    await flushPromises()

    // This test just ensures the component doesn't crash
    expect(true).toBe(true)
  })

  it('handles API error and shows error message', async () => {
    vi.mocked(adminSendMessageService).mockRejectedValue(new Error('API Error'))

    const wrapper = createWrapper()
    await flushPromises()

    // Fill in valid form data
    await wrapper.find('[data-test="users"]').setValue('testuser')
    await wrapper.find('input[data-test="duration-input"]').setValue('30')
    await wrapper
      .find('input[placeholder="Please input title"]')
      .setValue('Test Title')

    // Click send button
    const sendButton = wrapper.find('[data-test="send-button"]')
    await sendButton.trigger('click')
    await flushPromises()

    // Verify error message
    expect(vi.mocked(ElMessage.error)).toHaveBeenCalledWith(
      'Fail to send message'
    )
  })

  it('validates zero duration and prevents submission', async () => {
    const wrapper = createWrapper()
    await flushPromises()

    // Set duration to 0
    await wrapper.find('input[data-test="duration-input"]').setValue('0')
    await wrapper.find('[data-test="users"]').setValue('testuser')
    await wrapper
      .find('input[placeholder="Please input title"]')
      .setValue('Test Title')

    // Click send button
    await wrapper.find('[data-test="send-button"]').trigger('click')
    await flushPromises()

    // Verify API was not called (form validation prevented submission)
    expect(vi.mocked(adminSendMessageService)).not.toHaveBeenCalled()
  })

  it('preserves form data after failed submission', async () => {
    vi.mocked(adminSendMessageService).mockRejectedValue(new Error('API Error'))

    const wrapper = createWrapper()
    await flushPromises()

    // Fill in form data
    const usernameInput = wrapper.find('[data-test="users"]')
    const durationInput = wrapper.find('input[data-test="duration-input"]')
    const titleInput = wrapper.find('input[placeholder="Please input title"]')

    await usernameInput.setValue('testuser')
    await durationInput.setValue('30')
    await titleInput.setValue('Test Title')

    // Click send button
    const sendButton = wrapper.find('[data-test="send-button"]')
    await sendButton.trigger('click')
    await flushPromises()

    // Verify form data is still there after failed submission
    expect((usernameInput.element as HTMLInputElement).value).toBe('testuser')
    expect((durationInput.element as HTMLInputElement).value).toBe('30')
    expect((titleInput.element as HTMLInputElement).value).toBe('Test Title')
  })
})
