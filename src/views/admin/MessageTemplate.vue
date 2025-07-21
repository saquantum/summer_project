<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'

import {
  adminDeleteTemplateByIdService,
  adminGetTemplateByTypesService,
  adminUpdateTemplateByIdService
} from '@/api/admin'
import Handlebars from 'handlebars'
import { useAssetStore } from '@/stores'
import type { Template } from '@/types'
import DOMPurify from 'dompurify'

const assetStore = useAssetStore()

const template = ref<Template | null>(null)
const warningTypeOption = [
  { label: 'Rain', value: 'Rain' },
  { label: 'Thunderstorm', value: 'Thunderstorm' },
  { label: 'Lightning', value: 'Lightning' },
  { label: 'Snow', value: 'Snow' },
  { label: 'Ice', value: 'Ice' },
  { label: 'Fog', value: 'Fog' },
  { label: 'Wind', value: 'Wind' },
  { label: 'Extreme Heat', value: 'Heat' }
]

const warningLevelOptions = [
  {
    value: 'YELLOW',
    label: 'Yellow Warning'
  },
  {
    value: 'AMBER',
    label: 'Amber Warning'
  },
  {
    value: 'RED',
    label: 'Red Warning'
  }
]

const contactChannelOptions = [
  {
    value: 'Email',
    label: 'Email'
  },
  {
    value: 'SMS',
    label: 'SMS'
  }
]

const warningType = ref('Rain')
const warningLevel = ref('YELLOW')
const assetType = ref('type_001')
const contactChannel = ref('Email')

const content = ref(`You haven't set message for this.`)
const title = ref('')
const editorRef = ref()
const allowedVariables = ['asset-model', 'contact_name', 'post_town']

const parseHtml = ref(false)

const mockData = {
  'asset-model': 'Water tank',
  contact_name: 'Alice',
  post_town: 'London'
}

const submit = async () => {
  if (!template.value) return
  template.value.body = renderedHTML.value
  template.value.title = title.value
  const res = await adminUpdateTemplateByIdService(template.value)
  console.log(res)
}

// delete confirm
const dialogVisible = ref(false)
const deleteId = ref<number[]>([])

const triggerDelete = () => {
  if (!template.value) return
  dialogVisible.value = true
  deleteId.value.push(template.value.id)
}

const handleDelete = async () => {
  await adminDeleteTemplateByIdService(deleteId.value)
  dialogVisible.value = false
}

function addLinkInlineStyle(html: string): string {
  const htmlWithLinkStyle = html.replace(
    /<a\b([^>]*)>/g,
    '<a$1 style="color: #409eff; text-decoration: underline; font-weight: bold;">'
  )
  const htmlWithImgStyle = htmlWithLinkStyle.replace(
    /<img\b([^>]*)>/g,
    '<img$1 style="display: block; height: auto; margin: 1.5rem 0; max-width: 100%; max-height: 100%;">'
  )
  const htmlWithPreStyle = htmlWithImgStyle.replace(
    /<pre\b([^>]*)>/g,
    '<pre$1 style="background: #f5f5f5; color: #333; border-radius: 6px; padding: 16px; font-family: Fira Mono, Consolas, Menlo, monospace; font-size: 1em; overflow-x: auto; margin: 1.2em 0;">'
  )
  const htmlWithCodeStyle = htmlWithPreStyle.replace(
    /<pre([^>]*)><code([^>]*)>/g,
    '<pre$1><code$2 style="background: none; color: inherit; padding: 0; border-radius: 0; font-family: inherit; font-size: inherit;">'
  )
  return htmlWithCodeStyle
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

const renderedHTML = computed(() => {
  try {
    let htmlWithStyle
    if (parseHtml.value) {
      htmlWithStyle = addLinkInlineStyle(
        restoreEscapedHtmlExceptCode(content.value)
      )
    } else {
      htmlWithStyle = addLinkInlineStyle(content.value)
    }

    // compile variable
    const compiled = Handlebars.compile(htmlWithStyle)
    const rawHtml = compiled(mockData)
    console.log(DOMPurify.sanitize(rawHtml))

    // sanitize html code
    return DOMPurify.sanitize(rawHtml)
    // return rawHtml
  } catch (e) {
    console.error(e)
    return '<p style="color:red">Syntax Error!</p>'
  }
})

onMounted(async () => {})

watch(
  [warningType, warningLevel, assetType, contactChannel],
  async ([
    newWarningType,
    newWarningLevel,
    newAssetType,
    newContactChannel
  ]) => {
    const res = await adminGetTemplateByTypesService(
      newAssetType,
      newWarningType,
      newWarningLevel,
      newContactChannel
    )
    template.value = res.data[0]
    if (template.value) {
      title.value = template.value.title
      content.value = template.value.body ?? `You haven't set message for this.`
    }
  },
  {
    immediate: true
  }
)
</script>

<template>
  <div>Allowed variable:</div>
  <ul>
    <li v-for="item in allowedVariables" :key="item">{{ item }}</li>
  </ul>

  <el-select v-model="warningType">
    <el-option
      v-for="item in warningTypeOption"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select v-model="contactChannel">
    <el-option
      v-for="item in contactChannelOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select v-model="assetType" placeholder="Please choose asset type">
    <el-option
      v-for="item in assetStore.typeOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>

  <el-select v-model="warningLevel" placeholder="Select warning level">
    <el-option
      v-for="item in warningLevelOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <div>Title</div>
  <el-input v-model="title"></el-input>

  <TiptapEditor ref="editorRef" v-model:content="content"></TiptapEditor>
  <span>Parse html: </span>
  <el-switch v-model="parseHtml"></el-switch>
  <div
    class="preview"
    style="
      width: 50%;
      border: 1px solid #ccc;
      padding: 1rem;
      margin-top: 20px;
      background-color: white;
    "
  >
    <div v-html="renderedHTML"></div>
  </div>

  <div>
    <el-button @click="submit">Submit</el-button>
    <el-button @click="triggerDelete">Delete</el-button>
  </div>

  <ConfirmDialog
    v-model="dialogVisible"
    title="Warning"
    content="This will permanently delete this template"
    :countdown-duration="1"
    @confirm="handleDelete"
    @cancel="dialogVisible = false"
  />
</template>

<style scoped>
.text-area {
  margin-top: 20px;
}
</style>
