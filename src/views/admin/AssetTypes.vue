<script setup lang="ts">
import { ref } from 'vue'
import { useAssetStore } from '@/stores/index'
import {
  adminDeleteAssetTypeService,
  adminInsetAssetTypeService,
  adminUpdateAssetTypeService
} from '@/api/admin'
import { type AssetType } from '@/types'
import { trimForm } from '@/utils/formUtils'
import PageTopTabs from '@/components/PageSurfaceTabs.vue'
import type { RouteLocationNormalized } from 'vue-router'

const tabsConfig = [
  {
    label: 'All Assets',
    to: { name: 'AdminAllAssets' },
    match: (r: RouteLocationNormalized) => r.name === 'AdminAllAssets'
  },
  { label: 'Add Assets', to: { name: 'AdminAddAsset' } },
  {
    label: 'Asset Types',
    to: { name: 'AdminAssetTypes' },
    match: (r: RouteLocationNormalized) =>
      r.path?.startsWith('/admin/assets/types')
  }
]

const assetStore = useAssetStore()

const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

const form = ref<AssetType>({
  id: '',
  name: '',
  description: ''
})

const addAssetType = async () => {
  trimForm(form.value)
  await adminInsetAssetTypeService(form.value)
  assetStore.getAssetTypes()
  addDialogVisible.value = false
  form.value = { id: '', name: '', description: '' }
}

const triggerAdd = () => {
  form.value = {
    id: '',
    name: '',
    description: ''
  }
  addDialogVisible.value = true
}

const triggerEdit = async (row: AssetType) => {
  editDialogVisible.value = true
  form.value.id = row.id
  form.value.name = row.name
  form.value.description = row.description
}

const handleEdit = async () => {
  await adminUpdateAssetTypeService(form.value)
  assetStore.getAssetTypes()
  addDialogVisible.value = false
  form.value = { id: '', name: '', description: '' }
  editDialogVisible.value = false
}

// delete confirm
const deleteDialogVisible = ref(false)
const deleteId = ref<string[]>([])

const triggerDelete = (row: AssetType) => {
  deleteDialogVisible.value = true
  deleteId.value.push(row.id as string)
}

const handleDelete = async () => {
  try {
    await adminDeleteAssetTypeService(deleteId.value)
    assetStore.getAssetTypes()
    deleteDialogVisible.value = false
  } catch (e) {
    console.error(e)
  }
}

defineExpose({
  deleteDialogVisible
})
</script>
<template>
  <div class="page-surface">
    <PageTopTabs :tabs="tabsConfig" />
    <el-button @click="triggerAdd" data-test="add-btn" class="styled-btn"
      >Add asset type</el-button
    >
    <el-table
      :data="assetStore.assetTypes"
      class="table-card balanced-table"
      stripe
      :table-layout="'fixed'"
    >
      <el-table-column prop="id" label="Type ID" />
      <el-table-column prop="name" label="Type Name" />
      <el-table-column prop="description" label="Description" />
      <el-table-column label="Actions" width="160">
        <template #default="scope">
          <el-button
            class="btn-edit"
            size="small"
            @click="triggerEdit(scope.row)"
            data-test="edit"
          >
            Edit
          </el-button>
          <el-button
            class="btn-del"
            type="danger"
            size="small"
            @click="triggerDelete(scope.row)"
            data-test="delete"
          >
            Delete
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="addDialogVisible"
      title="Add asset type"
      width="500"
      data-test="add-dialog"
    >
      <el-form :model="form" label-width="auto">
        <el-form-item label="name">
          <el-input v-model="form.name" data-test="name" />
        </el-form-item>
        <el-form-item label="discription">
          <el-input v-model="form.description" data-test="description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div>
          <el-button
            @click="addDialogVisible = false"
            data-test="cancel"
            class="styled-btn btn-del"
            >Cancel</el-button
          >
          <el-button
            type="primary"
            @click="addAssetType"
            data-test="submit"
            class="styled-btn"
          >
            Submit
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      title="Update asset type"
      width="500"
      data-test="edit-dialog"
    >
      <el-form :model="form" label-width="auto">
        <el-form-item label="id">
          <el-input v-model="form.id" disabled />
        </el-form-item>
        <el-form-item label="name">
          <el-input v-model="form.name" data-test="name" />
        </el-form-item>
        <el-form-item label="description">
          <el-input v-model="form.description" data-test="description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div>
          <el-button
            @click="editDialogVisible = false"
            data-test="cancel"
            class="styled-btn btn-del"
            >Cancel</el-button
          >
          <el-button
            type="primary"
            @click="handleEdit"
            data-test="submit"
            class="styled-btn"
          >
            Submit
          </el-button>
        </div>
      </template>
    </el-dialog>

    <ConfirmDialog
      v-model="deleteDialogVisible"
      title="Warning"
      content="This will permanently delete this asset type"
      :countdown-duration="5"
      @confirm="handleDelete"
    />
  </div>
</template>

<style>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  width: 1100px;
  box-sizing: border-box;
}

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
  margin-bottom: 20px;
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

.table-card {
  border-radius: 8px;
}

.btn-del {
  padding: 8px 10px;
  border-radius: 8px;
  border: none;
  color: rgba(104, 5, 5, 0.72);
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgba(234, 215, 184, 0.5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
}

.btn-edit {
  padding: 8px 10px;
  border-radius: 8px;
  color: #ffffff;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgba(122, 164, 194, 0.78);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
}
.balanced-table :deep(.el-table__cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.balanced-table :deep(.el-button--text) {
  padding: 0 6px;
}
</style>
