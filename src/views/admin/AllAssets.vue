<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAssetStore } from '@/stores/index'
import {
  adminDeleteAssetService,
  adminGetAssetsTotalService
} from '@/api/admin'
import { type AssetSearchBody, type AssetTableItem } from '@/types'
import { useResponsiveAction } from '@/composables/useResponsiveAction'
import AssetCard from '@/components/cards/AssetCard.vue'
import { ElMessage } from 'element-plus'

const assets = computed<AssetTableItem[]>(() => {
  if (!assetStore.allAssets || assetStore.allAssets.length <= 0) return []
  return assetStore.allAssets.map((item) => ({
    id: item.asset.id,
    name: item.asset.name,
    type: item.asset.type?.name ?? 'NULL',
    capacityLitres: item.asset.capacityLitres,
    material: item.asset.material,
    status: item.asset.status,
    installedAt: item.asset.installedAt,
    lastInspection: item.asset.lastInspection,
    assetHolderId: item.asset.ownerId,
    warningLevel: item.warnings[0]?.warningLevel ?? 'NULL'
  }))
})

const router = useRouter()
const assetStore = useAssetStore()

const isLoading = ref(false)

const handleShowDetail = (row: AssetTableItem) => {
  router.push(`/assets/${row.id}`)
}

// delete dialog
const dialogVisible = ref(false)

const deleteId = ref<string[]>([])

const triggerDelete = (rows: AssetTableItem[]) => {
  dialogVisible.value = true
  rows.forEach((element) => {
    deleteId.value.push(element.id)
  })
}

const handleDelete = async () => {
  if (deleteId.value.length === 0) return
  await adminDeleteAssetService(deleteId.value)
  deleteId.value = []

  await fetchTableData()
  dialogVisible.value = false
}

const tableRowClassName = (scope: { row: AssetTableItem }) => {
  const warningLevel = scope.row.warningLevel
  if (warningLevel === 'RED') {
    return 'warning-red'
  } else if (warningLevel === 'YELLOW') {
    return 'warning-yellow'
  } else if (warningLevel === 'AMBER') {
    return 'warning-amber'
  } else return ''
}

const columns = [
  { prop: 'id', label: 'Asset Id', width: 180 },
  { prop: 'name', label: 'Asset Name', width: 180 },
  { prop: 'assetHolderId', label: 'Asset Holder ID', width: 180 },
  { prop: 'type', label: 'Type', width: 180 },
  { prop: 'capacityLitres', label: 'Capacity litres', width: 180 },
  { prop: 'material', label: 'Material', width: 180 },
  { prop: 'status', label: 'Status', width: 180 },
  { prop: 'installedAt', label: 'Installed At', width: 180 },
  { prop: 'lastInspection', label: 'Last inspection', width: 180 },
  { prop: 'warningLevel', label: 'Warning level', width: 180 }
]

