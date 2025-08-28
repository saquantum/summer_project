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
  try {
    const res = await adminGetPermissionGroupsService()
    if (res && res.data) {
      permissions.value = res.data
    }
  } catch (error) {
    console.error('Failed to fetch permission groups:', error)
  }
}

const emit = defineEmits(['update-permission-group'])

onMounted(async () => {
  await fetchTableData()
})
</script>

<template>
  <el-dialog
    v-model="visible"
    modal-class="confirm-dialog-overlay"
    class="confirm-dialog"
    style="width: auto; margin: 80px 50px; min-width: 200px"
  >
    <el-checkbox v-model="selectAll" data-test="select-all">
      Select All ({{ props.total }})
    </el-checkbox>
    <el-table
      :data="permissions"
      highlight-current-row
      @current-change="handleCurrentChange"
    >
      <el-table-column prop="rowId" label="Row id" width="120" />
      <el-table-column prop="name" label="Name" width="180" />
      <el-table-column prop="description" label="Description" width="180" />
      <el-table-column prop="canCreateAsset" label="Create asset" width="180" />
      <el-table-column
        prop="canSetPolygonOnCreate"
        label="set polygon on create"
        width="180"
      />
      <el-table-column
        prop="canUpdateAssetFields"
        label="Update asset fields"
        width="180"
      />
      <el-table-column
        prop="canUpdateAssetPolygon"
        label="Update asset polygon"
        width="180"
      />
      <el-table-column prop="canDeleteAsset" label="Delete asset" width="180" />
      <el-table-column
        prop="canUpdateProfile"
        label="Update profile"
        width="180"
      />
    </el-table>

    <template #footer>
      <div>
        <el-button
          @click="visible = false"
          data-test="cancel"
          class="styled-btn btn-cancel"
          >Cancel</el-button
        >
        <el-button
          data-test="confirm"
          type="primary"
          @click="emit('update-permission-group', currentRow?.name ?? '')"
          class="styled-btn"
          >Confirm</el-button
        >
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 12px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.btn-cancel:hover {
  transform: translateY(-1px);
  background-image: linear-gradient(
    to bottom,
    #782d2d 0%,
    #852e2e 10%,
    #903737 100%
  );
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog.confirm-dialog) {
  border-radius: 16px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

:deep(.el-dialog.confirm-dialog .el-dialog__header) {
  padding: 16px 20px 8px;
  border-bottom: 1px solid #eef2f7;
}
:deep(.el-dialog.confirm-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.2px;
}
:deep(.el-dialog.confirm-dialog .el-dialog__body) {
  padding: 16px 20px;
  color: #334155;
  line-height: 1.7;
  background: #fff;
}
:deep(.el-dialog.confirm-dialog .el-dialog__footer) {
  padding: 14px 16px 16px;
  background: #fafbfd;
  border-top: 1px solid #eef2f7;
}

:deep(.el-overlay.confirm-dialog-overlay) {
  backdrop-filter: blur(2px);
  background-color: rgba(15, 23, 42, 0.35);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .confirm-dialog {
    display: flex;
    flex-direction: column;
    min-height: 100dvh !important;
    width: 100%;
    box-sizing: border-box;
    padding: 16px 16px calc(88px + env(safe-area-inset-bottom));
    gap: 16px;
    -webkit-overflow-scrolling: touch;
  }
}
</style>
