<script setup lang="ts">
import { watch, ref, computed } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import imageUrl from '@/assets/default.png'
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

const lowlight = createLowlight(all)
lowlight.register('html', html)
lowlight.register('css', css)
lowlight.register('js', js)
lowlight.register('ts', ts)

// import { Extension } from '@tiptap/core'
// import { Decoration, DecorationSet } from 'prosemirror-view'
// import { Plugin, PluginKey } from 'prosemirror-state'

const props = defineProps<{ content: string }>()

const emit = defineEmits(['update:content'])

const content = computed({
  get: () => props.content,
  set: (val: string) => emit('update:content', val)
})

// const allowedVariables = ['asset-model', 'contact_name', 'post_town']

// const createDecorations = (doc) => {
//   console.log('Creating decorations for doc:', doc)
//   const decorations = []
//   const regex = /{{\s*([a-zA-Z0-9_]+)\s*}}/g

//   doc.descendants((node, pos) => {
//     if (node.isText && node.text) {
//       const text = node.text
//       let match
//       regex.lastIndex = 0

//       while ((match = regex.exec(text)) !== null) {
//         const variableName = match[1]
//         if (!allowedVariables.includes(variableName)) {
//           const from = pos + match.index
//           const to = pos + match.index + match[0].length
//           console.log(
//             `Found invalid variable "${variableName}" at ${from}-${to}`
//           )

//           decorations.push(
//             Decoration.inline(from, to, {
//               class: 'error-variable',
//               title: `Unknown variable: ${variableName}`,
//               style:
//                 'text-decoration: red wavy underline; background-color: rgba(255, 0, 0, 0.2); padding: 0 2px; border-radius: 2px;'
//             })
//           )
//         }
//       }
//     }
//   })

//   console.log('Created decorations:', decorations)
//   const decorationSet =
//     decorations.length > 0
//       ? DecorationSet.create(doc, decorations)
//       : DecorationSet.empty
//   console.log('Final decoration set:', decorationSet)
//   return decorationSet
// }

// const VariableHighlight = Extension.create({
//   name: 'variableHighlight',

//   addProseMirrorPlugins() {
//     return [
//       new Plugin({
//         key: new PluginKey('variableHighlight'),
//         state: {
//           init(config, state) {
//             console.log('Extension plugin init called')
//             return createDecorations(state.doc)
//           },
//           apply(tr, oldState) {
//             console.log(
//               'Extension plugin apply called, docChanged:',
//               tr.docChanged
//             )
//             if (tr.docChanged) {
//               return createDecorations(tr.doc)
//             }
//             return oldState.map(tr.mapping, tr.doc)
//           }
//         },
//         props: {
//           decorations(state) {
//             const decorations = this.getState(state)
//             console.log('Returning decorations for render:', decorations)
//             return decorations
//           }
//         }
//       })
//     ]
//   }
// })

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
          const fileReader = new FileReader()

          fileReader.readAsDataURL(file)
          fileReader.onload = () => {
            currentEditor
              .chain()
              .insertContentAt(pos, {
                type: 'image',
                attrs: {
                  src: fileReader.result
                }
              })
              .focus()
              .run()
          }
        })
      },
      onPaste: (currentEditor, files) => {
        files.forEach((file) => {
          const fileReader = new FileReader()

          fileReader.readAsDataURL(file)
          fileReader.onload = () => {
            currentEditor
              .chain()
              .insertContentAt(currentEditor.state.selection.anchor, {
                type: 'image',
                attrs: {
                  src: fileReader.result
                }
              })
              .focus()
              .run()
          }
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

const updateContent = debounce((html: string) => {
  content.value = html
}, 300)

function debounce(fn: (_arg: string) => void, delay = 300) {
  let timer: ReturnType<typeof setTimeout> | null = null
  return function (_arg: string) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(_arg)
    }, delay)
  }
}

const saveContent = () => {
  console.log(editor.value?.getJSON())
  return JSON.stringify(editor.value?.getJSON())
}

const fileList = ref<File[]>([])

