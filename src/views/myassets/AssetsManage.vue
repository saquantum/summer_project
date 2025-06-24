<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ref, watch, computed } from 'vue'
import { useUserStore, useAssetStore } from '@/stores'
const assetStore = useAssetStore()
const userStore = useUserStore()
const router = useRouter()

// filter value
const assetName = ref('')
const assetWarningLevel = ref('')
const assetRegion = ref('')

// filtered assets
const currentAssets = ref([])

// select value
const warningLevelOptions = [
  {
    value: 'No Warning',
    label: 'No Warning'
  },
  {
    value: 'Yellow Warning',
    label: 'Yellow Warning'
  },
  {
    value: 'Amber Warning',
    label: 'Amber Warning'
  },
  {
    value: 'Red Warning',
    label: 'Red Warning'
  }
]
const warningRegion = [
  { value: 'North East England', label: 'North East England' },
  { value: 'North West England', label: 'North West England' },
  { value: 'Yorkshire & Humber', label: 'Yorkshire & Humber' },
  { value: 'East Midlands', label: 'East Midlands' },
  { value: 'West Midlands', label: 'West Midlands' },
  { value: 'East of England', label: 'East of England' },
  {
    value: 'London & South East England',
    label: 'London & South East England'
  },
  { value: 'South West England', label: 'South West England' },
  { value: 'Orkney & Shetland', label: 'Orkney & Shetland' },
  { value: 'Highlands & Eilean Siar', label: 'Highlands & Eilean Siar' },
  { value: 'Grampian', label: 'Grampian' },
  { value: 'Central, Tayside & Fife', label: 'Central, Tayside & Fife' },
  { value: 'Strathclyde', label: 'Strathclyde' },
  {
    value: 'Dumfries, Galloway, Lothian & Borders',
    label: 'Dumfries, Galloway, Lothian & Borders'
  },
  { value: 'Northern Ireland', label: 'Northern Ireland' },
  { value: 'Wales', label: 'Wales' }
]

// page change
const currentPage = ref(1)
const pageSize = 8
const handlePageChange = (page) => {
  currentPage.value = page
}

const addAssetVisible = ref(false)
const currentPageAssets = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  const arr = currentAssets.value.slice(start, end)

  // set indicator color
  arr.forEach((item) => {
    if (item.warnings[0]?.warningLevel.toLowerCase().includes('red')) {
      item.status = 'error'
    } else if (
      item.warnings[0]?.warningLevel.toLowerCase().includes('yellow')
    ) {
      item.status = 'warning'
    } else {
      item.status = 'success'
    }
  })

  return arr
})

onMounted(async () => {
  currentAssets.value = []
  let id
  if (!userStore.user.admin) {
    id = userStore.user.assetHolderId
  } else {
    id = userStore.proxyId
  }
  console.log(id)
  await assetStore.getUserAssets(id)
  currentAssets.value = assetStore.userAssets
})

// watch filter condition
watch(
  [assetName, assetWarningLevel, assetRegion],
  async () => {
    currentAssets.value = assetStore.userAssets.filter((item) => {
      const matchName = assetName.value
        ? item.asset.name?.toLowerCase().includes(assetName.value.toLowerCase())
        : true
      const matchLevel = assetWarningLevel.value
        ? item.warnings.length > 0 &&
          item.warnings[0].warningLevel &&
          assetWarningLevel.value
            .toLowerCase()
            .includes(item.warnings[0].warningLevel.toLowerCase())
        : true
      const matchRegion = assetRegion.value
        ? item.asset.region === assetRegion.value
        : true
      return matchName && matchLevel && matchRegion
    })
    currentPage.value = 1
  },
  {
    immediate: true
  }
)
</script>

<template>
  <!-- assets filter -->
  <div
    style="
      display: flex;
      justify-content: center;
      gap: 10px;
      margin-bottom: 10px;
      flex-wrap: wrap;
    "
  >
    <el-input v-model="assetName" class="select-style"></el-input>
    <el-select
      v-model="assetWarningLevel"
      placeholder="Select warning level"
      size="large"
      clearable
      class="select-style"
    >
      <el-option
        v-for="item in warningLevelOptions"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      ></el-option>
    </el-select>

    <el-select
      v-model="assetRegion"
      placeholder="Select Region"
      size="large"
      clearable
      class="select-style"
    >
      <el-option
        v-for="item in warningRegion"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      ></el-option>
    </el-select>

    <el-button @click="addAssetVisible = true"> Add asset </el-button>
  </div>

  <!-- cards for assets -->
  <div class="assets-container">
    <h3 v-if="assetStore.userAssets.length === 0">You don't have any asset</h3>
    <div class="card-grid">
      <el-card
        v-for="(item, index) in currentPageAssets"
        :key="item.asset.id"
        class="asset-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <h3 class="asset-title">{{ item.asset.name || 'Asset Name' }}</h3>
            <StatusIndicator :status="item.status" />
          </div>
        </template>

        <div class="map-container">
          <MapCard
            :map-id="'map-' + index"
            :drain-area="[item.asset.location]"
          />
        </div>

        <template #footer>
          <div class="card-footer">
            <el-button
              type="primary"
              @click="router.push(`/asset/${item.asset.id}`)"
              class="view-details-btn"
            >
              Show Detail
            </el-button>
          </div>
        </template>
      </el-card>
    </div>

    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total="currentAssets.length"
      layout="prev, pager, next"
      @current-change="handlePageChange"
      style="
        display: flex;
        justify-content: center;
        text-align: center;
        margin-top: 20px;
        position: relative;
        z-index: 2000;
      "
    />
    <AddAsset v-model:visible="addAssetVisible"></AddAsset>
  </div>
</template>

<style scoped>
.assets-container {
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.card-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;

  max-width: calc(300px * 4 + 20px * 3);
  margin-left: auto;
  margin-right: auto;

  justify-content: flex-start;
}

.asset-card {
  width: 300px;
  height: 310px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.asset-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: start;
  column-gap: 8px;
}

.asset-title {
  /* hide overflow text*/
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  line-height: 1.2;
  height: 1.2em;
}

.map-container {
  padding: 16px 0;
  display: flex;
  justify-content: center;
  height: 180px;
}

.map {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-footer {
  display: flex;
  justify-content: center;
  padding: 0;
}

.view-details-btn {
  width: 80%;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.select-style {
  width: 240px;
}

@media (max-width: 768px) {
  .assets-container {
    padding: 12px;
  }

  .card-grid {
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .card-grid {
    justify-content: flex-start;
  }
  .select-style {
    width: 100px;
  }
}

/* change defalut style */
:deep(.el-card__header) {
  padding: 12px 16px;
  height: 45px;
}

:deep(.el-card__body) {
  padding: 0px 16px 0px 16px;
}

:deep(.el-card__footer) {
  padding: 10px 16px;
  border-top: 1px solid #ebeef5;
}
</style>
