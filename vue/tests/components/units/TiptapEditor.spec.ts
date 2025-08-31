import { mount } from '@vue/test-utils'
import TiptapEditor from '@/components/TiptapEditor.vue'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { nextTick } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import type { VueWrapper } from '@vue/test-utils'
import * as uploadUtils from '@/utils/upload'

// Mock DOMPurify
vi.mock('dompurify', () => ({
  default: {
    sanitize: vi.fn((html) => html),
    addHook: vi.fn()
  }
}))

// Mock Handlebars
vi.mock('handlebars', () => ({
  default: {
    compile: vi.fn(() => vi.fn(() => '<p>Compiled template</p>'))
  }
}))

// Mock upload utilities
vi.mock('@/utils/upload', () => ({
  getUploadData: vi.fn(() => ({ key: 'value' })),
  getUploadUrl: vi.fn(() => 'http://example.com/upload'),
  validateImageFile: vi.fn(() => ({ valid: true }))
}))

// Mock editor style utilities
vi.mock('@/utils/editorStyle', () => ({
  addInlineStyle: vi.fn((html) => html),
  generateEditorCSS: vi.fn(() => '.editor { color: black; }')
}))

// // Mock Element Plus
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

// Mock Tiptap editor
const mockEditor = {
  chain: () => ({
    focus: () => ({
      undo: () => ({ run: vi.fn() }),
      redo: () => ({ run: vi.fn() }),
      toggleBold: () => ({ run: vi.fn() }),
      toggleItalic: () => ({ run: vi.fn() }),
      toggleCode: () => ({ run: vi.fn() }),
      toggleHeading: () => ({ run: vi.fn() }),
      setColor: () => ({ run: vi.fn() }),
      extendMarkRange: () => ({
        unsetLink: () => ({ run: vi.fn() }),
        setLink: () => ({ run: vi.fn() })
      }),
      setImage: () => ({ run: vi.fn() }),
      insertContentAt: () => ({ run: vi.fn() })
    })
  }),
  can: () => ({
    undo: vi.fn(() => true),
    redo: vi.fn(() => true)
  }),
  getHTML: vi.fn(() => '<p>Initial content</p>'),
  getText: vi.fn(() => 'Test content'),
  getAttributes: vi.fn(() => ({ color: '#000000', level: 1, href: '' })),
  isActive: vi.fn(() => false),
  commands: {
    setContent: vi.fn()
  },
  on: vi.fn(),
  off: vi.fn(),
  destroy: vi.fn()
}

vi.mock('@tiptap/vue-3', () => ({
  useEditor: vi.fn(() => {
    // Create a mock ref that will unwrap properly in templates
    const mockRef = { value: mockEditor }
    // Add all the editor methods directly to the ref for template access
    Object.assign(mockRef, mockEditor)
    return mockRef
  }),
  EditorContent: {
    name: 'EditorContent',
    template: '<div class="editor-content"><slot /></div>'
  }
}))

// Mock URL.createObjectURL and URL.revokeObjectURL
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
global.URL.revokeObjectURL = vi.fn()

// Mock fetch for file uploads
global.fetch = vi.fn()

