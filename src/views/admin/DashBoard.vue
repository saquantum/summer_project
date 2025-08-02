<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import ukmap from '@/assets/ukmap.json'
import type { GeoJSONSourceInput } from 'echarts/types/src/coord/geo/geoTypes.js'
import { adminGetMetaDateService, adminSearchUsersService } from '@/api/admin'
import type { UserItem, UserSearchBody } from '@/types'
import LineChart from '@/components/charts/LineChart.vue'
import RingGauge from '@/components/charts/RingGauge.vue'
import { Location, User, Message, ChatLineRound } from '@element-plus/icons-vue'
let mapChart: ECharts | null = null

const users = ref<UserItem[]>([])

const handleResize = () => {
  if (mapChart) {
    mapChart.resize()
  }
}

const userCount = ref()
const assetCount = ref()

onMounted(async () => {
  const res1 = await adminGetMetaDateService()

  const user = res1.data.find((item) => item.tableName === 'users')
  const asset = res1.data.find((item) => item.tableName === 'assets')

  userCount.value = user.totalCount
  assetCount.value = asset.totalCount

  const res = await adminSearchUsersService('count', {
    orderList: 'accumulation,desc',
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
      // Adjust map position and size within canvas
      left: '0%', // Left margin
      right: '0%', // Right margin
      top: '5%', // Top margin
      bottom: '5%', // Bottom margin
      // Or use layoutCenter and layoutSize for precise control
      layoutCenter: ['50%', '50%'],
      layoutSize: '80%', // Make map occupy 80% of canvas, reduce whitespace
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

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div style="display: flex; gap: 10px; flex-wrap: wrap">
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
  </div>

  <!-- Map and Dashboard Layout -->
  <div class="map-dashboard-container">
    <!-- Left Side: Map Area -->
    <div class="map-section">
      <div id="main" class="map-container"></div>
    </div>

    <!-- Right Side: Dashboard Area -->
    <div class="dashboard-section">
      <RingGauge
        id="email-sent"
        title="Email Sent"
        :value="75"
        :max="100"
        unit="%"
      >
        <template #icon>
          <el-icon><Message /></el-icon>
        </template>
      </RingGauge>

      <RingGauge id="sms-sent" title="SMS Sent" :value="65" :max="100" unit="%">
        <template #icon>
          <el-icon><ChatLineRound /></el-icon>
        </template>
      </RingGauge>
    </div>
  </div>
</template>

<style>
.middle-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.map-container {
  border: 1px solid black;
  padding: 16px 0;
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
    height: 800px;
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
    height: 800px;
  }
}
</style>