const multiSort = ref<{ prop: string; order: string }[]>([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const assetSearchBody = ref<AssetSearchBody>({
  filters: {},
  orderList: '',
  limit: pageSize.value,
  offset: 0
})

const fetchTableData = async () => {
  try {
    isLoading.value = true
    const propOrderList: string[] = []

    for (const { prop, order } of multiSort.value) {
      let dbField = ''
      if (prop === 'id') dbField = 'asset_id'
      else if (prop === 'name') dbField = 'asset_name'
      else if (prop === 'assetHolderId') dbField = 'asset_owner_id'
      else if (prop === 'warningLevel') dbField = 'warning_level'
      else if (prop === 'type') dbField = 'asset_type_id'
      else if (prop === 'capacityLitres') dbField = 'asset_capacity_litres'
      else if (prop === 'material') dbField = 'asset_material'
      else if (prop === 'status') dbField = 'asset_status'
      else if (prop === 'installedAt') dbField = 'asset_installed_at'
      else if (prop === 'lastInspection') dbField = 'asset_last_inspection'
      else continue

      const sortDir = order === 'descending' ? 'desc' : 'asc'
      propOrderList.push(`${dbField},${sortDir}`)
    }

    // order by asset_id asc by default
    const sortStr =
      propOrderList.length > 0 ? propOrderList.join(',') : 'asset_id,asc'

    assetSearchBody.value.offset = (currentPage.value - 1) * pageSize.value
    assetSearchBody.value.limit = pageSize.value
    assetSearchBody.value.orderList = sortStr

    const [, res] = await Promise.all([
      assetStore.getAllAssets(assetSearchBody.value),
      adminGetAssetsTotalService({ filters: assetSearchBody.value.filters })
    ])

    total.value = res.data
  } catch (error) {
    console.error('Fail to load assets:', error)
    ElMessage.error('Failed to fetch table data')
  } finally {
    isLoading.value = false
  }
}

const handleSortChange = (sort: { prop: string; order: string | null }) => {
  const index = multiSort.value.findIndex((item) => item.prop === sort.prop)
  if (index !== -1) {
    if (!sort.order) {
      multiSort.value.splice(index, 1)
    } else {
      if (index >= 0 && index < multiSort.value.length) {
        multiSort.value[index]!.order = sort.order
      }
    }
  } else {
    if (sort.order) {
      multiSort.value.push({ prop: sort.prop, order: sort.order })
    }
  }
  fetchTableData()
}

const mutipleSelection = ref<AssetTableItem[]>([])
const handleSelectionChange = (val: AssetTableItem[]) => {
  mutipleSelection.value = val
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchTableData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTableData()
}

// responsive design
const paginationLayout = ref('total, prev, pager, next, sizes')

useResponsiveAction((width) => {
  if (width < 576) {
    paginationLayout.value = 'total, prev, pager, next'
  } else if (width >= 576 && width < 768) {
    paginationLayout.value = 'total, prev, pager, next'
  } else if (width >= 768 && width < 992) {
    paginationLayout.value = 'total, prev, pager, next, sizes'
  } else {
    paginationLayout.value = 'total, prev, pager, next, sizes'
  }
})

onMounted(async () => {
  await fetchTableData()
})
</script>

<template>
  <div>
    <div class="search-wrapper">
      <AssetSearch
        :fetch-table-data="fetchTableData"
        v-model:asset-search-body="assetSearchBody"
      ></AssetSearch>
      <SortTool
        v-model:multi-sort="multiSort"
        :columns="columns"
        :fetch-table-data="fetchTableData"
      ></SortTool>
    </div>

    <div class="asset-list">
      <AssetCard
        v-for="item in assetStore.allAssets"
        :key="item.asset.id"
        :item="item"
      />
    </div>

    <div v-show="mutipleSelection.length > 0">
      <el-button type="danger" @click="triggerDelete(mutipleSelection)"
        >Delete</el-button
      >
    </div>
    <el-table
      :data="assets"
      :row-class-name="tableRowClassName"
      @sort-change="handleSortChange"
      :default-sort="multiSort[0] || {}"
      v-loading="isLoading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection"> </el-table-column>
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        sortable="custom"
        :width="column.width"
      />
      <el-table-column label="Actions" fixed="right" min-width="120">
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
            @click="triggerDelete([scope.row])"
          >
            Delete
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      background
      :layout="paginationLayout"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      :pager-count="3"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />

    <ConfirmDialog
      v-model="dialogVisible"
      title="Warning"
      content="This will permanently delete this asset"
      :countdown-duration="5"
      @confirm="handleDelete"
      @cancel="dialogVisible = false"
    />
  </div>
</template>

<style scoped>
.search-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  height: 35px;
  gap: 10px;
  margin-bottom: 10px;
}
.asset-list {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

:deep(.el-table .warning-red) {
  --el-table-tr-bg-color: var(--el-color-danger-light-8);
}
:deep(.el-table .warning-amber) {
  --el-table-tr-bg-color: var(--el-color-warning-light-3);
}
:deep(.el-table .warning-yellow) {
  --el-table-tr-bg-color: var(--el-color-warning-light-8);
}

@media (max-width: 768px) {
  .table {
    display: none !important;
  }
}

@media (min-width: 768px) {
  .asset-list {
    display: none !important;
  }
}

@media (max-width: 768px) {
  .el-pagination {
    max-width: 100vw;
    box-sizing: border-box;
    overflow-x: auto;
    justify-content: center;
    display: flex;
  }
}
</style>
