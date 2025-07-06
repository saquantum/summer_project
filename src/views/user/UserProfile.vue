<script setup lang="ts">
import { ref } from 'vue'

import { useRouter } from 'vue-router'
const router = useRouter()
const userFormRef = ref()

const submit = () => {
  userFormRef.value.submit()
}

const isEdit = ref(false)
</script>

<template>
  <UserForm ref="userFormRef" v-model:isEdit="isEdit"></UserForm>

  <!-- operation -->
  <el-button @click="isEdit = true" v-if="!isEdit">Edit</el-button>
  <el-button @click="isEdit = false" v-else>Cancel</el-button>
  <el-button v-if="isEdit" type="primary" @click="submit">Submit</el-button>

  <el-button @click="router.push('/security/verify-mail')">
    Change password</el-button
  >
</template>

<style scoped></style>
