<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface Props {
  id: string
  title: string
  data?: Record<string, number>
}

const props = withDefaults(defineProps<Props>(), {})

let barChart: ECharts | null = null

const handleResize = () => {
  if (barChart) {
    barChart.resize()
  }
}
const chartData = ref<Array<{ value: number; name: string }>>([])

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
  if (chartDom && !barChart) {
    barChart = echarts.init(chartDom)
    window.addEventListener('resize', handleResize)
  }
}

const updateChart = () => {
  if (!barChart || chartData.value.length === 0) return

  const categories = chartData.value.map((item) => item.name)
  const values = chartData.value.map((item) => item.value)

  const barOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}'
    },
    grid: {
      left: 40,
      right: 20,
      top: 20,
      bottom: 50
    },
    xAxis: {
      show: true,
      type: 'category',
      data: categories,
      axisLabel: {
        show: true,
        fontSize: 12,
        color: '#606266',
        rotate: 0
      },
      axisLine: {
        show: true
      },
      axisTick: {
        show: true
      }
    },
    yAxis: {
      show: true,
      type: 'value',
      axisLabel: {
        fontSize: 10,
        color: '#606266'
      },
      splitLine: {
        show: false
      }
    },
    series: [
      {
        data: values,
        type: 'bar',
        itemStyle: {
          color: '#409eff',
          borderRadius: [2, 2, 0, 0]
        },
        barWidth: '60%'
      }
    ]
  }
  barChart.setOption(barOption)
}

// Watch for data changes and update the chart
watch(
  () => props.data,
  async (newData) => {
    chartData.value = processData(newData)
    if (barChart === null) {
      await nextTick()
      initChart()
    }
    updateChart()
  },
  { deep: true }
)

onMounted(async () => {
  console.log(props.data)
  if (props.data && Object.keys(props.data).length > 0) {
    chartData.value = processData(props.data)
    await nextTick()
    initChart()
    updateChart()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (barChart) {
    barChart.dispose()
    barChart = null
  }
})
</script>

<template>
  <el-card class="bar-chart-card" shadow="never">
    <!-- Card header -->
    <div class="card-header">
      <h3 class="card-title">{{ props.title }}</h3>
      <slot name="icon"></slot>
    </div>

    <!-- Card content -->
    <div class="card-content">
      <!-- Bar chart -->
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
.bar-chart-card {
  min-width: 280px;
  flex: 1;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* Override Element Plus Card default styles */
.bar-chart-card :deep(.el-card__body) {
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
  min-height: 200px;
}

.chart-container {
  width: 100%;
  height: 100%;
}

@media (max-width: 768px) {
  .bar-chart-card {
    min-width: 250px;
    max-width: 100%;
    height: 350px !important;
  }

  .chart-section {
    height: 250px !important;
    min-height: 250px;
  }

  .chart-container {
    height: 250px !important;
  }
}

@media (max-width: 576px) {
  .bar-chart-card {
    min-width: 200px;
    width: 100%;
    height: 320px !important;
  }

  .chart-section {
    height: 220px !important;
    min-height: 220px;
  }

  .chart-container {
    height: 220px !important;
  }
}
</style>
