<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAssetStore } from '@/stores'
const assets = ref([])
const router = useRouter()
const assetStore = useAssetStore()

const handleShowDetail = (row) => {
  router.push(`/asset/${row.id}`)
}

const tableRowClassName = (scope) => {
  if (scope.row.warningLevel.toLowerCase().includes('red')) {
    return 'warning-red'
  } else if (scope.row.warningLevel.toLowerCase().includes('yellow')) {
    return 'warning-yellow'
  }
}

const currentSort = ref({ prop: '', order: '' })
const handleSortChange = ({ prop, order }) => {
  currentSort.value = { prop, order }
  fetchTableData()
}

const fetchTableData = () => {
  const { prop, order } = currentSort.value
  console.log(prop, order)
}
onMounted(async () => {
  await assetStore.getAllAssets()
  assets.value = assetStore.allAssets.map((item) => {
    return {
      id: item.asset.id,
      assetName: item.asset.name,
      assetHolderId: item.asset.ownerId,
      warningLevel: item.warnings[0]?.warningLevel.toLowerCase() || 'None'
    }
  })
})
</script>

<template>
  <el-table
    :data="assets"
    :row-class-name="tableRowClassName"
    @sort-change="handleSortChange"
  >
    <el-table-column prop="id" label="Asset id" sortable="custom" width="180" />
    <el-table-column
      prop="assetName"
      label="Asset Name"
      sortable="custom"
      width="180"
    />
    <el-table-column
      prop="assetHolderId"
      label="Asset Holder ID"
      sortable="custom"
      width="180"
    />
    <el-table-column
      prop="warningLevel"
      label="Warning Level"
      sortable="custom"
      width="180"
    />
    <el-table-column label="Actions">
      <template #default="scope">
        <el-button
          text
          type="primary"
          size="small"
          @click="handleShowDetail(scope.row)"
          >Show Detail</el-button
        >
        <el-button
          text
          type="danger"
          size="small"
          @click="handleDelete(scope.row)"
          >Delete</el-button
        >
      </template>
    </el-table-column>
  </el-table>
  <div></div>
</template>

<style>
.el-table .warning-red {
  --el-table-tr-bg-color: var(--el-color-danger-light-8);
}
.el-table .warning-yellow {
  --el-table-tr-bg-color: var(--el-color-warning-light-8);
}
</style>
