<script setup lang="ts">
import { adminGetPermissionGroupsService } from '@/api/admin'

import type { PermissionGroup } from '@/types'

import { onMounted, ref } from 'vue'

const props = defineProps<{
  total: number
}>()

const visible = defineModel('permissionDialogVisible')
const selectAll = defineModel('selectAll')

const permissions = ref<PermissionGroup[]>([])

const currentRow = ref<PermissionGroup | null>(null)
const handleCurrentChange = (val: PermissionGroup) => {
  currentRow.value = val
}

const fetchTableData = async () => {
  const res = await adminGetPermissionGroupsService()
  if (res && res.data) {
    permissions.value = res.data
  }
}

const emit = defineEmits(['update-permission-group'])

onMounted(async () => {
  fetchTableData()
})
</script>

<template>
  <el-dialog v-model="visible">
    <span>Select All ({{ props.total }})</span>
    <el-checkbox v-model="selectAll"></el-checkbox>
    <el-table
      :data="permissions"
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column prop="rowId" label="Row id" width="120" />
      <el-table-column prop="name" label="Name" width="180" />
      <el-table-column prop="description" label="Description" />
      <el-table-column prop="canCreateAsset" label="Create asset" />
      <el-table-column
        prop="canSetPolygonOnCreate"
        label="set polygon on create"
      />
      <el-table-column
        prop="canUpdateAssetFields"
        label="Update asset fields"
      />
      <el-table-column
        prop="canUpdateAssetPolygon"
        label="Update asset polygon"
      />
      <el-table-column prop="canDeleteAsset" label="Delete asset" />
      <el-table-column prop="canUpdateProfile" label="Update profile" />
    </el-table>
    <template #footer>
      <div>
        <el-button @click="visible = false">Cancel</el-button>
        <el-button
          type="primary"
          @click="emit('update-permission-group', currentRow?.name ?? '')"
          >Confirm</el-button
        >
      </div>
    </template>
  </el-dialog>
</template>
