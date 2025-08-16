<script setup lang="ts">
import { adminSendMessageService } from '@/api/admin'
import { trimForm } from '@/utils/formUtils'
import { ElMessage } from 'element-plus'
import type { FormItemRule } from 'element-plus'
import { computed, ref } from 'vue'

const form = ref({
  userId: '',
  duration: 0,
  title: '',
  body: ''
})

// Separate string field for duration input to avoid the 0 display issue
const durationInput = ref('')

const rules = computed(() => ({
  userId: sendToAll.value
    ? []
    : [
        {
          required: true,
          message: 'Please input username',
          trigger: 'blur'
        },
        {
          validator: (
            rule: FormItemRule,
            value: string,
            callback: (_error?: Error) => void
          ) => {
            const ids = form.value.userId.split(',')
            if (!ids) {
              callback(new Error('Invalid format'))
            } else {
              callback()
            }
          },
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

const sendToAll = ref(false)

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

  let requestBody
  if (sendToAll.value) {
    requestBody = {
      ...form.value,
      filters: {}
    }
  } else {
    const ids = form.value.userId.replace(/\s+/g, '').split(',')
    requestBody = {
      ...form.value,
      filters: {
        user_id: {
          op: 'in',
          list: ids
        }
      }
    }
  }

  try {
    await adminSendMessageService(requestBody)
    ElMessage.success('Message sent')
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
        data-test="username-input"
        placeholder="Split username with ','"
      ></el-input>
    </el-form-item>
    <el-form-item label="Duration" prop="duration">
      <el-input
        type="number"
        v-model="durationInput"
        data-test="duration-input"
        placeholder="Enter duration in minutes (integers only)"
        :min="1"
        step="1"
      ></el-input>
    </el-form-item>
    <el-form-item label="Title" prop="title">
      <el-input
        v-model="form.title"
        data-test="title-input"
        placeholder="Please input title"
      ></el-input>
    </el-form-item>

    <el-form-item label="Send to all" prop="sendToAll">
      <el-checkbox
        v-model="sendToAll"
        data-test="send-to-all-checkbox"
      ></el-checkbox>
    </el-form-item>
  </el-form>

  <div style="display: flex; gap: 24px; align-items: stretch; flex-wrap: wrap">
    <div
      data-test="editor-container"
      style="flex: 1; min-width: 300px; height: 564px"
    >
      <TiptapEditor ref="editorRef" v-model:content="form.body" />
    </div>
    <div
      data-test="message-preview"
      class="message-preview"
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
      <div v-html="renderedHTML"></div>
    </div>
  </div>
  <div class="send-button-container">
    <el-button
      @click="handleSend"
      v-bind="isLoading ? { loading: true } : {}"
      :disabled="isLoading"
      type="primary"
      data-test="send-button"
    >
      Send
    </el-button>
  </div>
</template>

<style scoped>
.send-button-container {
  margin-top: 1rem;
}
</style>
