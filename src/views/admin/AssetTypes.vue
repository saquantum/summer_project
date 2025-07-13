<script setup lang="ts">
import { ref } from 'vue'
// import { useRouter } from 'vue-router'
import { useAssetStore } from '@/stores/index'
import {
  adminDeleteAssetTypeService,
  adminInsetAssetTypeService
} from '@/api/admin'
import { type AssetType } from '@/types'

const assetStore = useAssetStore()

const dialogVisible = ref(false)

const form = ref<AssetType>({
  name: '',
  description: ''
})

const submit = async () => {
  await adminInsetAssetTypeService(form.value)
  assetStore.getAssetTypes()
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

  <el-dialog v-model="dialogVisible" title="Tips" width="500">
    <el-form :model="form" label-width="auto">
      <el-form-item label="name">
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="discription">
        <el-input v-model="form.description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submit"> Submit </el-button>
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
