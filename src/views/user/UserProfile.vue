<script setup>
import { ref, computed } from 'vue'

import { useRouter } from 'vue-router'
const router = useRouter()
const userFormRef = ref()
const setEdit = (val) => {
  userFormRef.value.setEdit(val)
}
const submit = () => {
  userFormRef.value.submit()
}

const isEdit = computed(() => userFormRef.value?.isEdit || false)
</script>

<template>
  <UserForm ref="userFormRef"></UserForm>

  <!-- operation -->
  <el-button @click="setEdit(true)">Edit</el-button>
  <el-button @click="setEdit(false)">Cancel</el-button>
  <el-button v-if="isEdit" type="primary" @click="submit">Submit</el-button>

  <el-button @click="router.push('/security/verify-mail')">
    Change password</el-button
  >
</template>

<style scoped></style>
