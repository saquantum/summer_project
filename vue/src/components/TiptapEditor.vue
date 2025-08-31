<script setup lang="ts">
import { watch, ref, computed, onMounted, onUnmounted } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import Code from '@tiptap/extension-code'
import Link from '@tiptap/extension-link'
import { Color, TextStyle } from '@tiptap/extension-text-style'
import { FileHandler } from '@tiptap/extension-file-handler'
import { CodeBlockLowlight } from '@tiptap/extension-code-block-lowlight'
import { all, createLowlight } from 'lowlight'
import css from 'highlight.js/lib/languages/css'
import js from 'highlight.js/lib/languages/javascript'
import ts from 'highlight.js/lib/languages/typescript'
import html from 'highlight.js/lib/languages/xml'
import { ElMessage, type UploadFile } from 'element-plus'
import Handlebars from 'handlebars'
import DOMPurify from 'dompurify'
import { getUploadData, getUploadUrl, validateImageFile } from '@/utils/upload'
import { addInlineStyle, generateEditorCSS } from '@/utils/editorStyle'

// Configure DOMPurify to allow blob URLs
DOMPurify.addHook('uponSanitizeAttribute', (node, data) => {
  if (data.attrName === 'src' && data.attrValue.startsWith('blob:')) {
    // Allow blob URLs for images
    return
  }
})

const lowlight = createLowlight(all)
lowlight.register('html', html)
lowlight.register('css', css)
lowlight.register('js', js)
lowlight.register('ts', ts)

const injectEditorStyles = () => {
  const styleId = 'tiptap-dynamic-styles'
  const existingStyle = document.getElementById(styleId)

  if (existingStyle) {
    existingStyle.remove()
  }

  const style = document.createElement('style')
  style.id = styleId
  style.textContent = generateEditorCSS()
  document.head.appendChild(style)
}

const props = defineProps<{ content: string }>()

const emit = defineEmits(['update:content'])

const content = computed({
  get: () => props.content,
  set: (val: string) => emit('update:content', val)
})

const parseHtml = ref(false)

const currentHeadingLevel = computed(() => {
  if (!editor.value || !editor.value.getAttributes('heading').level) return 'H'
  return `H${editor.value.getAttributes('heading').level}`
})

const CustomImage = Image.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      src: {
        default: null,
        parseHTML: (element) => {
          const src = element.getAttribute('src')
          return src
        },
        renderHTML: (attributes) => {
          if (!attributes.src) return {}
          return {
            src: attributes.src,
            loading: 'lazy'
          }
        }
      }
    }
  },

  parseHTML() {
    return [
      {
        tag: 'img[src]',
        getAttrs: (element) => {
          const src = element.getAttribute('src')
          return src ? {} : false
        }
      }
    ]
  }
})

const editor = useEditor({
  content: content.value,
  extensions: [
    StarterKit,
    CustomImage,
    Code,
    TextStyle,
    Color,
    Link.configure({
      openOnClick: false,
      defaultProtocol: 'https'
    }),
    // VariableHighlight,
    FileHandler.configure({
      allowedMimeTypes: ['image/png', 'image/jpeg', 'image/gif', 'image/webp'],
      onDrop: (currentEditor, files, pos) => {
        files.forEach((file) => {
          const validation = validateImageFile(file)
          if (!validation.valid) {
            ElMessage.error(validation.message || 'File validation failed')
            return
          }

          // Create local preview URL
          const localUrl = URL.createObjectURL(file)

          // Save file reference and mapping relationship
          pendingFiles.value.push(file)
          previewUrls.value.push(localUrl)
          localUrlToFileMap.value.set(localUrl, file)

          currentEditor
            .chain()
            .insertContentAt(pos, {
              type: 'image',
              attrs: {
                src: localUrl
              }
            })
            .focus()
            .run()
        })
      },
      onPaste: (currentEditor, files) => {
        files.forEach((file) => {
          const validation = validateImageFile(file)
          if (!validation.valid) {
            ElMessage.error(validation.message || 'File validation failed')
            return
          }

          // Create local preview URL
          const localUrl = URL.createObjectURL(file)

          // Save file reference and mapping relationship
          pendingFiles.value.push(file)
          previewUrls.value.push(localUrl)
          localUrlToFileMap.value.set(localUrl, file)

          currentEditor
            .chain()
            .insertContentAt(currentEditor.state.selection.anchor, {
              type: 'image',
              attrs: {
                src: localUrl
              }
            })
            .focus()
            .run()
        })
      }
    }),
    CodeBlockLowlight.configure({
      lowlight
    })
  ],
  onUpdate: ({ editor }) => {
    updateContent(editor.getHTML())
  }
})

