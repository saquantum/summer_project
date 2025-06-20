<script setup>
import { onMounted } from 'vue'
import { useAssetsStore } from '@/stores/modules/assets'
import 'leaflet/dist/leaflet.css'
import { useRouter } from 'vue-router'
import { ref, watch, computed } from 'vue'
import { useUserStore, useAdminStore } from '@/stores'
const assetsStore = useAssetsStore()
const userStore = useUserStore()
const adminStore = useAdminStore()
const router = useRouter()

// filter value
const assetName = ref('')
const assetWarningLevel = ref('')
const assetRegion = ref('')

// filtered assets
const currentAssets = ref([])

//select value
const warningLevelOptions = [
  {
    value: '',
    label: 'Select warning level'
  },
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
  {
    value: '',
    label: 'Select Region'
  },
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
const pageSize = 6
const handlePageChange = (page) => {
  currentPage.value = page
}

const currentPageAssets = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return currentAssets.value.slice(start, end)
})

onMounted(async () => {
  currentAssets.value = []
  let id
  if (!userStore.user.isAdmin) {
    id = userStore.user.user.assetHolderId
  } else {
    id = adminStore.proxyId
  }
  await assetsStore.getAssets(id)
  currentAssets.value = assetsStore.assets
})

watch(
  [assetName, assetWarningLevel, assetRegion],
  async () => {
    currentAssets.value = assetsStore.assets.filter((item) => {
      const matchName = assetName.value
        ? item.asset.name?.toLowerCase().includes(assetName.value.toLowerCase())
        : true
      const matchLevel = assetWarningLevel.value
        ? assetWarningLevel.value
            .toLowerCase()
            .includes(item.warnings[0]?.warningLevel?.toLowerCase() || '')
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
  <div style="margin-bottom: 10px">
    <el-input v-model="assetName" style="width: 240px"></el-input>
    <el-select
      v-model="assetWarningLevel"
      placeholder="Select warning level"
      size="large"
      style="width: 240px"
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
      style="width: 240px"
    >
      <el-option
        v-for="item in warningRegion"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      ></el-option>
    </el-select>
  </div>

  <!-- cards for assets -->
  <div class="assets-container">
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
            <span class="asset-id">ID: {{ item.asset.id }}</span>
          </div>
        </template>

        <div class="map-container">
          <MapCard
            :map-id="'map-' + index"
            :drain-area="item.asset.drainArea"
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
      "
    />
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
  justify-content: flex-start;
  max-width: 1200px;
  margin: 0 auto;
}

.asset-card {
  width: 300px;
  height: 360px;
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.asset-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.asset-id {
  font-size: 12px;
  color: #7f8c8d;
  background: #ecf0f1;
  padding: 4px 8px;
  border-radius: 12px;
}

.map-container {
  padding: 16px 0;
  display: flex;
  justify-content: center;
}

.map {
  width: 100%;
  height: 180px;
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
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
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
}

/* change defalut style */
:deep(.el-card__header) {
  padding: 12px 16px;
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-card__footer) {
  background: #fafafa;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}
</style>
