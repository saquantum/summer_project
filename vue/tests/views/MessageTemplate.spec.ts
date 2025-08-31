/* eslint-disable @typescript-eslint/no-explicit-any */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import { nextTick, ref } from 'vue'
import MessageTemplate from '@/views/admin/MessageTemplate.vue'
import {
  adminDeleteTemplateByIdService,
  adminGetTemplateByIdService,
  adminGetTemplateByTypesService,
  adminInsertTemplateService,
  adminUpdateTemplateByTypesService
} from '@/api/admin'

// Mock router
const routeMock = {
  query: { id: 'template1' }
}

vi.mock('vue-router', async (importOriginal) => {
  const actual = (await importOriginal()) as Record<string, unknown>
  return {
    ...actual,
    useRoute: () => routeMock
  }
})

// Mock the API services
vi.mock('@/api/admin', () => ({
  adminDeleteTemplateByIdService: vi.fn(),
  adminGetTemplateByIdService: vi.fn(),
  adminGetTemplateByTypesService: vi.fn(),
  adminInsertTemplateService: vi.fn(),
  adminUpdateTemplateByTypesService: vi.fn()
}))

// Mock Element Plus
vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<typeof import('element-plus')>()
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn()
    }
  }
})

// Mock TiptapEditor component
vi.mock('@/components/TiptapEditor.vue', () => ({
  default: {
    name: 'TiptapEditor',
    template:
      '<div data-testid="tiptap-editor"><textarea v-model="content" /></div>',
    props: ['content'],
    emits: ['update:content'],
    expose: ['compiledHTML', 'plainText', 'uploadAllImagesAndGetFinalContent'],
    setup() {
      const compiledHTML = ref('<p>Test HTML content</p>')
      const plainText = ref('Test plain content')

      const uploadAllImagesAndGetFinalContent = vi
        .fn()
        .mockResolvedValue('<p>Final content with images</p>')

      return {
        compiledHTML,
        plainText,
        uploadAllImagesAndGetFinalContent
      }
    }
  }
}))

// Mock ConfirmDialog component
vi.mock('@/components/dialog/ConfirmDialog.vue', () => ({
  default: {
    name: 'ConfirmDialog',
    template: `
      <div data-testid="confirm-dialog" v-if="modelValue">
        <div>{{ title }}</div>
        <div>{{ content }}</div>
        <button data-test="confirm" @click="$emit('confirm')">Confirm</button>
        <button data-test="cancel" @click="$emit('cancel')">Cancel</button>
      </div>
    `,
    props: ['modelValue', 'title', 'content', 'countdownDuration'],
    emits: ['confirm', 'cancel', 'update:modelValue']
  }
}))

// Mock template data
const mockTemplate = {
  id: 1,
  assetTypeId: 'type_001',
  warningType: 'Rain',
  severity: 'YELLOW',
  contactChannel: 'Email',
  title: 'Test Template',
  body: '<p>Test template body</p>'
}

// Mock asset store
const mockAssetStore = {
  typeOptions: [
    { label: 'Asset Type 1', value: 'type_001' },
    { label: 'Asset Type 2', value: 'type_002' }
  ]
}

// Mock useAssetStore
vi.mock('@/stores', () => ({
  useAssetStore: vi.fn(() => mockAssetStore)
}))