const linkUrl = ref('')

const updateContent = (html: string) => {
  content.value = html
}

const getLink = () => {
  linkUrl.value = ''
  if (!editor.value) return
  if (editor.value.getAttributes('link').href) {
    linkUrl.value = editor.value.getAttributes('link').href
  }
}

// Helper function to normalize URL
const normalizeUrl = (url: string): string => {
  if (!url) return url

  const trimmedUrl = url.trim()

  // Check if it already has a protocol
  if (/^https?:\/\//i.test(trimmedUrl)) {
    return trimmedUrl
  }

  // Check if it's a mailto link
  if (/^mailto:/i.test(trimmedUrl)) {
    return trimmedUrl
  }

  // Check if it's a tel link
  if (/^tel:/i.test(trimmedUrl)) {
    return trimmedUrl
  }

  // Check if it's a relative URL (starts with / or ./)
  if (/^(\/|\.\/|\.\.\/)/i.test(trimmedUrl)) {
    return trimmedUrl
  }

  // Check if it's an anchor link (starts with #)
  if (/^#/i.test(trimmedUrl)) {
    return trimmedUrl
  }

  // Otherwise, add https://
  return `https://${trimmedUrl}`
}

const setLink = () => {
  if (!editor.value) return

  // empty
  if (linkUrl.value === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }

  // Normalize the URL by adding https:// if needed
  const normalizedUrl = normalizeUrl(linkUrl.value)

  // update link
  editor.value
    .chain()
    .focus()
    .extendMarkRange('link')
    .setLink({ href: normalizedUrl })
    .run()

  // Update the input value to show the normalized URL
  linkUrl.value = normalizedUrl
}

const setColor = (e: Event) => {
  if (!editor.value || !e.target) return
  const target = e.target as HTMLInputElement
  editor.value.chain().focus().setColor(target.value).run()
}

const handleCommand = (command: string) => {
  if (!editor.value) return
  const level = Number(command) as 1 | 2 | 3 | 4 | 5 | 6
  editor.value.chain().focus().toggleHeading({ level }).run()
}

// Store pending files and their corresponding local preview URLs
const pendingFiles = ref<File[]>([])
const previewUrls = ref<string[]>([])
const localUrlToFileMap = ref<Map<string, File>>(new Map())

// Handle file selection and create local preview
const handleImageUpload = (uploadFile: UploadFile) => {
  const file = uploadFile.raw
  if (!file) return

  const validation = validateImageFile(file)
  if (!validation.valid) {
    ElMessage.error(validation.message || 'File validation failed')
    return
  }

  // Create local preview URL
  const localUrl = URL.createObjectURL(file)

  // Save file reference and mapping relationship
  pendingFiles.value.push(file)
  previewUrls.value.push(localUrl)
  localUrlToFileMap.value.set(localUrl, file)

  // Insert local preview image in editor
  if (editor.value) {
    editor.value.chain().focus().setImage({ src: localUrl }).run()
  }
}