const setLink = () => {
  if (!editor.value) return
  const previousUrl = editor.value.getAttributes('link').href
  const url = window.prompt('URL', previousUrl)
  console.log(url)
  // cancelled
  if (url === null) {
    return
  }

  // empty
  if (url === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()

    return
  }

  // update link
  editor.value
    .chain()
    .focus()
    .extendMarkRange('link')
    .setLink({ href: url })
    .run()
}

const setColor = (e: Event) => {
  if (!editor.value || !e.target) return
  const target = e.target as HTMLInputElement
  editor.value.chain().focus().setColor(target.value).run()
}

watch(content, async (newValue) => {
  if (editor.value && newValue !== editor.value.getHTML()) {
    editor.value.commands.setContent(newValue)
  }
})

watch(fileList, (newVal) => {
  console.log(newVal)
})

defineExpose({
  saveContent
})
</script>

<template>
  <div class="menu" v-if="editor">
    <input
      type="color"
      @input="setColor"
      :value="editor.getAttributes('textStyle').color"
    />

    <button @click="editor.chain().focus().toggleHeading({ level: 1 }).run()">
      H1
    </button>
    <button
      @click="editor.chain().focus().undo().run()"
      :disabled="!editor.can().undo()"
    >
      Undo
    </button>
    <button
      @click="editor.chain().focus().redo().run()"
      :disabled="!editor.can().redo()"
    >
      Redo
    </button>
    <button @click="editor.chain().focus().setImage({ src: imageUrl }).run()">
      Insert Image
    </button>
    <button @click="editor.chain().focus().toggleBold().run()">
      <strong>B</strong>
    </button>

    <button @click="editor.chain().focus().toggleItalic().run()">
      <svg
        width="12"
        height="12"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
        strokeWidth="{1.5}"
        stroke="currentColor"
        className="size-6"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          d="M5.248 20.246H9.05m0 0h3.696m-3.696 0 5.893-16.502m0 0h-3.697m3.697 0h3.803"
        />
      </svg>
    </button>

    <button @click="editor.chain().focus().toggleCode().run()">
      Toggle code
    </button>

    <button @click="setLink" :class="{ 'is-active': editor.isActive('link') }">
      Set link
    </button>
    <button
      @click="editor.chain().focus().unsetLink().run()"
      :disabled="!editor.isActive('link')"
    >
      Unset link
    </button>
  </div>
  <div class="editor">
    <editor-content :editor="editor" />
  </div>
</template>

<style lang="scss">
@import 'highlight.js/styles/github.css';
.menu {
  margin-bottom: 20px;

  button {
    margin-right: 5px;
    padding: 5px 10px;
  }
}

.editor {
  background: white;
  border: 1px black solid;
  height: 300px;
  padding: 10px;
  max-width: 50%;
  overflow: auto;
}

.ProseMirror img {
  display: block;
  height: auto;
  margin: 1.5rem 0;
  max-width: 100%;
  max-height: 100%;
}

.ProseMirror:focus {
  outline: none;
}

.ProseMirror ::selection {
  background: rgba(33, 196, 245, 0.555);
}

.ProseMirror code {
  background: #f5f5f5;
  color: #222;
  font-family: 'Fira Mono', 'Consolas', 'Menlo', monospace;
  font-size: 1em;
  padding: 2px 5px;
}

.ProseMirror pre {
  background: #f5f5f5;
  color: #222;
  font-family: 'Fira Mono', 'Consolas', 'Menlo', monospace;
  font-size: 1em;
  padding: 16px;
  overflow-x: auto;
  margin: 1.2em 0;
}

.ProseMirror pre code {
  background: none;
  color: inherit;
  font-family: inherit;
  font-size: inherit;
  padding: 0;
}

.ProseMirror .error-variable {
  text-decoration: red wavy underline !important;
  cursor: help !important;
  background-color: rgba(255, 0, 0, 0.2) !important;
  padding: 0 2px !important;
  border-radius: 2px !important;
}

.ProseMirror span[style*='text-decoration: red wavy underline'] {
  background-color: rgba(255, 0, 0, 0.2) !important;
  padding: 0 2px !important;
  border-radius: 2px !important;
}

.ProseMirror a {
  color: #409eff;
  text-decoration: underline;
  font-weight: bold;
}
</style>