describe('MessageTemplate', () => {
  beforeEach(() => {
    // Reset mocks
    vi.mocked(adminGetTemplateByIdService).mockResolvedValue({
      data: mockTemplate
    } as any)

    vi.mocked(adminGetTemplateByTypesService).mockResolvedValue({
      data: [mockTemplate]
    } as any)

    vi.mocked(adminUpdateTemplateByTypesService).mockResolvedValue({
      data: undefined
    } as any)

    vi.mocked(adminInsertTemplateService).mockResolvedValue({
      data: undefined
    } as any)

    vi.mocked(adminDeleteTemplateByIdService).mockResolvedValue({
      data: undefined
    } as any)

    // Reset route mock
    routeMock.query = { id: 'template1' }
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly with all form elements', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Check for allowed variables list
    expect(wrapper.text()).toContain('Allowed variable:')
    expect(wrapper.text()).toContain('asset_model')
    expect(wrapper.text()).toContain('contact_name')
    expect(wrapper.text()).toContain('post_town')

    // Check for select dropdowns
    const selects = wrapper.findAllComponents({ name: 'ElSelect' })
    expect(selects).toHaveLength(4) // warning type, contact channel, asset type, severity

    // Check for title input
    const titleInput = wrapper.findComponent({ name: 'ElInput' })
    expect(titleInput.exists()).toBe(true)

    // Check for TiptapEditor
    expect(wrapper.find('[data-testid="tiptap-editor"]').exists()).toBe(true)

    // Check for preview section
    expect(wrapper.find('.preview').exists()).toBe(true)

    // Check for submit and delete buttons
    const buttons = wrapper.findAllComponents({ name: 'ElButton' })
    expect(buttons.length).toBeGreaterThanOrEqual(2)
  })

  it('loads existing template when id is provided in route query', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    expect(adminGetTemplateByIdService).toHaveBeenCalledWith('template1')
    expect((wrapper.vm as any).form).toEqual(mockTemplate)
    expect((wrapper.vm as any).hasTemplate).toBe(true)
  })

  it('loads template by types when no id is provided', async () => {
    routeMock.query = {} as any

    mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    expect(adminGetTemplateByTypesService).toHaveBeenCalledWith(
      'type_001',
      'Rain',
      'YELLOW',
      'email'
    )
  })

  it('sets placeholder when no template exists', async () => {
    vi.mocked(adminGetTemplateByTypesService).mockResolvedValue({
      data: []
    } as any)

    routeMock.query = {} as any

    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    expect((wrapper.vm as any).hasTemplate).toBe(false)
    expect((wrapper.vm as any).form.body).toBe(
      "You haven't set template for this"
    )
  })

  it('updates template successfully for email channel', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Ensure email channel (lowercase as expected by component)
    ;(wrapper.vm as any).form.contactChannel = 'email'

    // Make the editor return valid content
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = 'Valid content'
    editorRef.uploadAllImagesAndGetFinalContent = vi
      .fn()
      .mockResolvedValue('<p>Valid content</p>')

    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')
    await flushPromises()

    expect(editorRef.uploadAllImagesAndGetFinalContent).toHaveBeenCalled()
    expect(adminUpdateTemplateByTypesService).toHaveBeenCalledWith(
      expect.objectContaining({
        body: '<p>Valid content</p>'
      })
    )
    expect(ElMessage.success).toHaveBeenCalledWith(
      'Template updated successfully'
    )
  })

  it('inserts new template when hasTemplate is false', async () => {
    vi.mocked(adminGetTemplateByTypesService).mockResolvedValue({
      data: []
    } as any)

    routeMock.query = {} as any

    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Set valid content
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = 'New template content'
    editorRef.uploadAllImagesAndGetFinalContent = vi
      .fn()
      .mockResolvedValue('<p>New template content</p>')

    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')
    await flushPromises()

    expect(adminInsertTemplateService).toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith(
      'Template updated successfully'
    )
  })

  it('shows confirmation dialog when submitting empty content', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Set empty content
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = "You haven't set template for this"

    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')

    expect((wrapper.vm as any).confirmDialogVisible).toBe(true)

    // Find and verify the dialog
    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    expect(dialog.exists()).toBe(true)
    expect(dialog.text()).toContain(
      "You didn't write anything. Are you sure you want to submit?"
    )
  })

  it('handles different contact channels correctly', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Test phone channel (should use plain text)
    ;(wrapper.vm as any).form.contactChannel = 'Phone'
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = 'Phone message content'

    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')
    await flushPromises()

    expect(adminUpdateTemplateByTypesService).toHaveBeenCalledWith(
      expect.objectContaining({
        body: 'Phone message content'
      })
    )
  })

  it('handles image upload error', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Ensure email channel to trigger image upload (lowercase)
    ;(wrapper.vm as any).form.contactChannel = 'email'

    // Mock image upload failure
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = 'Valid content'
    editorRef.uploadAllImagesAndGetFinalContent = vi
      .fn()
      .mockRejectedValue(new Error('Upload failed'))

    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')
    await flushPromises()

    expect(ElMessage.error).toHaveBeenCalledWith('Fail to upload images')
  })

  it('triggers delete functionality', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    const deleteButton = wrapper.findAllComponents({ name: 'ElButton' })[1]
    await deleteButton.trigger('click')

    expect((wrapper.vm as any).dialogVisible).toBe(true)
    expect((wrapper.vm as any).deleteId).toEqual([mockTemplate.id])
  })

  it('handles delete confirmation', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Trigger delete
    ;(wrapper.vm as any).dialogVisible = true
    ;(wrapper.vm as any).deleteId = [mockTemplate.id]

    await nextTick()

    const confirmDialog = wrapper.findComponent({ name: 'ConfirmDialog' })
    const confirmButton = confirmDialog.find('[data-test="confirm"]')
    await confirmButton.trigger('click')
    await flushPromises()

    expect(adminDeleteTemplateByIdService).toHaveBeenCalledWith([
      mockTemplate.id
    ])
    expect((wrapper.vm as any).dialogVisible).toBe(false)
  })

  it('watches form changes and fetches new template when dropdown values change', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Clear the mock calls from initial loading
    vi.clearAllMocks()
    vi.mocked(adminGetTemplateByTypesService).mockResolvedValue({
      data: [{ ...mockTemplate, warningType: 'Snow' }]
    } as any)

    // Reset the contact channel and change warning type
    ;(wrapper.vm as any).form.contactChannel = 'Email'
    ;(wrapper.vm as any).form.warningType = 'Snow'
    await nextTick()
    await flushPromises()

    expect(adminGetTemplateByTypesService).toHaveBeenCalledWith(
      'type_001',
      'Snow',
      'YELLOW',
      'Email'
    )
  })

  it('sets dirty state when content changes', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    expect((wrapper.vm as any).isDirty).toBe(false)

    // Change title to trigger dirty state
    ;(wrapper.vm as any).form.title = 'Updated title'
    await nextTick()

    expect((wrapper.vm as any).isDirty).toBe(true)
  })

  it('handles API errors gracefully', async () => {
    const consoleErrorSpy = vi
      .spyOn(console, 'error')
      .mockImplementation(() => {})

    vi.mocked(adminUpdateTemplateByTypesService).mockRejectedValue(
      new Error('API Error')
    )

    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = 'Valid content'

    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')
    await flushPromises()

    expect(ElMessage.error).toHaveBeenCalledWith('Failed to update template')
    expect(consoleErrorSpy).toHaveBeenCalled()

    consoleErrorSpy.mockRestore()
  })

  it('computes compiledHTML and plainText correctly', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    expect((wrapper.vm as any).compiledHTML).toBe('<p>Test HTML content</p>')
    expect((wrapper.vm as any).plainText).toBe('Test plain content')
  })

  it('manages beforeunload event listener correctly', async () => {
    const addEventListenerSpy = vi.spyOn(window, 'addEventListener')
    const removeEventListenerSpy = vi.spyOn(window, 'removeEventListener')

    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    expect(addEventListenerSpy).toHaveBeenCalledWith(
      'beforeunload',
      expect.any(Function)
    )

    wrapper.unmount()

    expect(removeEventListenerSpy).toHaveBeenCalledWith(
      'beforeunload',
      expect.any(Function)
    )

    addEventListenerSpy.mockRestore()
    removeEventListenerSpy.mockRestore()
  })

  it('handles beforeunload event when there are unsaved changes', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Set dirty state
    ;(wrapper.vm as any).isDirty = true

    const mockEvent = {
      preventDefault: vi.fn(),
      returnValue: ''
    } as any

    ;(wrapper.vm as any).handleBeforeUnload(mockEvent)

    expect(mockEvent.preventDefault).toHaveBeenCalled()
    expect(mockEvent.returnValue).toBe(
      'You have unsaved changes. Are you sure you want to leave?'
    )
  })

  it('does not prevent beforeunload when no unsaved changes', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Ensure clean state
    ;(wrapper.vm as any).isDirty = false

    const mockEvent = {
      preventDefault: vi.fn(),
      returnValue: ''
    } as any

    ;(wrapper.vm as any).handleBeforeUnload(mockEvent)

    expect(mockEvent.preventDefault).not.toHaveBeenCalled()
    expect(mockEvent.returnValue).toBe('')
  })

  it('submits empty content after confirmation', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Set empty content to trigger confirmation dialog
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = "You haven't set template for this"

    // Trigger submit which should show confirmation
    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')

    expect((wrapper.vm as any).confirmDialogVisible).toBe(true)

    // Click the submit button in the confirmation dialog
    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    const dialogSubmitButton = dialog.findAllComponents({ name: 'ElButton' })[1] // Second button is Submit
    await dialogSubmitButton.trigger('click')
    await flushPromises()

    expect(adminUpdateTemplateByTypesService).toHaveBeenCalled()
  })

  it('cancels empty content submission', async () => {
    const wrapper = mount(MessageTemplate, { attachTo: document.body })
    await nextTick()
    await flushPromises()

    // Set empty content to trigger confirmation dialog
    const editorRef = (wrapper.vm as any).editorRef
    editorRef.plainText = "You haven't set template for this"

    // Trigger submit which should show confirmation
    const submitButton = wrapper.findAllComponents({ name: 'ElButton' })[0]
    await submitButton.trigger('click')

    expect((wrapper.vm as any).confirmDialogVisible).toBe(true)

    // Click the cancel button in the confirmation dialog
    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    const dialogCancelButton = dialog.findAllComponents({ name: 'ElButton' })[0] // First button is Cancel
    await dialogCancelButton.trigger('click')

    expect((wrapper.vm as any).confirmDialogVisible).toBe(false)
    expect(adminUpdateTemplateByTypesService).not.toHaveBeenCalled()
  })
})
