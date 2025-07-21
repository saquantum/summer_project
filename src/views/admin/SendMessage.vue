<script setup lang="ts">
import { adminSendMessageService } from '@/api/admin'
import { ref } from 'vue'

const form = ref({
  userId: '',
  duration: 0,
  title: '',
  body: ''
})

const handleSend = async () => {
  form.value.duration = Number(form.value.duration)
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
    <el-form-item label="body" props="body">
      <el-input
        v-model="form.body"
        placeholder="Please input body"
        type="textarea"
        :row="5"
      ></el-input>
    </el-form-item>
  </el-form>
  <el-button @click="handleSend">Send</el-button>
</template>
