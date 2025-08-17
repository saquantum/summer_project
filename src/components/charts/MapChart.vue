<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, ref, watch, computed } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { ukRegionsCoordinates } from '@/utils/UKRegionsCoordinates'
import * as turf from '@turf/turf'
import { useWarningStore, useAssetStore } from '@/stores'
import type { Warning } from '@/types'

interface Props {
  id: string
  title?: string
  data?: Record<string, number>
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Asset Distribution Map',
  loading: false
})

const loadUKMap = () => import('@/assets/ukmap.json')

let mapChart: ECharts | null = null
const isMapLoading = ref(true)

const warningStore = useWarningStore()
const assetStore = useAssetStore()

const handleResize = () => {
  if (mapChart) {
    mapChart.resize()
  }
}

// Computed property to process map data - shows top 5 regions
const mapData = computed(() => {
  if (!props.data) return []
  const sortedEntries = Object.entries(props.data).sort((a, b) => b[1] - a[1])
  return sortedEntries.slice(0, 5)
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
  return arr
})

const initializeMap = async () => {
  try {
    // Load ECharts and map data in parallel
    const [echart, { default: ukmap }] = await Promise.all([
      Promise.resolve(echarts),
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
    const container = document.getElementById(props.id)
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

    mapChart = echart.init(container)
    mapChart.showLoading()

    // Register map
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    echart.registerMap('UK', ukmap as any)

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

// Watch for data changes and update the chart
watch(
  () => props.data,
  () => {
    if (mapChart && props.data) {
      initializeMap()
    }
  },
  { deep: true }
)

watch(
  () => props.loading,
  (newLoading) => {
    if (!newLoading && props.data) {
      initializeMap()
    }
  }
)

onMounted(() => {
  if (!props.loading && props.data) {
    initializeMap()
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (mapChart) {
    mapChart.dispose()
    mapChart = null
  }
})
</script>

<template>
  <el-card class="map-chart-card" shadow="never">
    <!-- Card header -->
    <div class="card-header">
      <h3 class="card-title">{{ props.title }}</h3>
      <slot name="icon"></slot>
    </div>

    <!-- Card content -->
    <div class="card-content">
      <!-- Map chart -->
      <div class="chart-section">
        <div v-if="isMapLoading || loading" class="map-loading">
          <div class="loading-spinner"></div>
          <p>Loading Map...</p>
        </div>
        <div
          v-show="!isMapLoading && !loading"
          :id="id"
          class="chart-container"
        ></div>
        <div
          v-if="!props.data || Object.keys(props.data).length === 0"
          class="no-data"
        >
          <el-empty description="No data available" />
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.map-chart-card {
  min-width: 320px;
  flex: 1;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* Override Element Plus Card default styles */
.map-chart-card :deep(.el-card__body) {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chart-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.chart-container {
  flex: 1;
  min-height: 300px;
}

.map-loading {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f9f9f9;
  border-radius: 4px;
  min-height: 300px;
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

.no-data {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}
</style>
