<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/index.ts'
import {
  adminGetPermissionByUIDService,
  adminGetUserInfoService,
  adminUpdatePermissionService
} from '@/api/admin'
import type { Permission } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = ref()

const userFormRef = ref()
const permission = ref<Permission[] | null>(null)
const checkboxOptions = ref<{ label: string; value: boolean }[]>([])

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
  userFormRef.value.submit()

  if (permission.value && permission.value.length > 0) {
    const p = permission.value[0]
    p.canCreateAsset = checkboxOptions.value[0].value
    p.canSetPolygonOnCreate = checkboxOptions.value[1].value
    p.canUpdateAssetPolygon = checkboxOptions.value[2].value
    p.canUpdateAssetFields = checkboxOptions.value[3].value
    p.canDeleteAsset = checkboxOptions.value[4].value
    p.canUpdateProfile = checkboxOptions.value[5].value
    console.log(p)
    await adminUpdatePermissionService(p)
  }
}
onMounted(async () => {
  const id = route.query.id
  if (typeof id === 'string') {
    const res = await adminGetUserInfoService(id)
    user.value = res.data
  }

  const res = await adminGetPermissionByUIDService(user.value.id)
  permission.value = res.data

  if (permission.value && permission.value.length > 0) {
    const p = permission.value[0]
    checkboxOptions.value = [
      { label: 'Add new asset', value: p.canCreateAsset },
      { label: 'Add polygon', value: p.canSetPolygonOnCreate },
      { label: 'Update polygon', value: p.canUpdateAssetPolygon },
      { label: 'Update basic information', value: p.canUpdateAssetFields },
      { label: 'Delete asset', value: p.canDeleteAsset },
      { label: 'Update profile', value: p.canUpdateProfile }
    ]
  }
})
</script>

<template>
  <UserForm ref="userFormRef" v-model:isEdit="isEdit"></UserForm>
  <div>
    <h3>Permission</h3>
    <el-checkbox
      :disabled="!isEdit"
      v-for="(item, index) in checkboxOptions"
      :key="index"
      :label="item.label"
      v-model="item.value"
    />
  </div>

  <el-button @click="proxyUser" v-if="!isEdit"> Proxy as this user</el-button>
  <el-button @click="isEdit = true" v-if="!isEdit">Edit</el-button>
  <el-button @click="isEdit = false" v-else>Cancel</el-button>
  <el-button v-if="isEdit" @click="submit">Submit</el-button>
  <el-button type="danger" v-if="isEdit"> Delete </el-button>
</template>
