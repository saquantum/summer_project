<script setup lang="ts">
import { adminSendMessageService } from '@/api/admin'
import { trimForm } from '@/utils/formUtils'
import { ElMessage } from 'element-plus'
import type { FormItemRule } from 'element-plus'
import { computed, ref } from 'vue'
import PageTopTabs from '@/components/PageSurfaceTabs.vue'
import type { RouteLocationNormalized } from 'vue-router'

const tabsConfig = [
  {
    label: 'Send Message',
    to: { name: 'AdminSendMessage' },
    match: (r: RouteLocationNormalized) => r.name === 'AdminSendMessage'
  },
  {
    label: 'Manage  Template',
    to: { name: 'AdminMessageTemplate' },
    match: (r: RouteLocationNormalized) => r.name === 'AdminMessageTemplate'
  }
]

const form = ref({
  users: '',
  duration: 0,
  title: '',
  body: ''
})

// Separate string field for duration input to avoid the 0 display issue
const durationInput = ref('')

const rules = computed(() => ({
  users: [
    {
      required: true,
      message: 'Please input username',
      trigger: 'blur'
    }
  ],
  title: [{ required: true, message: 'Please input title', trigger: 'blur' }],
  duration: [
    {
      validator: (
        rule: FormItemRule,
        value: string,
        callback: (_error?: Error) => void
      ) => {
        if (!durationInput.value) {
          callback()
          return
        }
        const durationValue = parseFloat(durationInput.value)
        if (durationValue <= 0 || !Number.isInteger(durationValue)) {
          callback(
            new Error('Please enter a valid duration (positive integer)')
          )
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}))

const editorRef = ref()
const isLoading = ref(false)
const formRef = ref()

const renderedHTML = computed(() => {
  if (!editorRef.value) return ''
  return editorRef.value.renderedHTML
})

const handleSend = async () => {
  // Prevent multiple simultaneous requests
  if (isLoading.value) return

  // Validate form first
  if (formRef.value) {
    try {
      await formRef.value.validate()
    } catch {
      return
    }
  }

  // Convert the string input to integer, handle empty string case
  const duration = durationInput.value
    ? parseInt(durationInput.value, 10)
    : 9999999

  form.value.duration = duration
  form.value.body = renderedHTML.value

  isLoading.value = true

  trimForm(form.value)
  try {
    await formRef.value.validate()
  } catch (fields) {
    if (fields) {
      const firstErrorField = Object.keys(fields)[0]
      formRef.value.scrollToField(firstErrorField)
      return
    }
  }

  try {
    await adminSendMessageService(form.value)
    ElMessage.success('Message sent')
  } catch (e) {
    console.error(e)
    ElMessage.error('Fail to send message')
  } finally {
    isLoading.value = false
  }
}

const selectUserDialogVisible = ref(false)
const selectAll = ref(false)

const requestBody = ref<{ filters: Filters }>({ filters: {} })

interface Filters {
  user_id?: { op: string; val: string } | { list: string[] }
  user_name?: { op: string; val: string }
}

const handleSelect = ({ filters }: { filters: Filters }) => {
  requestBody.value = { filters: filters }
  if (selectAll.value) {
    if (filters.user_id && 'op' in filters.user_id) {
      form.value.users = `All users where ID ${filters.user_id.op} "${filters.user_id.val.replace(/%/g, '')}"`
    } else if (filters.user_name) {
      form.value.users = `All users where Name ${filters.user_name.op} "${filters.user_name.val.replace(/%/g, '')}"`
    } else {
      form.value.users = 'All users'
    }
  } else {
    if (filters.user_id && 'list' in filters.user_id) {
      const list = filters.user_id.list.filter(Boolean)
      const displayed = list.slice(0, 2).join(', ')
      form.value.users =
        list.length > 2 ? `${displayed} (${list.length} in total)` : displayed
    } else {
      form.value.users = ''
    }
  }
  selectUserDialogVisible.value = false
}
</script>

<template>
  <div class="page-surface">
    <PageTopTabs :tabs="tabsConfig" />

    <SelectUserDialog
      v-model:selectAll="selectAll"
      v-model:visible="selectUserDialogVisible"
      @confirm="handleSelect"
    ></SelectUserDialog>

    <el-card class="card settings-card">
      <el-form
        ref="formRef"
        :model="form"
        label-width="auto"
        label-position="left"
        :rules="rules"
        class="filters-form"
      >
        <span class="title">Message settings:</span>
        <el-row :gutter="12">
          <el-col :xs="24" :sm="8">
            <el-form-item label="Select users" prop="users">
              <el-input
                data-test="users"
                v-model="form.users"
                readonly
                @click="selectUserDialogVisible = true"
              ></el-input>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="8">
            <el-form-item
              label="Duration"
              prop="duration"
              class="free-label-row"
            >
              <el-input
                type="number"
                v-model="durationInput"
                data-test="duration-input"
                placeholder="Enter duration in minutes (integers only)"
                :min="1"
                step="1"
              ></el-input>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="8">
            <el-form-item label="Title" prop="title">
              <el-input
                v-model="form.title"
                data-test="title-input"
                placeholder="Please input title"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="editor-grid">
          <div class="editor-panel" data-test="editor-container">
            <TiptapEditor ref="editorRef" v-model:content="form.body" />
          </div>

          <div class="preview-panel" data-test="message-preview">
            <div class="preview-content" v-html="renderedHTML"></div>
          </div>
        </div>

        <div class="actions-row">
          <el-button
            @click="handleSend"
            v-bind="isLoading ? { loading: true } : {}"
            :disabled="isLoading"
            type="primary"
            data-test="send-button"
            class="styled-btn"
          >
            Send
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.send-button-container {
  margin-top: 1rem;
}
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /*width: 1100px;*/
  width: min(80vw, 1600px);
  max-width: calc(100% - 2rem);
  box-sizing: border-box;
}

.card.settings-card {
  padding: 10px 14px 16px;
}

.title {
  display: inline-block;
  font-size: 18px;
  color: #475569;
  font-weight: 600;
  margin-bottom: 10px;
  margin-top: -10px;
}

.filters-form {
  margin-bottom: 14px;
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

.free-label-row :deep(.el-form-item__label) {
  width: auto !important;
}
.free-label-row :deep(.el-form-item__content) {
  margin-left: 0 !important;
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

.editor-panel :deep(.ProseMirror) {
  min-height: 520px;
  padding: 16px 18px;
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

.actions-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
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
</style>
