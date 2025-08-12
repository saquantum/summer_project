<script setup lang="ts">
import {
  adminGetPermissionByUIDService,
  adminUpdatePermissionService
} from '@/api/admin'
import { useUserStore } from '@/stores'
import type { Permission } from '@/types'
import { onMounted, ref } from 'vue'
const userStore = useUserStore()

const permission = ref<Permission | null>(null)
const checkboxOptions = ref<{ label: string; value: boolean }[]>([])
const selectUserVisible = ref(false)

const submit = async () => {
  if (permission.value) {
    const p = permission.value
    p.canCreateAsset = checkboxOptions.value[0].value
    p.canSetPolygonOnCreate = checkboxOptions.value[1].value
    p.canUpdateAssetPolygon = checkboxOptions.value[2].value
    p.canUpdateAssetFields = checkboxOptions.value[3].value
    p.canDeleteAsset = checkboxOptions.value[4].value
    p.canUpdateProfile = checkboxOptions.value[5].value
    await adminUpdatePermissionService(p)
  }
}

onMounted(async () => {
  if (userStore.user) {
    const res = await adminGetPermissionByUIDService(userStore.user.id)
    permission.value = res.data
  }

  if (permission.value) {
    const p = permission.value
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
  <div>
    <div>global control</div>

    <el-checkbox
      v-for="(item, index) in checkboxOptions"
      :key="index"
      :label="item.label"
      v-model="item.value"
    />
    <div>
      <el-button @click="submit">Submit</el-button>
    </div>
  </div>

  <el-input @click="selectUserVisible = true"></el-input>

  <SelectUserDialog
    v-model="selectUserVisible"
    :multiple="true"
    title="Select user"
  />
</template>
