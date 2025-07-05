<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAssetStore } from '@/stores'
import { adminDeleteAssetService } from '@/api/admin'

const assets = ref([])
const router = useRouter()
const assetStore = useAssetStore()

const handleShowDetail = (row) => {
  router.push(`/asset/${row.id}`)
}

// delete dialog
const dialogVisible = ref(false)
const confirmDisabled = ref(true)
const countdown = ref(5)
let timer = null
const deleteId = ref([])

const startCountDown = () => {
  countdown.value = 5
  confirmDisabled.value = true
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      confirmDisabled.value = false
    }
  }, 1000)
}

const triggerDelete = (row) => {
  dialogVisible.value = true
  deleteId.value.push(row.id)
}
const handleDelete = async () => {
  dialogVisible.value = true
  const res = await adminDeleteAssetService(deleteId.value)
  deleteId.value = []
  console.log(res)
  await fetchTableData()
  dialogVisible.value = false
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
  { prop: 'name', label: 'Asset Name', width: 180 },
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
    else if (prop === 'name') dbField = 'asset_name'
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
      name: item.asset.name,
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

const screenWidth = ref(window.innerWidth)

let debounceTimer = null

const handleResize = () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    screenWidth.value = window.innerWidth
    resizeBasedOnWidth(screenWidth.value)
  }, 200)
}

const resizeBasedOnWidth = (width) => {
  if (width < 576) {
    console.log('Extra small screen, e.g., portrait phone')
    handleSizeChange(5)
  } else if (width >= 576 && width < 768) {
    handleSizeChange(5)
    console.log('Small screen, e.g., landscape phone or small tablet')
    handleSizeChange(10)

    // Your logic for small screens
  } else if (width >= 768 && width < 992) {
    handleSizeChange(10)

    console.log('Medium screen, e.g., tablets or small laptops')
    // Your logic for medium screens
  } else {
    handleSizeChange(10)
    console.log('Large screen, e.g., desktops or larger')
    // Your logic for large screens
  }
}

onMounted(async () => {
  await fetchTableData()
  resizeBasedOnWidth(screenWidth.value)
  window.addEventListener('resize', handleResize)
})

watch(dialogVisible, (val) => {
  if (val) {
    startCountDown()
  } else {
    clearInterval(timer)
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
      <FilterSearch></FilterSearch>
      <SortTool
        v-model:multiSort="multiSort"
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
