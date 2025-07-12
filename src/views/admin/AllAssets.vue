<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAssetStore } from '@/stores/index'
import { adminDeleteAssetService } from '@/api/admin'
import { AssetSearchBody, type AssetTableItem } from '@/types'

const assets = ref<AssetTableItem[]>([])
const router = useRouter()
const assetStore = useAssetStore()

const handleShowDetail = (row: AssetTableItem) => {
  router.push(`/asset/${row.id}`)
}

// delete dialog
const dialogVisible = ref(false)
const confirmDisabled = ref(true)
const countdown = ref(5)
let timer: ReturnType<typeof setInterval> | null = null
const deleteId = ref<string[]>([])

const startCountDown = () => {
  countdown.value = 5
  confirmDisabled.value = true
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0 && timer !== null) {
      clearInterval(timer)
      timer = null
      confirmDisabled.value = false
    }
  }, 1000)
}

const triggerDelete = (row: AssetTableItem) => {
  dialogVisible.value = true
  deleteId.value.push(row.id)
}

const handleDelete = async () => {
  dialogVisible.value = true
  if (deleteId.value.length === 0) return
  const res = await adminDeleteAssetService(deleteId.value)
  deleteId.value = []
  console.log(res)
  await fetchTableData()
  dialogVisible.value = false
}

const tableRowClassName = (scope: { row: AssetTableItem }) => {
  const warningLevel = scope.row.warningLevel.toLowerCase()
  if (warningLevel.includes('red')) {
    return 'warning-red'
  } else if (warningLevel.includes('yellow')) {
    return 'warning-yellow'
  }
  return ''
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
  { prop: 'lastInspection', label: 'Last inspection', width: 180 }
]

const multiSort = ref<{ prop: string; order: string }[]>([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const assetSearchBody = ref<AssetSearchBody>({
  filters: {},
  orderList: '',
  limit: pageSize.value,
  offset: 0
})

const fetchTableData = async () => {
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
  await assetStore.getAllAssets(assetSearchBody.value)
  assets.value = assetStore.allAssets.map((item) => {
    return {
      id: item.asset.id,
      name: item.asset.name,
      type: item.asset.type.name,
      capacityLitres: item.asset.capacityLitres,
      material: item.asset.material,
      status: item.asset.status,
      installedAt: item.asset.installedAt,
      lastInspection: item.asset.lastInspection,
      assetHolderId: item.asset.ownerId,
      warningLevel: item.warnings[0]?.warningLevel?.toLowerCase() ?? 'none'
    }
  })
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

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchTableData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTableData()
}

const screenWidth = ref(window.innerWidth)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

const handleResize = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    screenWidth.value = window.innerWidth
    resizeBasedOnWidth(screenWidth.value)
  }, 200)
}

const resizeBasedOnWidth = (width: number) => {
  if (width < 576) {
    console.log('Extra small screen, e.g., portrait phone')
    handleSizeChange(5)
  } else if (width >= 576 && width < 768) {
    handleSizeChange(10)
    console.log('Small screen, e.g., landscape phone or small tablet')
  } else if (width >= 768 && width < 992) {
    handleSizeChange(10)
    console.log('Medium screen, e.g., tablets or small laptops')
  } else {
    handleSizeChange(10)
    console.log('Large screen, e.g., desktops or larger')
  }
}

onMounted(async () => {
  await fetchTableData()
  // resizeBasedOnWidth(screenWidth.value)
  window.addEventListener('resize', handleResize)
})

watch(dialogVisible, (val) => {
  if (val) {
    startCountDown()
  } else {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
    confirmDisabled.value = true
  }
})
</script>

<template>
  <div>
    <div class="asset-list">
      <AssetCard
        v-for="(item, index) in assets"
        :key="index"
        :asset="item"
      ></AssetCard>
    </div>
    <div class="search-wrapper">
      <FilterSearch
        :fetch-table-data="fetchTableData"
        v-model:asset-search-body="assetSearchBody"
      ></FilterSearch>
      <SortTool
        v-model:multi-sort="multiSort"
        :columns="columns"
        :fetch-table-data="fetchTableData"
      ></SortTool>
    </div>

    <el-table
      :data="assets"
      :row-class-name="tableRowClassName"
      @sort-change="handleSortChange"
      :default-sort="multiSort[0] || {}"
      class="table"
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
            @click="triggerDelete(scope.row)"
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

    <el-dialog v-model="dialogVisible" title="Tips" width="500">
      <span>Notice: This will permanently delete this asset</span>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button
            type="primary"
            :disabled="confirmDisabled"
            @click="handleDelete"
          >
            {{ confirmDisabled ? `Confirm (${countdown})` : 'Confirm' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
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
  gap: 16px;
}

.el-table .warning-red {
  --el-table-tr-bg-color: var(--el-color-danger-light-8);
}

.el-table .warning-yellow {
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
</style>
