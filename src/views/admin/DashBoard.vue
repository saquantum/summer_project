<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import ukmap from '@/assets/ukmap.json'
import type { GeoJSONSourceInput } from 'echarts/types/src/coord/geo/geoTypes.js'
import { adminSearchUsersService } from '@/api/admin'
import type { UserItem, UserSearchBody } from '@/types'

let mapChart: ECharts | null = null
let barChart: ECharts | null = null
let lineChart: ECharts | null = null
let nightingaleChart: ECharts | null = null

const users = ref<UserItem[]>([])

const userLabels = computed(() => {
  if (users.value.length <= 0) return []
  return users.value.map((item) => item.user.id)
})

const userAssetNumbers = computed(() => {
  if (users.value.length <= 0) return []
  return users.value.map((item) => item.accumulation)
})
const handleResize = () => {
  if (mapChart && barChart && lineChart && nightingaleChart) {
    mapChart.resize()
    barChart.resize()
    lineChart.resize()
    nightingaleChart.resize()
  }
}

onMounted(async () => {
  const res = await adminSearchUsersService('count', {
    orderList: 'accumulation, desc',
    limit: 5
  } as UserSearchBody)
  console.log(res)
  users.value = res.data

  mapChart = echarts.init(document.getElementById('main'))
  mapChart.showLoading()
  echarts.registerMap('UK', ukmap as GeoJSONSourceInput)

  function randomPieSeries(center: number[], radius: number) {
    const data = ['A', 'B', 'C', 'D'].map((t) => {
      return {
        value: Math.round(Math.random() * 100),
        name: 'Category ' + t
      }
    })
    return {
      type: 'pie',
      coordinateSystem: 'geo',
      tooltip: {
        formatter: '{b}: {c} ({d}%)'
      },
      label: {
        show: false
      },
      labelLine: {
        show: false
      },
      animationDuration: 0,
      radius,
      center,
      data
    }
  }

  const option = {
    geo: {
      map: 'UK',
      roam: true,
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
    tooltip: {},
    legend: {
      show: true
    },
    series: [
      randomPieSeries([-0.1276, 51.5072], 15), // London
      randomPieSeries([-3.1883, 55.9533], 20), // Edinburgh
      randomPieSeries([-1.2577, 51.752], 25), // Oxford
      randomPieSeries([-2.2426, 53.4808], 18) // Manchester
    ]
  }

  mapChart.hideLoading()
  mapChart.setOption(option)

  barChart = echarts.init(document.getElementById('barChart'))

  const barOption = {
    title: {
      text: 'User Assets'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        // Use axis to trigger tooltip
        type: 'shadow' // 'shadow' as default; can also be 'line' or 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: userLabels.value
    },
    series: [
      {
        name: 'Total',
        type: 'bar',
        data: userAssetNumbers.value
      }
    ]
  }

  barChart.setOption(barOption)

  // line chart
  lineChart = echarts.init(document.getElementById('lineChart'))
  const lineOption = {
    title: {
      text: 'User Change'
    },
    xAxis: {
      type: 'category',
      data: [
        'Jan',
        'Feb',
        'Mar',
        'Apr',
        'May',
        'Jun',
        'Jul',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec'
      ]
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [820, 932, 901, 934, 1290, 1330, 1320, 1440, 1550, 1666, 1777],
        type: 'line',
        smooth: true
      }
    ]
  }
  lineChart.setOption(lineOption)

  // nightingale chart
  nightingaleChart = echarts.init(document.getElementById('nightingaleChart'))
  const nightingaleOption = {
    title: {
      text: 'User Distribution in the UK'
    },
    legend: {
      show: false
    },
    series: [
      {
        name: 'Nightingale Chart',
        type: 'pie',
        radius: [10, 60],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 0
        },
        data: [
          { value: 40, name: 'England' },
          { value: 38, name: 'Scotland' },
          { value: 32, name: 'Wales' },
          { value: 30, name: 'Northern Ireland' }
        ]
      }
    ]
  }
  nightingaleChart.setOption(nightingaleOption)

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <el-row style="height: 100%">
    <el-col :span="6">
      <div class="middle-container">
        <div id="barChart" class="map-container"></div>
        <div id="lineChart" class="map-container"></div>
        <div id="nightingaleChart" class="map-container"></div>
      </div>
    </el-col>
    <el-col :span="12">
      <div class="middle-container">
        <div style="display: flex; justify-content: center">
          <WarningCard></WarningCard>
        </div>
        <div id="main" class="map-container"></div>
      </div>
    </el-col>

    <el-col :span="6"> </el-col>
  </el-row>
</template>

<style>
.middle-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.map-container {
  /* border: 1px solid black; */
  padding: 16px 0;
  display: flex;
  justify-content: center;

  flex: 1;
  min-height: 0;
}
</style>