// Upload single file to server
const uploadSingleFile = async (file: File): Promise<string | null> => {
  const formData = new FormData()
  formData.append('file', file)

  // Add other required upload data
  const uploadData = getUploadData()
  Object.entries(uploadData).forEach(([key, value]) => {
    formData.append(key, value)
  })

  try {
    const response = await fetch(getUploadUrl('image'), {
      method: 'POST',
      body: formData
    })

    const result = await response.json()
    if (result.data?.url) {
      return result.data.url
    }
    throw new Error('Upload failed')
  } catch (error) {
    console.error('Image upload error:', error)
    throw error
  }
}

// Batch upload all images and replace URLs when submitting
const uploadAllImagesAndGetFinalContent = async (): Promise<string> => {
  if (pendingFiles.value.length === 0) {
    return renderedHTML.value || ''
  }

  let finalContent = renderedHTML.value || ''

  // Upload corresponding file for each local URL
  for (const [localUrl, file] of localUrlToFileMap.value.entries()) {
    try {
      const serverUrl = await uploadSingleFile(file)
      console.log(serverUrl)
      if (serverUrl) {
        // Replace local URL with server URL in HTML
        finalContent = finalContent.replace(
          new RegExp(localUrl, 'g'),
          serverUrl
        )
        // Clean up local URL
        URL.revokeObjectURL(localUrl)
      }
    } catch (error) {
      console.error('Failed to upload image:', error)
      ElMessage.error('Some images failed to upload, please try again')
    }
  }

  // Clean up state
  pendingFiles.value = []
  previewUrls.value = []
  localUrlToFileMap.value.clear()

  return finalContent
}

// Clean up local preview URLs (called when component is destroyed)
const cleanupPreviewUrls = () => {
  previewUrls.value.forEach((url) => {
    URL.revokeObjectURL(url)
  })
  previewUrls.value = []
  localUrlToFileMap.value.clear()
}

/**
 * rendered html logic
 */

const mockData = {
  asset_model: 'Water tank',
  contact_name: 'Alice',
  post_town: 'London'
}

function restoreEscapedHtmlExceptCode(html: string) {
  const codeBlocks: string[] = []
  // 1. replace all code
  const htmlWithPlaceholders = html.replace(
    /<code[^>]*>[\s\S]*?<\/code>/g,
    (match: string) => {
      codeBlocks.push(match)
      return `__CODE_BLOCK_${codeBlocks.length - 1}__`
    }
  )

  // 2. restore other part
  const restored = htmlWithPlaceholders.replace(
    /(&lt;[\s\S]*?&gt;)/g,
    (match: string) => {
      const div = document.createElement('div')
      div.innerHTML = match
      return div.textContent || div.innerText || ''
    }
  )

  // 3. restore code
  return codeBlocks.reduce(
    (acc, block, idx) => acc.replace(`__CODE_BLOCK_${idx}__`, block),
    restored
  )
}

// Custom sanitize function that preserves blob URLs
const sanitizeHtml = (html: string): string => {
  return DOMPurify.sanitize(html, {
    ADD_ATTR: ['src', 'alt', 'title', 'width', 'height'],
    ADD_TAGS: ['img'],
    ALLOW_UNKNOWN_PROTOCOLS: true,
    ALLOWED_URI_REGEXP:
      /^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp|blob|data):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i,
    KEEP_CONTENT: true
  })
}

const renderedHTML = computed(() => {
  try {
    let htmlWithStyle
    if (parseHtml.value) {
      htmlWithStyle = addInlineStyle(
        restoreEscapedHtmlExceptCode(content.value)
      )
    } else {
      htmlWithStyle = addInlineStyle(content.value)
    }

    // sanitize html code with blob URL support
    return sanitizeHtml(htmlWithStyle)
  } catch (e) {
    console.error(e)
    return '<p style="color:red">Syntax Error!</p>'
  }
})

const plainText = computed(() => {
  if (!editor.value) return ''
  return editor.value.getText()
})

const compiledHTML = computed(() => {
  try {
    // compile variable
    const compiled = Handlebars.compile(renderedHTML.value)

    return compiled(mockData)
  } catch (e) {
    console.error(e)
    return '<p style="color:red">Syntax Error!</p>'
  }
})

