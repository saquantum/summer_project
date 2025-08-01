<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'

import {
  adminDeleteTemplateByIdService,
  adminGetTemplateByTypesService,
  adminUpdateTemplateByIdService
} from '@/api/admin'
import { useAssetStore } from '@/stores'
import type { Template } from '@/types'

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

const renderedHTML = computed(() => {
  if (!editorRef.value) return ''
  return editorRef.value.renderedHTML
})

const compiledHTML = computed(() => {
  if (!editorRef.value) return ''
  return editorRef.value.compiledHTML
})

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

  <div style="display: flex; gap: 24px; align-items: flex-start">
    <TiptapEditor ref="editorRef" v-model:content="content" />
    <div
      class="preview"
      style="
        border: 1px solid #ccc;
        padding: 1rem;
        margin-top: 0;
        min-height: 524px;
        background-color: white;
      "
    >
      <div v-html="compiledHTML"></div>
    </div>
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
