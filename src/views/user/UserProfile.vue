<script setup lang="ts">
import { useUserStore } from '@/stores'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = useRouter()
const userFormRef = ref()
const userStore = useUserStore()

const user = computed(() => {
  if (!userStore.user) throw new Error('user is null')
  return userStore.user
})
const submit = () => {
  userFormRef.value.submit()
}

const isEdit = ref(false)

const handleEdit = () => {
  if (user.value.admin) {
    isEdit.value = true
  } else {
    if (user.value.permissionConfig.canUpdateProfile) {
      isEdit.value = true
    } else {
      ElMessage.error('Can not update profile')
    }
  }
}

const isDisabled = computed(() => {
  if (user.value.admin) {
    return false
  } else {
    return !userStore.user?.permissionConfig.canUpdateProfile
  }
})

defineExpose({
  isEdit,
  isDisabled,
  user,
  submit,
  handleEdit,
  userFormRef
})
</script>

<template>
  <UserForm ref="userFormRef" v-model:isEdit="isEdit"></UserForm>

  <!-- operation -->
  <el-button
    @click="handleEdit"
    v-if="!isEdit"
    :class="{
      'is-disabled': isDisabled
    }"
    >Edit</el-button
  >
  <el-button @click="isEdit = false" v-else>Cancel</el-button>
  <el-button v-if="isEdit" type="primary" @click="submit">Submit</el-button>

  <el-button v-if="!isEdit" @click="router.push('/security/verify-mail')">
    Change password</el-button
  >
</template>

<style scoped>
.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
