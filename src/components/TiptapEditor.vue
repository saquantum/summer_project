<script setup lang="ts">
import { watch, ref, computed } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import imageUrl from '@/assets/default.png'

const props = defineProps<{ content: string }>()

const emit = defineEmits(['update:content'])

const content = computed({
  get: () => props.content,
  set: (val: string) => emit('update:content', val)
})

const editor = useEditor({
  content: content.value,
  extensions: [StarterKit, Image],
  onUpdate: ({ editor }) => {
    content.value = editor.getHTML()
  }
})

const fileList = ref<File[]>([])

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
  <div class="menu">
    <button @click="editor?.chain().focus().setImage({ src: imageUrl }).run()">
      Insert Image
    </button>
    <button @click="editor?.chain().focus().toggleBold().run()">
      <strong>B</strong>
    </button>
    <!--
    <el-upload
      v-model:file-list="fileList"
      action="https://run.mocky.io/v3/9d059bf9-4660-45f2-925d-ce80ad6c4d15"
      multiple
    >
      <el-button type="primary">Click to upload</el-button>
    </el-upload>
    -->
  </div>
  <div class="editor">
    <editor-content :editor="editor" />
  </div>

  <div>{{ content }}</div>
</template>

<style lang="scss">
.menu {
  margin-bottom: 20px;
}

.editor {
  border: 1px black solid;
  height: 300px;
}

.ProseMirror:focus {
  outline: none;
}
</style>
