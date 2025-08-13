<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/index.ts'
import { adminGetUserInfoService } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = ref()

const userCardRef = ref()

const proxyUser = () => {
  // goto user interface
  const id = route.query.id
  if (typeof id === 'string') {
    userStore.setProxyId(id)
    router.push('/assets')
  }
}

const isEdit = ref(false)
const submit = async () => {
  userCardRef.value.submit()
}

const handleCancel = () => {
  userCardRef.value.cancelEdit()
}

onMounted(async () => {
  const id = route.query.id
  if (typeof id === 'string') {
    const res = await adminGetUserInfoService(id)
    user.value = res.data
  }
})
</script>

<template>
  <UserCard ref="userCardRef" v-model:isEdit="isEdit"></UserCard>

  <el-button @click="proxyUser" v-if="!isEdit"> Proxy as this user</el-button>
  <el-button @click="isEdit = true" v-if="!isEdit">Edit</el-button>
  <el-button @click="handleCancel" v-else>Cancel</el-button>
  <el-button v-if="isEdit" @click="submit">Submit</el-button>
  <el-button type="danger" v-if="isEdit"> Delete </el-button>
</template>
