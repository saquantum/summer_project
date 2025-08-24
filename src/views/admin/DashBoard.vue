<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  adminGetActiveUsersService,
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
const activeUsers = ref(0)
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

    const [
      userDistRes,
      contactPrefRes,
      assetDistRes,
      assetsTotalRes,
      activeUsersRes
    ] = await Promise.all([
      adminGetUserDistributionService(),
      adminGetContactPreferenceService(),
      adminGetAssetDistributionService(),
      adminGetAssetsTotalService({
        filters: { warning_id: { op: 'notNull' } }
      }),
      adminGetActiveUsersService()
    ])

    if (userDistRes?.data) userDistribution.value = userDistRes.data
    if (contactPrefRes?.data) contactPreference.value = contactPrefRes.data
    if (assetDistRes?.data) assetDistribution.value = assetDistRes.data
    if (assetsTotalRes?.data) assetsInDanger.value = assetsTotalRes.data
    if (activeUsersRes?.data) activeUsers.value = activeUsersRes.data

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
  <div class="page-surface cards-page">
    <section class="layout-2col">
      <aside class="left-col">
        <div class="mini-grid">
          <el-card class="kpi-card">
            <div class="kpi-header">
              <div class="kpi-title">
                <el-icon><User /></el-icon><span>User Statistics </span>
              </div>
            </div>

            <div class="kpi-spark">
              <LineChart
                :count="userCount"
                title=""
                id="user"
                style="border: none"
              >
                <template #icon>
                  <el-icon style="font-size: 18px; color: #409eff">
                    <User />
                  </el-icon>
                </template>
              </LineChart>
            </div>
          </el-card>

          <el-card class="kpi-card">
            <div class="kpi-header">
              <div class="kpi-title">
                <el-icon><User /></el-icon><span>Active Users</span>
              </div>
            </div>
            <div class="kpi-spark">
              <LineChart
                :count="activeUsers"
                title=""
                id="active-users"
                style="border: none"
              >
                <template #icon>
                  <el-icon style="font-size: 18px; color: #409eff">
                    <Location />
                  </el-icon>
                </template>
              </LineChart>
            </div>
          </el-card>

          <el-card class="kpi-card">
            <div class="kpi-header">
              <div class="kpi-title">
                <el-icon><User /></el-icon><span>Asset Statistics</span>
              </div>
            </div>

            <div class="kpi-spark">
              <LineChart
                :count="assetCount"
                title=""
                id="asset-copy"
                style="border: none"
              >
                <template #icon>
                  <el-icon style="font-size: 18px; color: #409eff">
                    <Location />
                  </el-icon>
                </template>
              </LineChart>
            </div>
          </el-card>

          <el-card class="kpi-card">
            <div class="kpi-header">
              <div class="kpi-title">
                <el-icon><User /></el-icon><span>Assets in danger</span>
              </div>
            </div>

            <div class="kpi-spark">
              <LineChart
                :count="assetsInDanger"
                id="asset-in-danger"
                title=""
                color="orange"
                style="border: none"
              >
                <template #icon>
                  <el-icon style="font-size: 18px; color: #409eff">
                    <Location />
                  </el-icon>
                </template>
              </LineChart>
            </div>
          </el-card>
        </div>

        <div class="left-bottom-grid">
          <el-card class="chart-card">
            <div class="card-hd">
              <div class="title">Contact preference</div>
              <el-icon><Message /></el-icon>
            </div>

            <BarChart
              class="chart"
              id="contact-preference"
              style="border: none"
              title=""
              :data="contactPreference"
            />
          </el-card>

          <el-card class="chart-card">
            <div class="card-hd">
              <div class="title">User distribution</div>
            </div>

            <div class="pie-wrap">
              <PieChart
                class="chart"
                id="regional-pie-chart"
                style="border: none"
                title=""
                :data="pieChartData"
                chart-type="normal"
              />
            </div>
          </el-card>
        </div>
      </aside>

      <main class="right-col">
        <el-card class="map-card">
          <div class="card-hd">
            <div class="title">Asset Distribution Map</div>
            <el-tag round type="info">Live</el-tag>
          </div>

          <div class="map-wrap">
            <MapChart
              id="asset-distribution-map"
              style="border: none"
              title=""
              :data="assetDistribution"
            />
          </div>
        </el-card>
      </main>
    </section>
  </div>
</template>

<style scoped>
.cards-page {
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 16px;
  padding: 18px;
  margin: 28px auto;
  box-shadow: 0 8px 26px rgba(16, 24, 40, 0.04);
  /*width: min(1280px, calc(100vw - 24px));*/
  width: min(80vw, 1600px);
  max-width: calc(100% - 2rem);
  box-sizing: border-box;
}

.layout-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  align-items: stretch;
  /*align-items: start;*/
  max-width: 1600px;
  margin: 0 auto;
}

.left-col {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.mini-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
  min-height: 200px;
  height: auto;
}
.kpi-card {
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 120px;
  height: 100%;
  padding: 12px;
}
.kpi-header {
  padding: 8px 10px 0;
  margin: -10px;
}
.kpi-title {
  display: flex;
  gap: 8px;
  align-items: center;
  font-weight: 700;
  color: #0f172a;
}

.left-bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  min-height: 250px;
  height: auto;
}
.chart-card {
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 250px;
}

.card-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-hd .title {
  font-weight: 800;
  color: #0f172a;
}

.right-col {
  display: flex;
  flex-direction: column;
  min-height: 500px;
}
.map-card {
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
}
.map-wrap {
  /*height: 65vh;*/
  /*min-height: 520px;*/
  height: 100%;
  min-height: 400px;
  max-height: 65vh;
  flex: 1;
  display: flex;
}
::v-deep(.leaflet-container) {
  width: 100% !important;
  height: 100% !important;
  border-radius: 12px;
  flex: 1;
}

.kpi-spark {
  margin: -20px;
}

.kpi-spark :deep(canvas) {
  width: 60% !important;
}

.pie-wrap {
  width: 300px;
  height: 300px;
  margin: -20px -20px;
}

.pie-wrap :deep(canvas),
.pie-wrap :deep(svg) {
  width: 90% !important;
  height: 90% !important;
}

@media (max-width: 1200px) {
  .layout-2col {
    grid-template-columns: 1fr;
  }
  .map-wrap {
    /*height: 56vh;*/
    max-height: 56vh;
    min-height: 350px;
  }
}
@media (max-width: 768px) {
  .mini-grid {
    grid-template-columns: 1fr;
  }
  .left-bottom-grid {
    grid-template-columns: 1fr;
  }
  .map-wrap {
    height: 48vh;
  }
  .bar-chart-container,
  .pie-chart-container {
    min-height: 150px;
  }

  .pie-wrap :deep(canvas),
  .pie-wrap :deep(svg) {
    max-width: 180px;
    max-height: 180px;
  }

  #contact-preference {
    width: 95% !important;
    height: 95% !important;
  }
}
</style>
