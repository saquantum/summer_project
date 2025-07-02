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

const columns = [
  { prop: 'id', label: 'Asset Id', width: 180 },
  { prop: 'assetName', label: 'Asset Name', width: 180 },
  { prop: 'assetHolderId', label: 'Asset Holder ID', width: 180 },
  { prop: 'type', label: 'Type', width: 180 },
  { prop: 'capacityLitres', label: 'Capacity litres', width: 180 },
  { prop: 'material', label: 'Material', width: 180 },
  { prop: 'status', label: 'Status', width: 180 },
  { prop: 'installedAt', label: 'Installed At', width: 180 },
  { prop: 'lastInspection', label: 'Last inspection', width: 180 }
]

const multiSort = ref([])

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
    else if (prop === 'warningLevel') dbField = 'warning_level'
    else if (prop === 'type') dbField = 'asset_type_id'
    else if (prop === 'capacityLitres') dbField = 'asset_capacity_litres'
    else if (prop === 'material') dbField = 'asset_material'
    else if (prop === 'status') dbField = 'asset_status'
    else if (prop === 'installedAt') dbField = 'asset_installed_at'
    else if (prop === 'lastInspection') dbField = 'asset_last_inspection'
    // if no field, skip
    else continue

    const sortDir = order === 'descending' ? 'desc' : 'asc'
    propOrderList.push(`${dbField},${sortDir}`)
  }

  // order by asset_id asc by default
  const sortStr =
    propOrderList.length > 0 ? propOrderList.join(',') : 'asset_id,asc'

  await assetStore.getAllAssets(
    (currentPage.value - 1) * pageSize.value,
    pageSize.value,
    sortStr
  )
  assets.value = assetStore.allAssets.map((item) => {
    return {
      id: item.asset.id,
      assetName: item.asset.name,
      type: item.asset.type.name,
      capacityLitres: item.asset.capacityLitres,
      material: item.asset.material,
      status: item.asset.status,
      installedAt: item.asset.installedAt,
      lastInspection: item.asset.lastInspection,
      assetHolderId: item.asset.ownerId,
      warningLevel: item.warnings[0]?.warningLevel.toLowerCase() || 'None'
    }
  })
}

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
  await fetchTableData()
})
</script>

<template>
  <div>
    <FilterSearch></FilterSearch>
    <SortTool
      v-model:multiSort="multiSort"
      :columns="columns"
      :fetch-table-data="fetchTableData"
    ></SortTool>

    <el-table
      :data="assets"
      :row-class-name="tableRowClassName"
      @sort-change="handleSortChange"
      :default-sort="multiSort[0] || {}"
    >
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        sortable="custom"
        :width="column.width"
      />
      <el-table-column label="Actions">
        <template #default="scope">
          <el-button
            text
            type="primary"
            size="small"
            @click="handleShowDetail(scope.row)"
          >
            Show Detail
          </el-button>
          <el-button
            text
            type="danger"
            size="small"
            @click="handleDelete(scope.row)"
          >
            Delete
          </el-button>
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
  </div>
</template>

<style scoped>
.sort-status {
  margin-bottom: 16px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.sort-status .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

.el-table .warning-red {
  --el-table-tr-bg-color: var(--el-color-danger-light-8);
}

.el-table .warning-yellow {
  --el-table-tr-bg-color: var(--el-color-warning-light-8);
}
</style>
