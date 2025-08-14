<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import type { ECharts } from 'echarts'
import {
  adminGetAssetDistributionService,
  adminGetContactPreferenceService,
  adminGetMetaDateService,
  adminGetUserDistributionService
} from '@/api/admin'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'
import { Location, User, Message } from '@element-plus/icons-vue'
import { ukRegionsCoordinates } from '@/utils/UKRegionsCoordinates'
import * as turf from '@turf/turf'
import { useWarningStore, useAssetStore } from '@/stores'
import type { Warning } from '@/types'

const loadUKMap = () => import('@/assets/ukmap.json')
const loadECharts = () => import('echarts')

let mapChart: ECharts | null = null

const isMapLoading = ref(true)

const handleResize = () => {
  if (mapChart) {
    mapChart.resize()
  }
}

const warningStore = useWarningStore()
const assetStore = useAssetStore()

const userCount = ref(0)
const assetCount = ref(0)

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

// map data
const mapData = computed(() => {
  const sortedEntries = Object.entries(assetDistribution.value).sort(
    (a, b) => b[1] - a[1]
  )
  const top5 = sortedEntries.slice(0, 5)
  return top5
})

const citys = computed(() => {
  if (!mapData.value.length) return []
  return mapData.value.map(([city]) => city)
})

type ScatterData = [number, number, number]

const locations = computed(() => {
  const newArr: ScatterData[] = []
  mapData.value.forEach((item) => {
    newArr.push([...ukRegionsCoordinates[item[0]], item[1]])
  })
  return newArr
})

const assetWarnings = computed(() => {
  if (!mapData.value.length) return []
  const arr: (Warning | null)[] = []
  mapData.value.forEach((item) => {
    const point = ukRegionsCoordinates[item[0]]
    const warnings: Warning[] = []
    warningStore.liveWarnings.forEach((warning) => {
      if (turf.booleanPointInPolygon(point, warning.area)) {
        warnings.push(warning)
      }
    })
    const maxWarning = assetStore.getMaxWarningLevel(warnings)
    arr.push(maxWarning)
  })
  console.log(arr)
  return arr
})

// Separate data fetching logic
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
    const userDistributionRes = await adminGetUserDistributionService()
    if (userDistributionRes && userDistributionRes.data) {
      userDistribution.value = userDistributionRes.data
    }
    const percentage = await adminGetContactPreferenceService()
    if (percentage && percentage.data) {
      contactPreference.value = percentage.data
    }
    const assetDis = await adminGetAssetDistributionService()
    if (assetDis && assetDis.data) {
      assetDistribution.value = assetDis.data
    }
    console.log(assetDistribution.value)

    await warningStore.getAllLiveWarnings()
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
  }
}

// Asynchronously initialize map
const initializeMap = async () => {
  try {
    // Load ECharts and map data in parallel
    const [echarts, { default: ukmap }] = await Promise.all([
      loadECharts(),
      loadUKMap()
    ])

    // Wait for DOM update to ensure element is rendered
    await nextTick()

    // Set loading to false first to make container visible
    isMapLoading.value = false

    // Wait for DOM update after container becomes visible
    await nextTick()

    // Wait a short time to ensure DOM is fully rendered and styles are applied
    await new Promise((resolve) => setTimeout(resolve, 200))

    // Ensure DOM element exists
    const container = document.getElementById('main')
    if (!container) {
      console.error('Map container not found')
      return
    }

    // Check if container has valid dimensions
    if (container.clientWidth === 0 || container.clientHeight === 0) {
      console.error('Map container has zero dimensions:', {
        width: container.clientWidth,
        height: container.clientHeight
      })
      return
    }

    mapChart = echarts.init(container)
    mapChart.showLoading()

    // Register map
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    echarts.registerMap('UK', ukmap as any)

    // Scatter plot data
    const scatterData = locations.value

    const cityNames = citys.value

    const warningColors: Record<string, string> = {
      RED: '#ff4757',
      AMBER: '#ffa502',
      YELLOW: '#ffda3a',
      NONE: '#409eff'
    }

    const option = {
      tooltip: {
        trigger: 'item',
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        formatter: function (params: any) {
          if (params.seriesType === 'effectScatter') {
            return `${params.name}<br/>Value: ${params.value[2]}`
          }
          return params.name
        }
      },
      legend: {
        show: true,
        left: 'center',
        top: '10px',
        textStyle: {
          color: '#333'
        }
      },
      geo: {
        map: 'UK',
        roam: true,
        left: '0%',
        right: '0%',
        top: '10%',
        bottom: '5%',
        layoutCenter: ['44%', '50%'],
        layoutSize: '100%',
        itemStyle: {
          areaColor: '#e7e8ea',
          borderColor: '#999'
        },
        emphasis: {
          itemStyle: {
            areaColor: '#c1e0ff'
          }
        }
      },
      series: ['RED', 'AMBER', 'YELLOW', 'NONE'].map((level) => ({
        name: level,
        type: 'effectScatter',
        coordinateSystem: 'geo',
        geoIndex: 0,
        itemStyle: {
          color: warningColors[level]
        },
        symbolSize: (params: number[]) => {
          return (params[2] / 100) * 100 + 8
        },
        data: scatterData
          .map((item, index) => {
            const warningLevel =
              assetWarnings.value[index]?.warningLevel || 'NONE'
            if (warningLevel !== level) return null
            let rippleConfig = {}
            if (warningLevel === 'RED') {
              rippleConfig = { period: 2, scale: 4 }
            } else if (warningLevel === 'AMBER') {
              rippleConfig = { period: 3, scale: 3 }
            } else {
              rippleConfig = { period: 6, scale: 2 }
            }
            return {
              name: cityNames[index],
              value: item,
              itemStyle: {
                color: warningColors[warningLevel] || '#409eff',
                opacity: 0.8
              },
              emphasis: {
                itemStyle: {
                  color: '#ff6b6b',
                  opacity: 1
                }
              },
              rippleEffect: rippleConfig
            }
          })
          .filter(Boolean)
      }))
    }

    mapChart.hideLoading()
    mapChart.setOption(option)
  } catch (error) {
    console.error('Failed to initialize map:', error)
    isMapLoading.value = false
  }
}

onMounted(() => {
  fetchDashboardData()
  initializeMap()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)

  if (mapChart) {
    mapChart.dispose()
    mapChart = null
  }
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
    </div>
  </div>

  <!-- Map and Dashboard Layout -->
  <div class="map-dashboard-container">
    <!-- Left Side: Map Area -->
    <div class="map-section">
      <div v-if="isMapLoading" class="map-loading">
        <div class="loading-spinner"></div>
        <p>Loading Map...</p>
      </div>
      <div v-show="!isMapLoading" id="main" class="map-container"></div>
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

.map-container {
  border: 1px solid black;
  display: flex;
  justify-content: center;
  flex: 1;
  min-height: 0;
  height: 100%;
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

.map-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  border: 1px solid #ddd;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.map-loading p {
  color: #666;
  font-size: 14px;
  margin: 0;
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
