<script setup lang="ts">
import { ref, onMounted, watch, computed, onBeforeUnmount, nextTick } from 'vue'

import {
  adminDeleteTemplateByIdService,
  adminGetTemplateByIdService,
  adminGetTemplateByTypesService,
  adminInsertTemplateService,
  adminUpdateTemplateByTypesService
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
    label: 'email'
  },
  {
    value: 'Phone',
    label: 'phone'
  }
]

const placeholder = "You haven't set template for this"
const hasTemplate = ref(true)

const editorRef = ref()
const allowedVariables = ['asset_model', 'contact_name', 'post_town']

const compiledHTML = computed(() => {
  if (!editorRef.value) return ''
  return editorRef.value.compiledHTML
})

const plainText = computed(() => {
  if (!editorRef.value) return ''
  return editorRef.value.plainText
})

const form = ref<Template>({
  id: 0,
  assetTypeId: 'type_001',
  warningType: 'Rain',
  severity: 'YELLOW',
  contactChannel: 'email',
  title: '',
  body: ''
})

const confirmDialogVisible = ref(false)

const triggerSubmit = () => {
  // empty content alert
  if (plainText.value === placeholder || !plainText.value) {
    confirmDialogVisible.value = true
  } else {
    submit()
  }
}
const submit = async () => {
  // set platform that can receive html format content.
  if (form.value.contactChannel === 'email') {
    try {
      const content = await editorRef.value.uploadAllImagesAndGetFinalContent()
      form.value.body = content
    } catch {
      ElMessage.error('Fail to upload images')
    }
  } else {
    form.value.body = plainText.value
  }

  try {
    if (hasTemplate.value) {
      await adminUpdateTemplateByTypesService(form.value)
    } else {
      await adminInsertTemplateService(form.value)
    }

    isDirty.value = false
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

  let template: Template
  if (typeof id === 'string') {
    const res = await adminGetTemplateByIdService(id)

    template = res.data

    if (template) {
      form.value.assetTypeId = template.assetTypeId
      form.value.warningType = template.warningType
      form.value.severity = template.severity
      form.value.contactChannel = template.contactChannel
    }
  } else {
    const res = await adminGetTemplateByTypesService(
      form.value.assetTypeId,
      form.value.warningType,
      form.value.severity,
      form.value.contactChannel
    )
    template = res.data[0]
  }
  if (template) {
    form.value = template
  } else {
    form.value.body = placeholder
    hasTemplate.value = false
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
  () => ({ ...form.value }),
  async (newForm, oldForm) => {
    if (isInitializing.value) return

    // Check if dropdown values changed (not title/body changes)
    if (
      oldForm &&
      (newForm.warningType !== oldForm.warningType ||
        newForm.severity !== oldForm.severity ||
        newForm.assetTypeId !== oldForm.assetTypeId ||
        newForm.contactChannel !== oldForm.contactChannel)
    ) {
      // User changed dropdown, fetch new template
      const res = await adminGetTemplateByTypesService(
        newForm.assetTypeId,
        newForm.warningType,
        newForm.severity,
        newForm.contactChannel
      )
      const template = res.data[0]
      if (template) {
        form.value = template
        isDirty.value = false // Reset dirty state after loading new template
        return // Don't set dirty if we just loaded a template
      } else {
        form.value.body = placeholder
        hasTemplate.value = false
      }
    }

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

  <el-select v-model="form.warningType">
    <el-option
      v-for="item in warningTypeOption"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select v-model="form.contactChannel">
    <el-option
      v-for="item in contactChannelOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select v-model="form.assetTypeId" placeholder="Please choose asset type">
    <el-option
      v-for="item in assetStore.typeOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>

  <el-select v-model="form.severity" placeholder="Select warning level">
    <el-option
      v-for="item in warningLevelOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <div>Title</div>
  <el-input v-model="form.title"></el-input>

  <div style="display: flex; gap: 24px; align-items: stretch; flex-wrap: wrap">
    <div style="flex: 1; min-width: 300px; height: 564px">
      <TiptapEditor ref="editorRef" v-model:content="form.body" />
    </div>
    <div
      class="preview"
      style="
        border: 1px solid #ccc;
        padding: 1rem;
        margin-top: 0;
        height: 564px;
        min-width: 300px;
        flex: 1;
        background-color: white;
        overflow-y: auto;
      "
    >
      <div v-html="compiledHTML"></div>
    </div>
  </div>

  <div>
    <el-button @click="triggerSubmit">Submit</el-button>
    <el-button @click="triggerDelete">Delete</el-button>
  </div>

  <el-dialog v-model="confirmDialogVisible" title="Alert" width="500">
    <span>You didn't write anything. Are you sure you want to submit?</span>
    <template #footer>
      <div>
        <el-button @click="confirmDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submit"> Submit </el-button>
      </div>
    </template>
  </el-dialog>

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
