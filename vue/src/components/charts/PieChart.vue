<script setup lang="ts">
import { onMounted, onUnmounted, nextTick, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface Props {
  id: string
  title: string
  data?: Record<string, number>
  chartType?: 'normal' | 'rose'
}

const props = withDefaults(defineProps<Props>(), {
  chartType: 'rose'
})

let pieChart: ECharts | null = null
const chartData = ref<Array<{ value: number; name: string }>>([])

const handleResize = () => {
  if (pieChart) {
    setTimeout(() => {
      if (pieChart) {
        pieChart.resize()
      }
    }, 100)
  }
}

const processData = (apiData: Record<string, number> | undefined) => {
  if (!apiData) {
    return []
  }

  return Object.entries(apiData).map(([name, value]) => ({
    value,
    name
  }))
}

const initChart = () => {
  const chartDom = document.getElementById(props.id)
  if (chartDom) {
    pieChart = echarts.init(chartDom)

    const pieOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      series: [
        {
          name: 'Regional Distribution',
          type: 'pie',
          radius: props.chartType === 'rose' ? [30, 120] : '60%',
          center: ['50%', '40%'],
          roseType: props.chartType === 'rose' ? 'area' : false,
          itemStyle: {
            borderRadius: props.chartType === 'rose' ? 4 : 0,
            borderColor: '#fff',
            borderWidth: 1
          },
          label: {
            show: true,
            fontSize: 11,
            color: '#606266'
          },
          labelLine: {
            show: true
          },
          data: chartData.value,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }

    pieChart.setOption(pieOption)
  }
}

const updateChart = () => {
  if (pieChart && chartData.value.length > 0) {
    pieChart.setOption({
      series: [
        {
          data: chartData.value
        }
      ]
    })
  }
}

// Watch for data changes
watch(
  () => props.data,
  (newData) => {
    chartData.value = processData(newData)
    if (pieChart) {
      updateChart()
    }
  },
  { immediate: true }
)

onMounted(async () => {
  await nextTick()
  setTimeout(() => {
    initChart()
  }, 100)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) {
    pieChart.dispose()
  }
})
</script>

<template>
  <el-card class="pie-chart-card" shadow="never">
    <!-- Card header -->
    <div class="card-header">
      <h3 class="card-title">{{ props.title }}</h3>
      <slot name="icon"></slot>
    </div>

    <!-- Card content -->
    <div class="card-content">
      <!-- Pie chart -->
      <div class="chart-section">
        <div :id="props.id" class="chart-container"></div>
        <div v-if="!chartData.length" class="no-data">
          <el-empty description="No data available" />
        </div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.pie-chart-card {
  min-width: 320px;
  flex: 1;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* Override Element Plus Card default styles */
.pie-chart-card :deep(.el-card__body) {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-icon {
  font-size: 18px;
  color: #409eff;
}

.card-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.chart-section {
  flex: 1;
  height: 100%;
  min-height: 300px;
  position: relative;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.no-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
}

@media (max-width: 768px) {
  .pie-chart-card {
    min-width: 280px;
    max-width: 100%;
    height: 400px !important;
  }

  .chart-section {
    height: 300px !important;
    min-height: 300px;
  }

  .chart-container {
    height: 300px !important;
  }
}

@media (max-width: 576px) {
  .pie-chart-card {
    min-width: 250px;
    width: 100%;
    height: 380px !important;
  }

  .chart-section {
    height: 280px !important;
    min-height: 280px;
  }

  .chart-container {
    height: 280px !important;
  }
}
</style>
