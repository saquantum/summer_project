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

const multiSort = ref([{ prop: 'id', order: 'ascending' }]) // [{ prop: 'id', order: 'ascending' }]

const handleSortChange = ({ prop, order }) => {
  const index = multiSort.value.findIndex((item) => item.prop === prop)
  if (index !== -1) {
    if (!order) {
      multiSort.value.splice(index, 1)
    } else {
      multiSort.value[index].order = order
    }
  } else {
    if (order) {
      multiSort.value.push({ prop, order })
    }
  }
  fetchTableData()
}

const fetchTableData = async () => {
  const propOrderList = []

  for (const { prop, order } of multiSort.value) {
    let dbField = ''
    if (prop === 'id') dbField = 'asset_id'
    else if (prop === 'assetName') dbField = 'asset_name'
    else if (prop === 'assetHolderId') dbField = 'asset_owner_id'
    else if (prop === 'warningLevel') dbField = 'asset_warning_level'
    else continue

    const sortDir = order === 'descending' ? 'desc' : 'asc'
    propOrderList.unshift(`${dbField},${sortDir}`)
  }

  await assetStore.getAllAssets(
    (currentPage.value - 1) * pageSize.value,
    pageSize.value,
    propOrderList.join(',')
  )
  assets.value = assetStore.allAssets.map((item) => {
    return {
      id: item.asset.id,
      assetName: item.asset.name,
      assetHolderId: item.asset.ownerId,
      warningLevel: item.warnings[0]?.warningLevel.toLowerCase() || 'None'
    }
  })
}

// page setting
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const handlePageChange = (page) => {
  currentPage.value = page
  fetchTableData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTableData()
}

onMounted(async () => {
  await assetStore.getAllAssets(
    (currentPage.value - 1) * pageSize.value,
    pageSize.value,
    'asset_id,asc'
  )
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

  <el-pagination
    background
    layout="total, prev, pager, next, sizes"
    :current-page="currentPage"
    :page-size="pageSize"
    :total="total"
    @current-change="handlePageChange"
    @size-change="handleSizeChange"
  />
</template>

<style>
.el-table .warning-red {
  --el-table-tr-bg-color: var(--el-color-danger-light-8);
}
.el-table .warning-yellow {
  --el-table-tr-bg-color: var(--el-color-warning-light-8);
}
</style>