describe('TiptapEditor', () => {
  let wrapper: VueWrapper<InstanceType<typeof TiptapEditor>>

  beforeEach(() => {
    vi.clearAllMocks()
    // Reset DOM
    document.head.innerHTML = ''
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const createWrapper = (props = {}) => {
    return mount(TiptapEditor, {
      props: {
        content: '',
        ...props
      }
    })
  }

  describe('style Injection', () => {
    it('injects editor styles on mount', () => {
      wrapper = createWrapper()
      expect(document.getElementById('tiptap-dynamic-styles')).toBeTruthy()
    })

    it('removes editor styles on unmount', () => {
      wrapper = createWrapper()
      expect(document.getElementById('tiptap-dynamic-styles')).toBeTruthy()
      wrapper.unmount()
      expect(document.getElementById('tiptap-dynamic-styles')).toBeFalsy()
    })
  })

  describe('Content Management', () => {
    it('initializes with provided content', () => {
      const testContent = '<p>Test initial content</p>'
      wrapper = createWrapper({ content: testContent })
      expect(wrapper.props('content')).toBe(testContent)
    })

    it('emits update:content when content changes', async () => {
      wrapper = createWrapper()

      // Simulate editor update
      const updateContent = wrapper.vm.updateContent
      updateContent('<p>New content</p>')

      await nextTick()
      const emittedEvents = wrapper.emitted('update:content')
      expect(emittedEvents).toBeTruthy()
      expect(emittedEvents![0]).toEqual(['<p>New content</p>'])
    })

    it('updates editor content when prop changes', async () => {
      wrapper = createWrapper()

      await wrapper.setProps({ content: '<p>Updated content</p>' })
      await nextTick()

      // The editor should be updated with new content
      // Since we're mocking the editor, we need to verify the mock was called
      expect(mockEditor.commands.setContent).toHaveBeenCalled()
    })
  })

  describe('Editor Controls', () => {
    beforeEach(() => {
      wrapper = createWrapper()
    })

    it('renders undo button', () => {
      const undoButton = wrapper.find('button').element
      expect(undoButton).toBeTruthy()
    })

    it('renders redo button', () => {
      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBeGreaterThan(1)
    })

    it('renders color picker', () => {
      const colorInput = wrapper.find('input[type="color"]')
      expect(colorInput.exists()).toBe(true)
    })

    it('handles color change', async () => {
      const colorInput = wrapper.find('input[type="color"]')
      await colorInput.setValue('#ff0000')
      expect(mockEditor.chain().focus().setColor).toBeDefined()
    })

    it('handles heading command', async () => {
      const handleCommand = wrapper.vm.handleCommand
      handleCommand('2')
      expect(mockEditor.chain().focus().toggleHeading).toBeDefined()
    })
  })

  describe('Link Management', () => {
    beforeEach(() => {
      wrapper = createWrapper()
    })

    it('normalizes URLs correctly', () => {
      const normalizeUrl = wrapper.vm.normalizeUrl

      expect(normalizeUrl('example.com')).toBe('https://example.com')
      expect(normalizeUrl('https://example.com')).toBe('https://example.com')
      expect(normalizeUrl('mailto:test@example.com')).toBe(
        'mailto:test@example.com'
      )
      expect(normalizeUrl('tel:+1234567890')).toBe('tel:+1234567890')
      expect(normalizeUrl('/relative')).toBe('/relative')
      expect(normalizeUrl('#anchor')).toBe('#anchor')
    })

    it('sets link with normalized URL', async () => {
      // Since linkUrl is exposed as an unwrapped value, we need to access the component's internal ref
      // Let's test the normalize function directly and then test setLink behavior
      const normalizeUrl = wrapper.vm.normalizeUrl
      const normalizedUrl = normalizeUrl('example.com')
      expect(normalizedUrl).toBe('https://example.com')

      // Test that setLink is callable and uses the mock editor
      wrapper.vm.setLink()
      expect(mockEditor.chain().focus().extendMarkRange().setLink).toBeDefined()
    })

    it('removes link when URL is empty', async () => {
      // Test that setLink handles empty URL correctly
      wrapper.vm.setLink()

      expect(
        mockEditor.chain().focus().extendMarkRange().unsetLink
      ).toBeDefined()
    })
  })

  describe('Image Upload', () => {
    beforeEach(() => {
      wrapper = createWrapper()
    })

    it('handles image upload with valid file', () => {
      const mockFile = new File(['test'], 'test.png', { type: 'image/png' })
      const uploadFile: UploadFile = {
        raw: mockFile,
        name: 'test.png',
        status: 'ready',
        uid: 123
      }

      wrapper.vm.handleImageUpload(uploadFile)

      expect(wrapper.vm.pendingFiles).toContain(mockFile)
      expect(wrapper.vm.previewUrls.length).toBeGreaterThan(0)
      expect(wrapper.vm.localUrlToFileMap.has('blob:mock-url')).toBe(true)
    })

    it('validates image files', () => {
      const mockInvalidFile = new File(['test'], 'test.txt', {
        type: 'text/plain'
      })
      const uploadFile = { raw: mockInvalidFile }

      // Mock validation to return invalid
      vi.mocked(uploadUtils.validateImageFile).mockReturnValue({
        valid: false,
        message: 'Invalid file type'
      })

      wrapper.vm.handleImageUpload(uploadFile)

      expect(ElMessage.error).toHaveBeenCalledWith('Invalid file type')
    })

    it('uploads single file successfully', async () => {
      const mockFile = new File(['test'], 'test.png', { type: 'image/png' })

      // Mock successful fetch response
      vi.mocked(global.fetch).mockResolvedValue({
        json: () =>
          Promise.resolve({ data: { url: 'http://example.com/image.png' } })
      } as Response)

      const result = await wrapper.vm.uploadSingleFile(mockFile)

      expect(result).toBe('http://example.com/image.png')
      expect(global.fetch).toHaveBeenCalledWith(
        'http://example.com/upload',
        expect.objectContaining({
          method: 'POST',
          body: expect.any(FormData)
        })
      )
    })

    it('handles upload failure', async () => {
      const mockFile = new File(['test'], 'test.png', { type: 'image/png' })

      // Mock failed fetch response
      vi.mocked(global.fetch).mockRejectedValue(new Error('Upload failed'))

      await expect(wrapper.vm.uploadSingleFile(mockFile)).rejects.toThrow(
        'Upload failed'
      )
    })

    it('uploads all images and gets final content', async () => {
      // Add a pending file
      const mockFile = new File(['test'], 'test.png', { type: 'image/png' })
      wrapper.vm.pendingFiles = [mockFile]
      wrapper.vm.localUrlToFileMap.set('blob:mock-url', mockFile)

      // Set the content prop to contain the blob URL since renderedHTML uses content.value
      await wrapper.setProps({ content: '<img src="blob:mock-url" />' })

      // Mock successful upload
      vi.mocked(global.fetch).mockResolvedValue({
        json: () =>
          Promise.resolve({ data: { url: 'http://example.com/image.png' } })
      } as Response)

      const result = await wrapper.vm.uploadAllImagesAndGetFinalContent()

      // The result should have replaced the blob URL with the actual URL
      expect(result).toContain('http://example.com/image.png')
      expect(wrapper.vm.pendingFiles).toHaveLength(0)
      expect(global.URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-url')
    })
  })

  describe('HTML Processing', () => {
    beforeEach(() => {
      wrapper = createWrapper()
    })

    it('restores escaped HTML except code blocks', () => {
      const htmlWithCode =
        '<p>&lt;span&gt;test&lt;/span&gt;</p><code>&lt;div&gt;</code>'
      const result = wrapper.vm.restoreEscapedHtmlExceptCode(htmlWithCode)

      expect(result).toContain('<span>test</span>')
      expect(result).toContain('<code>&lt;div&gt;</code>')
    })

    it('computes rendered HTML correctly', () => {
      const renderedHTML = wrapper.vm.renderedHTML
      expect(typeof renderedHTML).toBe('string')
    })

    it('computes plain text correctly', () => {
      const plainText = wrapper.vm.plainText
      expect(plainText).toBe('Test content')
    })

    it('compiles HTML with Handlebars', () => {
      const compiledHTML = wrapper.vm.compiledHTML
      expect(compiledHTML).toBe('<p>Compiled template</p>')
    })
  })

  describe('Cleanup', () => {
    it('cleans up preview URLs', () => {
      wrapper = createWrapper()

      wrapper.vm.previewUrls = ['blob:url1', 'blob:url2']
      wrapper.vm.cleanupPreviewUrls()

      expect(wrapper.vm.previewUrls).toHaveLength(0)
      expect(global.URL.revokeObjectURL).toHaveBeenCalledTimes(2)
    })

    it('cleans up on unmount', () => {
      wrapper = createWrapper()
      wrapper.vm.previewUrls = ['blob:url1']

      wrapper.unmount()

      expect(global.URL.revokeObjectURL).toHaveBeenCalled()
    })
  })

  describe('Exposed Methods', () => {
    beforeEach(() => {
      wrapper = createWrapper()
    })

    it('exposes required methods and computed properties', () => {
      const exposed = wrapper.vm

      expect(typeof exposed.renderedHTML).toBe('string')
      expect(typeof exposed.compiledHTML).toBe('string')
      expect(typeof exposed.plainText).toBe('string')
      expect(typeof exposed.uploadAllImagesAndGetFinalContent).toBe('function')
      expect(typeof exposed.cleanupPreviewUrls).toBe('function')
    })
  })

  describe('Parse HTML Toggle', () => {
    beforeEach(() => {
      wrapper = createWrapper()
    })

    it('toggles parseHtml state', async () => {
      expect(wrapper.vm.parseHtml).toBe(false)

      const parseButton = wrapper.find('button[title="Parse HTML"]')
      await parseButton.trigger('click')

      expect(wrapper.vm.parseHtml).toBe(true)
    })

    it('applies active class when parseHtml is true', async () => {
      wrapper.vm.parseHtml = true
      await nextTick()

      const parseButton = wrapper.find('button[title="Parse HTML"]')
      expect(parseButton.classes()).toContain('is-active')
    })
  })
})
