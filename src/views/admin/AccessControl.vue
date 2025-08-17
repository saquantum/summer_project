<script setup lang="ts">
import {
  adminDeletPermissionGroup,
  adminGetPermissionGroupsService,
  adminInsertPermissionGroupService,
  adminUpdatePermissionGroupService
} from '@/api/admin'
import type { PermissionGroup } from '@/types'

import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'

const permissions = ref<PermissionGroup[]>([])

const rules = {
  name: [{ required: true, message: 'Name is required', trigger: 'blur' }]
}
const update = async () => {
  try {
    await adminUpdatePermissionGroupService(form.value)
    fetchTableData()
    ElMessage.success('Permission group updated')
    editDialogVisible.value = false
  } catch {
    ElMessage.error('Fail to update')
  }
}

const createPermissionGroup = async () => {
  try {
    form.value.rowId = ''
    await adminInsertPermissionGroupService(form.value)
    fetchTableData()
    ElMessage.success('Permission group created')
    createDialogVisible.value = false
  } catch {
    ElMessage.error('Fail to create')
  }
}

const editDialogVisible = ref(false)
const createDialogVisible = ref(false)

const form = ref({
  rowId: '',
  name: '',
  description: '',
  canCreateAsset: false,
  canSetPolygonOnCreate: false,
  canUpdateAssetFields: false,
  canUpdateAssetPolygon: false,
  canDeleteAsset: false,
  canUpdateProfile: false
})

const triggerEdit = async (row: PermissionGroup) => {
  editDialogVisible.value = true
  form.value.rowId = row.rowId
  form.value.name = row.name
  form.value.description = row.description
  form.value.canCreateAsset = row.canCreateAsset
  form.value.canSetPolygonOnCreate = row.canSetPolygonOnCreate
  form.value.canUpdateAssetFields = row.canUpdateAssetFields
  form.value.canUpdateAssetPolygon = row.canUpdateAssetPolygon
  form.value.canDeleteAsset = row.canDeleteAsset
  form.value.canUpdateProfile = row.canUpdateProfile
}

const handleCreate = () => {
  form.value = {
    rowId: '',
    name: '',
    description: '',
    canCreateAsset: false,
    canSetPolygonOnCreate: false,
    canUpdateAssetFields: false,
    canUpdateAssetPolygon: false,
    canDeleteAsset: false,
    canUpdateProfile: false
  }
  createDialogVisible.value = true
}

// delete confirm
const deleteDialogVisible = ref(false)
const deleteId = ref<string[]>([])

const triggerDelete = (row: PermissionGroup) => {
  deleteDialogVisible.value = true
  deleteId.value.push(row.rowId)
}

const handleDelete = async () => {
  try {
    await adminDeletPermissionGroup(deleteId.value)
    fetchTableData()
    deleteDialogVisible.value = false
  } catch (e) {
    console.error(e)
  }
}

const fetchTableData = async () => {
  const res = await adminGetPermissionGroupsService()
  if (res && res.data) {
    permissions.value = res.data
  }
}

onMounted(async () => {
  fetchTableData()
})
</script>

<template>
  <div>
    <el-button @click="handleCreate" data-test="add-permission-group"
      >Add permission group</el-button
    >
  </div>

  <el-table :data="permissions">
    <el-table-column prop="rowId" label="Row id" width="120" />
    <el-table-column prop="name" label="Name" width="180" />
    <el-table-column prop="description" label="Description" />
    <el-table-column prop="canCreateAsset" label="Create asset" />
    <el-table-column
      prop="canSetPolygonOnCreate"
      label="set polygon on create"
    />
    <el-table-column prop="canUpdateAssetFields" label="Update asset fields" />
    <el-table-column
      prop="canUpdateAssetPolygon"
      label="Update asset polygon"
    />
    <el-table-column prop="canDeleteAsset" label="Delete asset" />
    <el-table-column prop="canUpdateProfile" label="Update profile" />
    <el-table-column label="Actions">
      <template #default="scope">
        <el-button
          text
          size="small"
          @click="triggerEdit(scope.row)"
          data-test="trigger-edit"
        >
          Edit
        </el-button>
        <el-button
          text
          type="danger"
          size="small"
          data-test="delete"
          @click="triggerDelete(scope.row)"
        >
          Delete
        </el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-dialog
    v-model="editDialogVisible"
    title="Update asset type"
    width="500"
    data-test="dialog-edit"
  >
    <el-form :model="form" label-width="auto">
      <el-form-item label="Id">
        <el-input v-model="form.rowId" disabled data-test="input-id" />
      </el-form-item>
      <el-form-item label="Name">
        <el-input v-model="form.name" data-test="input-name" />
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="form.description" data-test="input-description" />
      </el-form-item>
      <el-form-item label="Create asset">
        <el-radio-group
          v-model="form.canCreateAsset"
          data-test="radio-canCreateAsset"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Set polygon on create">
        <el-radio-group
          v-model="form.canSetPolygonOnCreate"
          data-test="radio-canSetPolygonOnCreate"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Update asset fields">
        <el-radio-group
          v-model="form.canUpdateAssetFields"
          data-test="radio-canUpdateAssetFields"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Update asset polygon">
        <el-radio-group
          v-model="form.canUpdateAssetPolygon"
          data-test="radio-canUpdateAssetPolygon"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Delete asset">
        <el-radio-group
          v-model="form.canDeleteAsset"
          data-test="radio-canDeleteAsset"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Update profile">
        <el-radio-group
          v-model="form.canUpdateProfile"
          data-test="radio-canUpdateProfile"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editDialogVisible = false" data-test="cancel"
          >Cancel</el-button
        >
        <el-button type="primary" @click="update" data-test="update">
          Submit
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog
    v-model="createDialogVisible"
    title="Create permission group"
    width="500"
  >
    <el-form :model="form" :rules="rules" label-width="auto">
      <el-form-item label="Name" prop="name">
        <el-input v-model="form.name" data-test="input-name" />
      </el-form-item>
      <el-form-item label="Description">
        <el-input v-model="form.description" data-test="input-description" />
      </el-form-item>
      <el-form-item label="Create asset">
        <el-radio-group
          v-model="form.canCreateAsset"
          data-test="radio-canCreateAsset"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Set polygon on create">
        <el-radio-group
          v-model="form.canSetPolygonOnCreate"
          data-test="radio-canSetPolygonOnCreate"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Update asset fields">
        <el-radio-group
          v-model="form.canUpdateAssetFields"
          data-test="radio-canUpdateAssetFields"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Update asset polygon">
        <el-radio-group
          v-model="form.canUpdateAssetPolygon"
          data-test="radio-canUpdateAssetPolygon"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Delete asset">
        <el-radio-group
          v-model="form.canDeleteAsset"
          data-test="radio-canDeleteAsset"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Update profile">
        <el-radio-group
          v-model="form.canUpdateProfile"
          data-test="radio-canUpdateProfile"
        >
          <el-radio :value="true">true</el-radio>
          <el-radio :value="false">false</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div>
        <el-button @click="createDialogVisible = false" data-test="cancel"
          >Cancel</el-button
        >
        <el-button
          type="primary"
          data-test="create"
          @click="createPermissionGroup"
        >
          Submit
        </el-button>
      </div>
    </template>
  </el-dialog>

  <ConfirmDialog
    v-model="deleteDialogVisible"
    title="Warning"
    content="This will permanently delete this permission group"
    :countdown-duration="5"
    @confirm="handleDelete"
  />
</template>
