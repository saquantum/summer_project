<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
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
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Asset Distribution Map'
})

const loadUKMap = () => import('@/assets/ukmap.json')

let mapChart: ECharts | null = null

const warningStore = useWarningStore()
const assetStore = useAssetStore()

const handleResize = () => {
  if (mapChart) {
    if (mapChart) {
      mapChart.resize()
    }
  }
}

// Computed property to process map data - shows top 5 regions
const mapData = computed(() => {
  if (!props.data) return []
  const sortedEntries = Object.entries(props.data).sort((a, b) => b[1] - a[1])
  return sortedEntries
})

// Removed unused computed properties citys and locations

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

const initMapChart = async () => {
  const { default: mapData } = await loadUKMap()
  await nextTick()
  await nextTick()
  const container = document.getElementById(props.id)
  if (!container) return

  if (!mapChart) {
    mapChart = echarts.init(container)
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    echarts.registerMap('UK', mapData as any)
    window.addEventListener('resize', handleResize)
  }
}

const updateMapData = async () => {
  if (!mapChart || !props.data || Object.keys(props.data).length === 0) return

  // Prepare region data for coloring
  const regionData = mapData.value.map(([regionName, value]) => ({
    name: regionName,
    value: value
  }))

  // Find min and max values for color scaling
  const values = regionData.map((item) => item.value)
  const minValue = Math.min(...values)
  const maxValue = Math.max(...values)

  const warningColors: Record<string, string[]> = {
    RED: ['#ff6b6b', '#ff4757', '#ff3742'],
    AMBER: ['#ffb84d', '#ffa502', '#ff9500'],
    YELLOW: ['#ffe55c', '#ffda3a', '#ffd32a'],
    NONE: ['#5dade2', '#409eff', '#3490dc']
  }

  // Function to get color based on value within warning level
  const getWarningColor = (
    warningLevel: string,
    value: number,
    maxValue: number
  ) => {
    const colors = warningColors[warningLevel]
    if (!colors || colors.length === 0) return colors?.[0] || '#409eff'

    // Calculate intensity based on asset count (0 = lightest, 1 = darkest)
    const intensity = maxValue > 0 ? Math.min(value / maxValue, 1) : 0
    const colorIndex = Math.floor(intensity * (colors.length - 1))
    return colors[colorIndex] || colors[colors.length - 1]
  }

  const option = {
    tooltip: {
      trigger: 'item',
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      formatter: function (params: any) {
        if (params.data) {
          const assetCount = params.data.actualValue || params.data.value || 0
          const warningLevel = params.data.warningLevel
          const warningText =
            warningLevel && warningLevel !== 'NONE'
              ? ` (${warningLevel} Alert)`
              : ''
          return `${params.name}<br/>Assets: ${assetCount}${warningText}`
        }
        return params.name
      }
    },
    visualMap: {
      min: minValue,
      max: maxValue,
      left: 'left',
      top: 'bottom',
      text: ['High', 'Low'],
      seriesIndex: [0],
      inRange: {
        color: ['#e6f3ff', '#1890ff', '#0050b3']
      },
      calculable: true,
      show: false // Hide visual map when regions have warning colors
    },
    series: [
      {
        name: 'Asset Distribution',
        type: 'map',
        map: 'UK',
        roam: true,
        emphasis: {
          label: {
            show: true
          }
        },
        itemStyle: {
          borderColor: '#999',
          borderWidth: 1
        },
        data: regionData.map((item) => {
          const regionIndex = mapData.value.findIndex(
            ([name]) => name === item.name
          )
          const warning =
            regionIndex >= 0 ? assetWarnings.value[regionIndex] : null
          const warningLevel = warning?.warningLevel || 'NONE'

          // If there's a warning, use warning color for the area
          // Otherwise, let the visualMap handle the coloring based on value
          const hasWarning = warningLevel !== 'NONE'

          // Get appropriate color with gradient effect based on asset count
          const warningColor = hasWarning
            ? getWarningColor(warningLevel, item.value, maxValue)
            : undefined

          return {
            name: item.name,
            value: hasWarning ? null : item.value, // Set value to null for warning regions to avoid visualMap coloring
            actualValue: item.value, // Store actual value for tooltip
            warningLevel: warningLevel,
            itemStyle: hasWarning
              ? {
                  areaColor: warningColor,
                  borderColor: '#fff',
                  borderWidth: 2,
                  opacity: 1
                }
              : {
                  borderColor: '#999',
                  borderWidth: 1
                },
            emphasis: {
              itemStyle: {
                areaColor: hasWarning ? warningColor : '#ffeb3b',
                shadowOffsetX: 0,
                shadowOffsetY: 0,
                shadowBlur: 20,
                borderWidth: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
                opacity: 1
              }
            }
          }
        })
      }
    ]
  }

  mapChart.hideLoading()
  mapChart.setOption(option)
}

// Watch for data changes and update the chart
watch(
  () => props.data,
  async () => {
    if (mapChart === null) {
      await initMapChart()
    }
    updateMapData()
  },
  { deep: true }
)

onMounted(async () => {
  if (props.data && Object.keys(props.data).length > 0) {
    await initMapChart()
    updateMapData()
  }
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
        <div :id="id" class="chart-container"></div>
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

.no-data {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

/* Responsive styles */
@media (max-width: 768px) {
  .map-chart-card {
    min-width: 280px;
    max-width: 100%;
    height: 450px !important;
  }

  .chart-section {
    min-height: 350px;
  }

  .chart-container {
    min-height: 350px;
  }
}

@media (max-width: 576px) {
  .map-chart-card {
    min-width: 250px;
    width: 100%;
    height: 400px !important;
  }

  .chart-section {
    min-height: 300px;
  }

  .chart-container {
    min-height: 300px;
  }
}
</style>
