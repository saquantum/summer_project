<script setup lang="ts">
import { onMounted, onUnmounted, withDefaults } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface Props {
  value?: number
  max?: number
  id: string
  title: string
  unit?: string
}

const props = withDefaults(defineProps<Props>(), {
  value: 0,
  max: 100,
  unit: '%'
})

let gaugeChart: ECharts | null = null

const initChart = (): void => {
  const chartDom = document.getElementById(props.id)
  if (chartDom) {
    gaugeChart = echarts.init(chartDom)

    const gaugeData = [
      {
        value: props.value,
        name: props.title,
        title: {
          offsetCenter: ['0%', '0%']
        },
        detail: {
          valueAnimation: true,
          offsetCenter: ['0%', '40%']
        }
      }
    ]

    const gaugeOption = {
      series: [
        {
          type: 'gauge',
          startAngle: 90,
          endAngle: -270,
          center: ['50%', '50%'],
          radius: '80%',
          min: 0,
          max: props.max,
          pointer: {
            show: false
          },
          progress: {
            show: true,
            overlap: false,
            roundCap: true,
            clip: false,
            itemStyle: {
              borderWidth: 1,
              borderColor: '#409eff',
              color: '#409eff'
            },
            width: 12
          },
          axisLine: {
            lineStyle: {
              width: 12,
              color: [[1, '#e6f7ff']]
            }
          },
          splitLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            show: false
          },
          data: gaugeData,
          title: {
            fontSize: 14,
            fontWeight: 600,
            color: '#303133',
            offsetCenter: ['0%', '-50%']
          },
          detail: {
            width: 80,
            height: 20,
            fontSize: 24,
            fontWeight: 'bold',
            color: '#409eff',
            borderColor: 'transparent',
            borderRadius: 10,
            formatter: function (value: number) {
              return Math.round(value) + props.unit
            },
            offsetCenter: ['0%', '40%']
          }
        }
      ]
    }
    gaugeChart.setOption(gaugeOption)
  }
}

onMounted((): void => {
  initChart()
})

onUnmounted((): void => {
  if (gaugeChart) {
    gaugeChart.dispose()
  }
})
</script>

<template>
  <el-card class="ring-gauge-card" shadow="never">
    <!-- Card header -->
    <!-- <div class="card-header">
      <h3 class="card-title">{{ props.title }}</h3>
      <slot name="icon"></slot>
    </div> -->

    <!-- Card content -->
    <div class="card-content">
      <!-- Full width ring gauge with centered information -->
      <div class="chart-section">
        <div :id="props.id" class="chart-container"></div>
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.ring-gauge-card {
  width: 375px;
  box-sizing: border-box;
}

/* Override Element Plus Card default styles */
.ring-gauge-card :deep(.el-card__body) {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-section {
  width: 100%;
  height: 220px;
}

.chart-container {
  width: 100%;
  height: 100%;
}
</style>
