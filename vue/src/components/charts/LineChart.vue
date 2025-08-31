<script setup lang="ts">
import ICountUp from 'vue-countup-v3'
import { onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface Props {
  count: number
  id: string
  title: string
  color?: string
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
  color: ' #409eff'
})

let lineChart: ECharts | null = null

const handleResize = () => {
  if (lineChart) {
    lineChart.resize()
  }
}

const initChart = () => {
  const chartDom = document.getElementById(props.id)
  if (chartDom) {
    lineChart = echarts.init(chartDom)
    const lineOption = {
      grid: {
        left: 0,
        right: 0,
        top: 0,
        bottom: 0
      },
      xAxis: {
        show: false,
        type: 'category'
      },
      yAxis: {
        show: false,
        type: 'value'
      },
      series: [
        {
          data: [10, 22, 28, 23, 19],
          type: 'line',
          smooth: true,
          symbol: 'none' // Hide data points
        }
      ]
    }
    lineChart.setOption(lineOption)
  }
}

onMounted(async () => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (lineChart) {
    lineChart.dispose()
  }
})
</script>

<template>
  <el-card class="line-chart-card" shadow="never">
    <!-- Card header - now in body -->
    <div class="card-header">
      <h3 class="card-title">{{ props.title }}</h3>
      <slot name="icon"></slot>
    </div>

    <!-- Card content -->
    <div class="card-content">
      <!-- Left side: count number -->
      <div class="count-section">
        <div class="count-number" :style="{ color: props.color }">
          <ICountUp :end-val="props.count" :duration="2" />
        </div>
      </div>

      <!-- Right side: line chart -->
      <div class="chart-section">
        <div :id="props.id" class="chart-container"></div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.line-chart-card {
  min-width: 375px !important;
  flex: 1;
  box-sizing: border-box;
}

/* Override Element Plus Card default styles */
.line-chart-card :deep(.el-card__body) {
  padding: 16px;
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
  gap: 24px;
}

.count-section {
  flex: 0 0 auto;
  text-align: center;
  padding: 0;
  /* Remove min-width, let width adapt to content */
}

.count-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.count-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  min-height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px; /* Only add minimal padding on left and right */
}

.chart-section {
  flex: 1;
  height: 70px;
}

.chart-container {
  width: 100%;
  height: 100%;
}

/* Responsive styles */
@media (max-width: 768px) {
  .line-chart-card {
    min-width: 250px;
    max-width: 100%;
  }
}

@media (max-width: 576px) {
  .line-chart-card {
    min-width: 200px;
    width: 100%;
  }

  .card-content {
    gap: 16px;
  }

  .count-number {
    font-size: 24px;
  }
}
</style>
