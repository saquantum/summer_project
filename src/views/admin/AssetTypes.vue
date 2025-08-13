<script setup lang="ts">
import { ref } from 'vue'
// import { useRouter } from 'vue-router'
import { useAssetStore } from '@/stores/index'
import {
  adminDeleteAssetTypeService,
  adminInsetAssetTypeService,
  adminUpdateAssetTypeService
} from '@/api/admin'
import { type AssetType } from '@/types'

const assetStore = useAssetStore()

const dialogVisible = ref(false)
const editDialogVisible = ref(false)

const form = ref<AssetType>({
  id: '',
  name: '',
  description: ''
})

const addAssetType = async () => {
  console.log(form.value)
  await adminInsetAssetTypeService(form.value)
  assetStore.getAssetTypes()
  dialogVisible.value = false
  form.value = { name: '', description: '' }
}

const triggerEdit = async (row: AssetType) => {
  editDialogVisible.value = true
  form.value.id = row.id
}

const handleEdit = async () => {
  await adminUpdateAssetTypeService(form.value)
  assetStore.getAssetTypes()
  dialogVisible.value = false
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

const handleDelete = async (row: AssetType) => {
  console.log(row)
  try {
    await adminDeleteAssetTypeService(deleteId.value)
    assetStore.getAssetTypes()
    deleteDialogVisible.value = false
  } catch (e) {
    console.error(e)
  }
}
</script>
<template>
  <el-button @click="dialogVisible = true">Add asset type</el-button>
  <el-table :data="assetStore.assetTypes" class="table">
    <el-table-column type="selection"> </el-table-column>
    <el-table-column prop="id" label="Type ID" width="120" />
    <el-table-column prop="name" label="Type Name" width="180" />
    <el-table-column prop="description" label="Description" />
    <el-table-column label="Actions">
      <template #default="scope">
        <el-button text size="small" @click="triggerEdit(scope.row)">
          Edit
        </el-button>
        <el-button
          text
          type="danger"
          size="small"
          @click="triggerDelete(scope.row)"
        >
          Delete
        </el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog v-model="dialogVisible" title="Add asset type" width="500">
    <el-form :model="form" label-width="auto">
      <el-form-item label="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="discription">
        <el-input v-model="form.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="addAssetType"> Submit </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="editDialogVisible" title="Update asset type" width="500">
    <el-form :model="form" label-width="auto">
      <el-form-item label="id">
        <el-input v-model="form.id" disabled="true" />
      </el-form-item>
      <el-form-item label="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="discription">
        <el-input v-model="form.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="handleEdit"> Submit </el-button>
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
</template>
