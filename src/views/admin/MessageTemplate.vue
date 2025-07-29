<script setup lang="ts">
import { ref, onMounted, watch, computed, onBeforeUnmount, nextTick } from 'vue'

import {
  adminDeleteTemplateByIdService,
  adminGetTemplateByIdService,
  adminGetTemplateByTypesService,
  adminUpdateTemplateByIdService
} from '@/api/admin'
import { useAssetStore } from '@/stores'
import type { Template } from '@/types'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const route = useRoute()

const assetStore = useAssetStore()

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

const form = ref<Template>({
  id: 0,
  assetTypeId: '',
  warningType: '',
  severity: '',
  contactChannel: '',
  title: '',
  body: ''
})

const submit = async () => {
  form.value.body = renderedHTML.value
  try {
    await adminUpdateTemplateByIdService(form.value)
    isDirty.value = false
    console.log(renderedHTML.value)
    ElMessage.success('Template updated successfully')
  } catch (e) {
    console.error(e)
    ElMessage.error('Failed to update template')
  }
}

// Auto-save and user experience management
const isDirty = ref(false)
const isInitializing = ref(true)

// Delete confirm dialog
const dialogVisible = ref(false)
const deleteId = ref<number[]>([])

const triggerDelete = () => {
  dialogVisible.value = true
  deleteId.value.push(form.value.id)
}

const handleDelete = async () => {
  await adminDeleteTemplateByIdService(deleteId.value)
  dialogVisible.value = false
}

// Smart beforeunload handling
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  // Only warn if there are unsaved changes that haven't been submitted
  if (isDirty.value) {
    e.preventDefault()
    e.returnValue = 'You have unsaved changes. Are you sure you want to leave?'
  }
}

onMounted(async () => {
  const id = route.query.id as string
  console.log(id)
  let template: Template
  if (typeof id === 'string') {
    console.log(id)
    const res = await adminGetTemplateByIdService(id)
    template = res.data[0]
    if (template) {
      assetType.value = template.assetTypeId
      warningType.value = template.warningType
      warningLevel.value = template.severity
      contactChannel.value = template.contactChannel
    }
  } else {
    const res = await adminGetTemplateByTypesService(
      assetType.value,
      warningType.value,
      warningLevel.value,
      contactChannel.value
    )
    template = res.data[0]
  }
  if (template) {
    form.value = template
  }

  // Now that initialization is complete, enable the watch
  await nextTick()
  isInitializing.value = false

  // Add beforeunload listener for unsaved changes
  window.addEventListener('beforeunload', handleBeforeUnload)
  // Removed beforeunload listener since data is auto-saved
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

watch(
  [warningType, warningLevel, assetType, contactChannel],
  async ([
    newWarningType,
    newWarningLevel,
    newAssetType,
    newContactChannel
  ]) => {
    // Don't fetch new template during initialization
    if (isInitializing.value) {
      return
    }

    // User changed dropdown, fetch new template
    const res = await adminGetTemplateByTypesService(
      newAssetType,
      newWarningType,
      newWarningLevel,
      newContactChannel
    )
    const template = res.data[0]
    if (template) {
      form.value = res.data[0]
    }
  },
  {
    immediate: false // Don't run immediately, let onMounted handle initial load
  }
)

watch(
  form,
  () => {
    if (isInitializing.value) return

    isDirty.value = true
  },
  { deep: true }
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
  <el-input v-model="form.title"></el-input>

  <div style="display: flex; gap: 24px; align-items: flex-start">
    <TiptapEditor ref="editorRef" v-model:content="form.body" />
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