watch(content, async (newValue) => {
  if (editor.value && newValue !== editor.value.getHTML()) {
    editor.value.commands.setContent(newValue)
  }
})

onMounted(() => {
  injectEditorStyles()
})

onUnmounted(() => {
  const styleElement = document.getElementById('tiptap-dynamic-styles')
  if (styleElement) {
    styleElement.remove()
  }
  // Clean up local preview URLs
  cleanupPreviewUrls()
})

defineExpose({
  renderedHTML,
  compiledHTML,
  plainText,
  uploadAllImagesAndGetFinalContent,
  cleanupPreviewUrls,
  // Expose for testing - keep refs as refs
  linkUrl: linkUrl,
  setLink,
  normalizeUrl,
  handleImageUpload,
  uploadSingleFile,
  pendingFiles: pendingFiles,
  previewUrls: previewUrls,
  localUrlToFileMap: localUrlToFileMap,
  restoreEscapedHtmlExceptCode,
  updateContent,
  handleCommand,
  parseHtml: parseHtml
})
</script>

<template>
  <div class="editor-wrapper">
    <div class="menu" v-if="editor">
      <button
        @click="editor.chain().focus().undo().run()"
        :disabled="!editor.can().undo()"
      >
        <svg
          width="24"
          height="24"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="black"
          class="my-svg"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="m7.49 12-3.75 3.75m0 0 3.75 3.75m-3.75-3.75h16.5V4.499"
          />
        </svg>
      </button>
      <button
        @click="editor.chain().focus().redo().run()"
        :disabled="!editor.can().redo()"
      >
        <svg
          width="24"
          height="24"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="my-svg"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="m16.49 12 3.75 3.75m0 0-3.75 3.75m3.75-3.75H3.74V4.499"
          />
        </svg>
      </button>
      <input
        type="color"
        @input="setColor"
        :value="editor.getAttributes('textStyle').color"
      />

      <el-dropdown @command="handleCommand">
        <button>{{ currentHeadingLevel }}</button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="1">H1</el-dropdown-item>
            <el-dropdown-item command="2">H2</el-dropdown-item>
            <el-dropdown-item command="3">H3</el-dropdown-item>
            <el-dropdown-item command="4">H4</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <button @click="editor.chain().focus().toggleBold().run()">
        <svg
          width="24"
          height="24"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="my-svg"
        >
          <path
            stroke-linejoin="round"
            d="M6.75 3.744h-.753v8.25h7.125a4.125 4.125 0 0 0 0-8.25H6.75Zm0 0v.38m0 16.122h6.747a4.5 4.5 0 0 0 0-9.001h-7.5v9h.753Zm0 0v-.37m0-15.751h6a3.75 3.75 0 1 1 0 7.5h-6m0-7.5v7.5m0 0v8.25m0-8.25h6.375a4.125 4.125 0 0 1 0 8.25H6.75m.747-15.38h4.875a3.375 3.375 0 0 1 0 6.75H7.497v-6.75Zm0 7.5h5.25a3.75 3.75 0 0 1 0 7.5h-5.25v-7.5Z"
          />
        </svg>
      </button>

      <button @click="editor.chain().focus().toggleItalic().run()">
        <svg
          width="24"
          height="24"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          strokeWidth="{1.5}"
          stroke="currentColor"
          class="my-svg"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            d="M5.248 20.246H9.05m0 0h3.696m-3.696 0 5.893-16.502m0 0h-3.697m3.697 0h3.803"
          />
        </svg>
      </button>

      <button @click="editor.chain().focus().toggleCode().run()">
        <svg
          width="24"
          height="24"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="my-svg"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M17.25 6.75 22.5 12l-5.25 5.25m-10.5 0L1.5 12l5.25-5.25m7.5-3-4.5 16.5"
          />
        </svg>
      </button>

      <el-popover placement="bottom" :width="200" trigger="click">
        <template #reference>
          <button
            @click="getLink"
            :class="{ 'is-active': editor.isActive('link') }"
          >
            <svg
              width="24"
              height="24"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              stroke="currentColor"
              class="my-svg"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M13.19 8.688a4.5 4.5 0 0 1 1.242 7.244l-4.5 4.5a4.5 4.5 0 0 1-6.364-6.364l1.757-1.757m13.35-.622 1.757-1.757a4.5 4.5 0 0 0-6.364-6.364l-4.5 4.5a4.5 4.5 0 0 0 1.242 7.244"
              />
            </svg>
          </button>
        </template>

        <div>
          <el-input
            v-model="linkUrl"
            @keyup.enter="setLink"
            placeholder="Enter URL (https:// will be added automatically)"
            clearable
            style="margin-bottom: 10px"
          ></el-input>
          <div style="display: flex; gap: 8px; justify-content: flex-end">
            <el-button size="small" @click="setLink" type="primary">
              Apply
            </el-button>
          </div>
        </div>
      </el-popover>

      <el-upload
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleImageUpload"
        accept="image/*"
      >
        <button>
          <svg
            width="24"
            height="24"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="my-svg"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z"
            />
          </svg>
        </button>
      </el-upload>

      <button
        @click="parseHtml = !parseHtml"
        :class="{ 'is-active': parseHtml }"
        title="Parse HTML"
      >
        <svg
          width="24"
          height="24"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="my-svg"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M9 12h3.75M9 15h3.75M9 18h3.75m3 .75H18a2.25 2.25 0 0 0 2.25-2.25V6.108c0-1.135-.845-2.098-1.976-2.192a48.424 48.424 0 0 0-1.123-.08m-5.801 0c-.065.21-.1.433-.1.664 0 .414.336.75.75.75h4.5a.75.75 0 0 0 .75-.75 2.25 2.25 0 0 0-.1-.664m-5.8 0A2.251 2.251 0 0 1 13.5 2.25H15c1.012 0 1.867.668 2.15 1.586m-5.8 0c-.376.023-.75.05-1.124.08C9.095 4.01 8.25 4.973 8.25 6.108V8.25m0 0H4.875c-.621 0-1.125.504-1.125 1.125v11.25c0 .621.504 1.125 1.125 1.125h9.75c.621 0 1.125-.504 1.125-1.125V9.375c0-.621-.504-1.125-1.125-1.125H8.25ZM6.75 12h.008v.008H6.75V12Zm0 3h.008v.008H6.75V15Zm0 3h.008v.008H6.75V18Z"
          />
        </svg>
      </button>
    </div>
    <div class="editor-container">
      <editor-content :editor="editor" />
    </div>
  </div>
