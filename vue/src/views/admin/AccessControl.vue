<script setup lang="ts">
import {
  adminDeletPermissionGroup,
  adminGetPermissionGroupsService,
  adminInsertPermissionGroupService,
  adminUpdatePermissionGroupService
} from '@/api/admin'
import type { PermissionGroup } from '@/types'
import PageTopTabs from '@/components/PageSurfaceTabs.vue'
import type { RouteLocationNormalized } from 'vue-router'

import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'

const tabsConfig = [
  {
    label: 'Users',
    to: { name: 'AdminAllUsers' },
    match: (r: RouteLocationNormalized) =>
      r.name === 'AdminAllUsers' || r.name === 'AdminUserDetail'
  },
  { label: 'Add User', to: { name: 'AdminAddUser' } },
  {
    label: 'Access Control',
    to: { name: 'AdminAccessControl' },
    match: (r: RouteLocationNormalized) =>
      String(r.path || '').startsWith('/admin/access-control')
  }
]

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
  <div class="page-surface">
    <PageTopTabs :tabs="tabsConfig" />

    <el-table :data="permissions" class="pg-table">
      <el-table-column prop="rowId" label="Row id" width="120" />
      <el-table-column prop="name" label="Name" width="120" />
      <el-table-column prop="description" label="Description" width="120" />
      <el-table-column prop="canCreateAsset" label="Create asset" width="120" />
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
      <el-table-column prop="canDeleteAsset" label="Delete asset" width="120" />
      <el-table-column
        prop="canUpdateProfile"
        label="Update profile"
        width="120"
      />
      <el-table-column label="Actions" width="120">
        <template #default="scope">
          <el-button
            class="styled-btn"
            size="small"
            @click="triggerEdit(scope.row)"
            data-test="trigger-edit"
          >
            Edit
          </el-button>
          <el-button
            class="styled-btn btn-delete"
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
          <el-button
            @click="editDialogVisible = false"
            data-test="cancel"
            class="styled-btn btn-del"
            >Cancel</el-button
          >
          <el-button
            type="primary"
            @click="update"
            data-test="update"
            class="styled-btn"
          >
            Submit
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="createDialogVisible"
      title=" "
      class="pg-dialog create-perm-dialog"
      :append-to-body="true"
      max-width="600px"
    >
      <div class="title">Create permission group</div>
      <el-form
        :model="form"
        :rules="rules"
        label-position="left"
        label-width="auto"
        class="pg-form"
      >
        <div class="perm-radios">
          <el-form-item label="Name" prop="name">
            <el-input v-model="form.name" data-test="input-name" />
          </el-form-item>
          <el-form-item label="Description">
            <el-input
              v-model="form.description"
              data-test="input-description"
            />
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
        </div>

        <div class="perm-card">
          <el-form-item label="Name" prop="name" label-position="top">
            <el-input
              v-model="form.name"
              data-test="input-name"
              class="input"
            />
          </el-form-item>
          <el-form-item label="Description" label-position="top">
            <el-input
              v-model="form.description"
              data-test="input-description"
              class="input"
            />
          </el-form-item>
          <div class="perm-item">
            <span class="perm-label">Create asset</span>
            <el-switch
              v-model="form.canCreateAsset"
              :active-value="true"
              :inactive-value="false"
              data-test="radio-canCreateAsset"
            />
          </div>
          <div class="perm-item">
            <span class="perm-label">Set polygon on create</span>
            <el-switch
              v-model="form.canSetPolygonOnCreate"
              :active-value="true"
              :inactive-value="false"
              data-test="radio-canSetPolygonOnCreate"
            />
          </div>
          <div class="perm-item">
            <span class="perm-label">Update asset fields</span>
            <el-switch
              v-model="form.canUpdateAssetFields"
              :active-value="true"
              :inactive-value="false"
              data-test="radio-canUpdateAssetFields"
            />
          </div>
          <div class="perm-item">
            <span class="perm-label">Update asset polygon</span>
            <el-switch
              v-model="form.canUpdateAssetPolygon"
              :active-value="true"
              :inactive-value="false"
              data-test="radio-canUpdateAssetPolygon"
            />
          </div>
          <div class="perm-item">
            <span class="perm-label">Delete asset</span>
            <el-switch
              v-model="form.canDeleteAsset"
              :active-value="true"
              :inactive-value="false"
              data-test="radio-canDeleteAsset"
            />
          </div>
          <div class="perm-item">
            <span class="perm-label">Update profile</span>
            <el-switch
              v-model="form.canUpdateProfile"
              :active-value="true"
              :inactive-value="false"
              data-test="radio-canUpdateProfile"
            />
          </div>
        </div>
      </el-form>

      <template #footer>
        <div>
          <el-button
            @click="createDialogVisible = false"
            data-test="cancel"
            class="styled-btn btn-del"
            >Cancel</el-button
          >
          <el-button
            type="primary"
            data-test="create"
            @click="createPermissionGroup"
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
      content="This will permanently delete this permission group"
      :countdown-duration="5"
      @confirm="handleDelete"
    />
    <div class="toolbar">
      <el-button
        @click="handleCreate"
        data-test="add-permission-group"
        class="styled-btn"
        >Add permission group</el-button
      >
    </div>
  </div>
</template>

<style scoped>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /*width: 1100px;*/
  width: min(80vw, 1600px);
  max-width: calc(100% - 2rem);
  box-sizing: border-box;
}
.toolbar {
  margin-bottom: 0px;
}
.pg-table :deep(.el-table__cell) {
  vertical-align: middle;
}
.title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 30px;
}
.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 10px;
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
  margin-top: 10px;
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

.btn-del {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.btn-del :hover {
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

.perm-card {
  display: none;
}

.perm-card {
  margin-top: 12px;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
}
.perm-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #f2f4f8;
}
.perm-item:last-child {
  border-bottom: 0;
}
.perm-label {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.3;
  word-break: break-word;
}

@media (max-width: 768px) {
  .page-surface {
    width: min(1100px, calc(100vw - 24px));
    margin: 40px auto;
    padding: 16px;
  }

  .perm-radios {
    display: none;
  }
  .perm-card {
    display: block;
  }
  .title {
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 30px;
  }
}
</style>
