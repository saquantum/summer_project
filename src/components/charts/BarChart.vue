<script setup lang="ts">
import { onMounted, onUnmounted, withDefaults, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface Props {
  id: string
  title: string
}

const props = withDefaults(defineProps<Props>(), {})

let barChart: ECharts | null = null

const handleResize = () => {
  if (barChart) {
    setTimeout(() => {
      if (barChart) {
        barChart.resize()
      }
    }, 100)
  }
}

const initChart = () => {
  const chartDom = document.getElementById(props.id)
  if (chartDom) {
    barChart = echarts.init(chartDom)
    const barOption = {
      tooltip: {
        trigger: 'axis',
        formatter: '{b}: {c}%'
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
        data: ['Email', 'SMS', 'Discord', 'Post', 'Telegram', 'Whatsapp'],
        axisLabel: {
          fontSize: 12,
          color: '#606266',
          rotate: 0
        }
      },
      yAxis: {
        show: true,
        type: 'value',
        max: 40,
        interval: 10,
        axisLabel: {
          formatter: '{value}%',
          fontSize: 10,
          color: '#606266'
        },
        splitLine: {
          show: false
        }
      },
      series: [
        {
          data: [12, 28, 15, 32, 20, 25],
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
}

onMounted(async () => {
  await nextTick()
  setTimeout(() => {
    initChart()
  }, 100)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (barChart) {
    barChart.dispose()
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

/* 响应式样式 */
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