</template>

<style lang="scss">
@import 'highlight.js/styles/github.css';

.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.menu {
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  padding: 8px 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;

  button {
    background: transparent;
    border: none;
    border-radius: 4px;
    padding: 6px;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    color: #606266;

    &:hover {
      background: #e6f7ff;
      color: #409eff;
    }

    &:disabled {
      background: transparent;
      color: #c0c4cc;
      cursor: not-allowed;
    }

    &.is-active {
      background: #409eff;
      color: #fff;
    }
  }

  input[type='color'] {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    padding: 0;
    background: transparent;
  }

  .el-dropdown {
    .el-dropdown-link {
      background: transparent;
      border: none;
      border-radius: 4px;
      padding: 6px 8px;
      cursor: pointer;
      height: 32px;
      display: flex;
      align-items: center;
      color: #606266;
      min-width: 40px;
      justify-content: center;

      &:hover {
        background: #e6f7ff;
        color: #409eff;
      }
    }
  }

  .el-upload {
    display: flex;
    align-items: center;
  }

  span {
    color: #606266;
    font-size: 14px;
    margin-left: 12px;
    margin-right: 8px;
  }
}

.my-svg {
  width: 16px;
  height: 16px;
}

.editor-container {
  background: #fff;
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  overflow: auto;
}
</style>
