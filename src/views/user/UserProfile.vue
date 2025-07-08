<script setup lang="ts">
import { useUserStore } from '@/stores'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = useRouter()
const userFormRef = ref()
const userStore = useUserStore()

const submit = () => {
  userFormRef.value.submit()
}

const isEdit = ref(false)

const handleEdit = () => {
  if (userStore.user?.permissionConfig.canUpdateProfile) {
    isEdit.value = true
  }
  ElMessage.error('Can not update profile')
}
</script>

<template>
  <UserForm ref="userFormRef" v-model:isEdit="isEdit"></UserForm>

  <!-- operation -->
  <el-button
    @click="handleEdit"
    v-if="!isEdit"
    :class="{
      'is-disabled': !userStore.user?.permissionConfig.canUpdateProfile
    }"
    >Edit</el-button
  >
  <el-button @click="isEdit = false" v-else>Cancel</el-button>
  <el-button v-if="isEdit" type="primary" @click="submit">Submit</el-button>

  <el-button @click="router.push('/security/verify-mail')">
    Change password</el-button
  >
</template>

<style scoped>
.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
