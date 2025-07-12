<script setup lang="ts">
import { watch, ref, computed } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import imageUrl from '@/assets/default.png'
import Code from '@tiptap/extension-code'
import Link from '@tiptap/extension-link'

import { Extension } from '@tiptap/core'
import { Decoration, DecorationSet } from 'prosemirror-view'
import { Plugin, PluginKey } from 'prosemirror-state'
const props = defineProps<{ content: string }>()

const emit = defineEmits(['update:content'])

const content = computed({
  get: () => props.content,
  set: (val: string) => emit('update:content', val)
})

const allowedVariables = ['asset-model', 'contact_name', 'post_town']

const createDecorations = (doc) => {
  console.log('Creating decorations for doc:', doc)
  const decorations = []
  const regex = /{{\s*([a-zA-Z0-9_]+)\s*}}/g

  doc.descendants((node, pos) => {
    if (node.isText && node.text) {
      const text = node.text
      let match
      regex.lastIndex = 0

      while ((match = regex.exec(text)) !== null) {
        const variableName = match[1]
        if (!allowedVariables.includes(variableName)) {
          const from = pos + match.index
          const to = pos + match.index + match[0].length
          console.log(
            `Found invalid variable "${variableName}" at ${from}-${to}`
          )

          decorations.push(
            Decoration.inline(from, to, {
              class: 'error-variable',
              title: `Unknown variable: ${variableName}`,
              style:
                'text-decoration: red wavy underline; background-color: rgba(255, 0, 0, 0.2); padding: 0 2px; border-radius: 2px;'
            })
          )
        }
      }
    }
  })

  console.log('Created decorations:', decorations)
  const decorationSet =
    decorations.length > 0
      ? DecorationSet.create(doc, decorations)
      : DecorationSet.empty
  console.log('Final decoration set:', decorationSet)
  return decorationSet
}

const VariableHighlight = Extension.create({
  name: 'variableHighlight',

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey('variableHighlight'),
        state: {
          init(config, state) {
            console.log('Extension plugin init called')
            return createDecorations(state.doc)
          },
          apply(tr, oldState) {
            console.log(
              'Extension plugin apply called, docChanged:',
              tr.docChanged
            )
            if (tr.docChanged) {
              return createDecorations(tr.doc)
            }
            return oldState.map(tr.mapping, tr.doc)
          }
        },
        props: {
          decorations(state) {
            const decorations = this.getState(state)
            console.log('Returning decorations for render:', decorations)
            return decorations
          }
        }
      })
    ]
  }
})

const editor = useEditor({
  content: content.value,
  extensions: [
    StarterKit,
    Image,
    Code,
    Link.configure({
      openOnClick: false,
      defaultProtocol: 'https'
    }),
    VariableHighlight
  ],
  onUpdate: ({ editor }) => {
    console.log('Editor onUpdate called')
    content.value = editor.getHTML()
  }
})

const fileList = ref<File[]>([])

const setLink = () => {
  const previousUrl = editor.value.getAttributes('link').href
  const url = window.prompt('URL', previousUrl)

  // cancelled
  if (url === null) {
    return
  }

  // empty
  if (url === '') {
    editor.chain().focus().extendMarkRange('link').unsetLink().run()

    return
  }

  // update link
  editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

watch(content, (newValue) => {
  if (editor.value && newValue !== editor.value.getHTML()) {
    editor.value.commands.setContent(newValue)
  }
})

watch(fileList, (newVal) => {
  console.log(newVal)
})
</script>

<template>
  <div class="menu" v-if="editor">
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

  <div>{{ content }}</div>
</template>

<style lang="scss">
.menu {
  margin-bottom: 20px;

  button {
    margin-right: 5px;
    padding: 5px 10px;
  }
}

.editor {
  border: 1px black solid;
  height: 300px;
  padding: 10px;
}

.ProseMirror:focus {
  outline: none;
}

.ProseMirror ::selection {
  background: rgba(33, 196, 245, 0.555);
}

.ProseMirror code {
  background-color: white;
  color: #c7254e;
  padding: 2px 4px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.95em;
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
</style>
