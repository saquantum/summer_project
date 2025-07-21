<script setup lang="ts">
import { adminSendMessageService } from '@/api/admin'
import { computed, ref } from 'vue'

const form = ref({
  userId: '',
  duration: 0,
  title: '',
  body: ''
})

const editorRef = ref()

const renderedHTML = computed(() => {
  if (!editorRef.value) return ''
  return editorRef.value.renderedHTML
})

const handleSend = async () => {
  form.value.duration = Number(form.value.duration)
  form.value.body = renderedHTML.value
  const res = await adminSendMessageService(form.value)
  console.log(res)
}
</script>

<template>
  <el-form label-width="auto" label-position="left">
    <el-form-item label="username" props="userId">
      <el-input
        v-model="form.userId"
        placeholder="Please input username"
      ></el-input>
    </el-form-item>
    <el-form-item label="duration" porps="duration">
      <el-input type="number" v-model="form.duration"></el-input>
    </el-form-item>
    <el-form-item label="title" props="title">
      <el-input
        v-model="form.title"
        placeholder="Please input title"
      ></el-input>
    </el-form-item>
  </el-form>

  <div style="display: flex; gap: 24px; align-items: flex-start">
    <TiptapEditor ref="editorRef" v-model:content="form.body" />
    <div
      class="preview"
      style="
        border: 1px solid #ccc;
        padding: 1rem;
        margin-top: 0;
        min-height: 524px;
        min-width: 500px;
        background-color: white;
      "
    >
      <div v-html="renderedHTML"></div>
    </div>
  </div>
  <div>
    <el-button @click="handleSend">Send</el-button>
  </div>
</template>
