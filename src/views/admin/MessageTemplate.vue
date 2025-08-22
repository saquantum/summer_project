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
import PageTopTabs from '@/components/PageSurfaceTabs.vue'
import type { RouteLocationNormalized } from 'vue-router'

const tabsConfig = [
  {
    label: 'Send Message',
    to: { name: 'AdminSendMessage' },
    match: (r: RouteLocationNormalized) => r.name === 'AdminSendMessage'
  },

  {
    label: 'Manage Template',
    to: { name: 'AdminMessageTemplate' },
    match: (r: RouteLocationNormalized) => r.name === 'AdminMessageTemplate'
  }
]

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
  <div class="page-surface">
    <PageTopTabs :tabs="tabsConfig" />
    <el-card class="card settings-card">
      <el-form class="filters-form" label-position="top">
        <span class="title">Template settings: </span>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="Warning type">
              <el-select v-model="form.warningType" placeholder="Select">
                <el-option
                  v-for="item in warningTypeOption"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="Channel">
              <el-select v-model="form.contactChannel" placeholder="Select">
                <el-option
                  v-for="item in contactChannelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="Asset type">
              <el-select
                v-model="form.assetTypeId"
                placeholder="Please choose asset type"
              >
                <el-option
                  v-for="item in assetStore.typeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="Warning level">
              <el-select
                v-model="form.severity"
                placeholder="Select warning level"
              >
                <el-option
                  v-for="item in warningLevelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="vars-row">
          <span class="title">Allowed variables:</span>
          <div class="vars-tags">
            <el-tag
              v-for="item in allowedVariables"
              :key="item"
              round
              effect="light"
              class="var-tag"
              >{{ item }}</el-tag
            >
          </div>
        </div>
      </el-form>

      <span class="title">Title:</span>
      <el-input
        v-model="form.title"
        placeholder="Enter message title"
        clearable
        maxlength="120"
        show-word-limit
      >
      </el-input>

      <!--div
          style="display: flex; gap: 24px; align-items: stretch; flex-wrap: wrap"
        >
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
        </div-->

      <div class="editor-grid">
        <div class="editor-panel">
          <TiptapEditor ref="editorRef" v-model:content="form.body" />
        </div>

        <div class="preview-panel">
          <div class="preview-content" v-html="compiledHTML"></div>
        </div>
      </div>

      <div class="actions-row">
        <el-button @click="triggerSubmit" class="styled-btn">Submit</el-button>
        <el-button @click="triggerDelete" class="styled-btn btn-del"
          >Delete</el-button
        >
      </div>
    </el-card>

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
  </div>
</template>

<style scoped>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  width: 1100px;
  box-sizing: border-box;
}

.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 12px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-del {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.btn-del:hover {
  transform: translateY(-1px);
  background-image: linear-gradient(
    to bottom,
    #782d2d 0%,
    #852e2e 10%,
    #903737 100%
  );
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.card.settings-card {
  padding: 10px 14px 16px;
}

.title {
  display: inline-block;
  font-size: 18px;
  color: #475569;
  font-weight: 600;
  margin-right: 10px;
  margin-bottom: 10px;
}

.title + .el-input {
  margin-bottom: 15px;
}

.filters-form {
  margin-bottom: var(--section-gap);
}
.filters-form :deep(.el-form-item) {
  margin-bottom: 12px;
}
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  background: #fbfcfe;
  border: 1px solid rgba(36, 109, 182, 0.71);
  box-shadow: none;
}
:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  border-color: #cbd5e1;
}
:deep(.is-focus),
:deep(.el-textarea__inner:focus) {
  border-color: #22a3a7 !important;
  box-shadow: 0 0 0 3px rgba(34, 163, 167, 0.16) !important;
  background: #fff !important;
}

.vars-row {
  margin-top: 4px;
}
.vars-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}
.vars-tags .title {
  margin: 0 4px 0 0;
}
.var-tag {
  border-radius: 999px;
  padding: 0 10px;
}

.editor-panel :deep(.ProseMirror) {
  min-height: 520px;
  padding: 16px 18px;
}

.preview-content {
  padding: 22px 26px;
  min-height: 520px;
  color: #0f172a;
}

.preview-content h1,
.preview-content h2,
.preview-content h3 {
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 10px;
}
.preview-content h1 {
  font-size: 28px;
}
.preview-content h2 {
  font-size: 22px;
}
.preview-content p {
  color: #334155;
  line-height: 1.7;
  margin: 10px 0;
}

.actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.editor-grid {
  display: flex;
  gap: 24px;
  align-items: stretch;
  flex-wrap: nowrap;
  overflow-x: auto;
  padding-bottom: 4px;
}

.editor-panel,
.preview-panel {
  flex: 1 0 0;
  min-width: 420px;
  min-height: 520px;
  border-radius: 4px;
}

.preview-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-panel :deep(a) {
  color: #2563eb;
  text-decoration: underline;
}
.preview-panel :deep(a:hover) {
  text-decoration: none;
}
</style>
