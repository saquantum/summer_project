<script setup lang="ts">
import { adminSendMessageService } from '@/api/admin'
import { useUserStore } from '@/stores'
import { createAssetHolderRules, trimForm } from '@/utils/formUtils'
import { ElMessage } from 'element-plus'
import type { FormItemRule } from 'element-plus'
import { computed, ref } from 'vue'

const form = ref({
  userId: '',
  duration: 0,
  title: '',
  body: ''
})

const userStore = useUserStore()

const rules = {
  userId: createAssetHolderRules(userStore.user?.admin ?? false),
  title: [
    { required: true, message: 'Please input title', trigger: 'blur' },
    {
      min: 1,
      max: 100,
      message: 'Title must be 1-100 characters',
      trigger: 'blur'
    }
  ],
  duration: [
    {
      validator: (
        rule: FormItemRule,
        value: string,
        callback: (_error?: Error) => void
      ) => {
        const durationValue = parseInt(durationInput.value, 10)
        if (!durationInput.value) {
          // Empty is allowed (defaults to 9999999)
          callback()
          return
        }
        if (
          isNaN(durationValue) ||
          durationValue <= 0 ||
          !Number.isInteger(durationValue)
        ) {
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
}

// Separate string field for duration input to avoid the 0 display issue
const durationInput = ref('')

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
      // Form validation failed, errors already shown by Element Plus
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
    ElMessage.success('Message send')
  } catch (e) {
    console.error(e)
    ElMessage.error('Fail to send message')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    label-width="auto"
    label-position="left"
    :rules="rules"
  >
    <el-form-item label="Username" prop="userId">
      <el-input
        v-model="form.userId"
        placeholder="Please input username"
      ></el-input>
    </el-form-item>
    <el-form-item label="Duration" prop="duration">
      <el-input
        type="number"
        v-model="durationInput"
        placeholder="Enter duration in minutes"
        :min="1"
        step="1"
      ></el-input>
    </el-form-item>
    <el-form-item label="Title" prop="title">
      <el-input
        v-model="form.title"
        placeholder="Please input title"
      ></el-input>
    </el-form-item>
  </el-form>

  <div class="editor-container">
    <TiptapEditor ref="editorRef" v-model:content="form.body" />
    <div class="message-preview">
      <div v-html="renderedHTML"></div>
    </div>
  </div>
  <div class="send-button-container">
    <el-button
      @click="handleSend"
      :loading="isLoading"
      :disabled="isLoading"
      type="primary"
    >
      {{ isLoading ? 'Sending...' : 'Send' }}
    </el-button>
  </div>
</template>

<style scoped>
.editor-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.message-preview {
  border: 1px solid #ccc;
  padding: 1rem;
  margin-top: 0;
  min-height: 524px;
  min-width: 500px;
  background-color: white;
}

.send-button-container {
  margin-top: 1rem;
}
</style>
