<script setup>
import { onMounted } from 'vue'
import { adminGetUKMapService } from '@/api/admin'
import * as echarts from 'echarts'

onMounted(async () => {
  const res = await adminGetUKMapService()
  console.log(res)

  const myChart = echarts.init(document.getElementById('main'))
  myChart.showLoading()
  echarts.registerMap('UK', res)

  // 创建一个随机饼图 series
  function randomPieSeries(center, radius) {
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

  // 设置地图和图表配置项
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

  // 渲染图表
  myChart.hideLoading()
  myChart.setOption(option)

  const barChart = echarts.init(document.getElementById('barChart'))

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
    legend: {},
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
      data: ['Brazil', 'Indonesia', 'USA', 'India', 'China', 'World']
    },
    series: [
      {
        name: '2011',
        type: 'bar',
        data: [18203, 23489, 29034, 104970, 131744, 630230]
      }
    ]
  }

  barChart.setOption(barOption)
})
</script>

<template>
  <div id="main" class="map-container"></div>
  <div id="barChart" class="map-container"></div>
</template>

<style>
.map-container {
  border: 1px solid black;
  height: 500px;
  padding: 16px 0;
  display: flex;
  justify-content: center;
}
</style>
