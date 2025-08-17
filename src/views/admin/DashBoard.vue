<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  adminGetAssetDistributionService,
  adminGetAssetsTotalService,
  adminGetContactPreferenceService,
  adminGetMetaDateService,
  adminGetUserDistributionService
} from '@/api/admin'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import MapChart from '@/components/charts/MapChart.vue'
import { Location, User, Message } from '@element-plus/icons-vue'
import { useWarningStore } from '@/stores'

const warningStore = useWarningStore()

const userCount = ref(0)
const assetCount = ref(0)
const assetsInDanger = ref(0)

const userDistribution = ref<Record<string, number>>({})
const assetDistribution = ref<Record<string, number>>({})
const contactPreference = ref<Record<string, number>>({})

// Computed property to process regional data - shows top 5 regions and groups others
const pieChartData = computed(() => {
  // Convert to array and sort by value (descending)
  const sortedEntries = Object.entries(userDistribution.value).sort(
    (a, b) => b[1] - a[1]
  )

  // Take top 5
  const top5 = sortedEntries.slice(0, 5)

  // Calculate sum of remaining regions
  const othersSum = sortedEntries
    .slice(5)
    .reduce((sum, [, value]) => sum + value, 0)

  // Create new data object
  const processedData: Record<string, number> = {}

  // Add top 5
  top5.forEach(([name, value]) => {
    processedData[name] = value
  })

  // Add others if there are remaining regions
  if (othersSum > 0) {
    processedData['Others'] = othersSum
  }

  return processedData
})

const fetchDashboardData = async () => {
  try {
    const res = await adminGetMetaDateService()
    const user = res.data.find(
      (item: { tableName: string }) => item.tableName === 'users'
    )
    const asset = res.data.find(
      (item: { tableName: string }) => item.tableName === 'assets'
    )
    userCount.value = user?.totalCount || 0
    assetCount.value = asset?.totalCount || 0

    const [userDistRes, contactPrefRes, assetDistRes, assetsTotalRes] =
      await Promise.all([
        adminGetUserDistributionService(),
        adminGetContactPreferenceService(),
        adminGetAssetDistributionService(),
        adminGetAssetsTotalService({
          filters: { warning_id: { op: 'notNull' } }
        })
      ])

    if (userDistRes?.data) userDistribution.value = userDistRes.data
    if (contactPrefRes?.data) contactPreference.value = contactPrefRes.data
    if (assetDistRes?.data) assetDistribution.value = assetDistRes.data
    if (assetsTotalRes?.data) assetsInDanger.value = assetsTotalRes.data

    await warningStore.getAllLiveWarnings()
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
  }
}

onMounted(async () => {
  await fetchDashboardData()
})
</script>

<template>
  <div class="stats-container">
    <div class="stats-cards">
      <LineChart :count="userCount" id="user" title="User Statistics">
        <template #icon>
          <el-icon style="font-size: 18px; color: #409eff">
            <User />
          </el-icon>
        </template>
      </LineChart>
      <LineChart :count="assetCount" id="asset" title="Asset Statistics">
        <template #icon>
          <el-icon style="font-size: 18px; color: #409eff">
            <Location />
          </el-icon>
        </template>
      </LineChart>
      <LineChart :count="assetCount" id="asset-copy" title="Asset Statistics">
        <template #icon>
          <el-icon style="font-size: 18px; color: #409eff">
            <Location />
          </el-icon>
        </template>
      </LineChart>
      <LineChart
        :count="assetsInDanger"
        id="asset-in-danger"
        title="Assets in danger"
        color="orange"
      >
        <template #icon>
          <el-icon style="font-size: 18px; color: #409eff">
            <Location />
          </el-icon>
        </template>
      </LineChart>
    </div>
  </div>

  <!-- Map and Dashboard Layout -->
  <div class="map-dashboard-container">
    <!-- Left Side: Map Area -->
    <div class="map-section">
      <MapChart
        id="asset-distribution-map"
        title="Asset Distribution Map"
        :data="assetDistribution"
      >
        <!-- <template #icon>
          <el-icon style="font-size: 18px; color: #409eff">
            <Location />
          </el-icon>
        </template> -->
      </MapChart>
    </div>

    <!-- Right Side: Dashboard Area -->
    <div class="dashboard-section">
      <BarChart
        id="contact-preference"
        title="Contact preference"
        :data="contactPreference"
      >
        <template #icon>
          <el-icon><Message /></el-icon>
        </template>
      </BarChart>
      <PieChart
        id="regional-pie-chart"
        title="User Distribution"
        :data="pieChartData"
        chart-type="normal"
      />
    </div>
  </div>
</template>

<style scoped>
.page-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 9999;
}

.page-loading .loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

.page-loading p {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.middle-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stats-container {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  align-items: stretch;
}

.stats-cards {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  width: 100%;
  align-items: stretch;
}

/* Responsive Layout */
.map-dashboard-container {
  display: flex;
  gap: 20px;
  height: 650px;
}

.map-section {
  flex: 3;
  min-height: 0;
}

.dashboard-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
}

.dashboard-section :deep(.bar-chart-card) {
  flex: 1;
}

/* Small Screen Responsive - Below 768px */
@media (max-width: 768px) {
  .map-dashboard-container {
    flex-direction: column;
    height: auto;
    gap: 20px;
  }

  .map-section {
    flex: none;
    height: 500px;
  }

  .dashboard-section {
    flex: none;
    flex-direction: row;
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
  }
}

/* Smaller Screen - Below 576px */
@media (max-width: 576px) {
  .dashboard-section {
    flex-direction: column;
    align-items: center;
  }

  .map-section {
    height: 500px;
  }

  .stats-container {
    flex-direction: column;
    gap: 10px;
  }
}

/* Extra responsive styles for stats container */
@media (max-width: 1200px) {
  .stats-container {
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .stats-container {
    gap: 10px;
    justify-content: center;
  }
}
</style>
