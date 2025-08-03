<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import ukmap from '@/assets/ukmap.json'
import type { GeoJSONSourceInput } from 'echarts/types/src/coord/geo/geoTypes.js'
import { adminGetMetaDateService, adminSearchUsersService } from '@/api/admin'
import type { UserItem, UserSearchBody } from '@/types'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import { Location, User, Message } from '@element-plus/icons-vue'
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

  // Scatter plot data - UK cities data points
  const scatterData = [
    [-0.1276, 51.5072, 85], // London - [longitude, latitude, value]
    [-3.1883, 55.9533, 62], // Edinburgh
    [-1.2577, 51.752, 45], // Oxford
    [-2.2426, 53.4808, 73], // Manchester
    [-1.4701, 53.3811, 58], // Sheffield
    [-1.8904, 52.4862, 39] // Birmingham
  ]

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: function (params: {
        seriesType: string
        name: string
        value: number[]
      }) {
        if (params.seriesType === 'effectScatter') {
          return `${params.name}<br/>Value: ${params.value[2]}`
        }
        return params.name
      }
    },
    legend: {
      show: true,
      data: ['UK Cities Data'],
      left: 'center',
      top: '10px',
      textStyle: {
        color: '#333'
      }
    },
    geo: {
      map: 'UK',
      roam: true,
      // Adjust map position and size within canvas
      left: '0%', // Left margin
      right: '0%', // Right margin
      top: '15%', // Top margin - increased to make space for legend
      bottom: '5%', // Bottom margin
      // Or use layoutCenter and layoutSize for precise control
      layoutCenter: ['50%', '55%'], // Adjusted center to account for legend
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
    series: [
      {
        name: 'UK Cities Data', // Add name for legend
        type: 'effectScatter',
        coordinateSystem: 'geo',
        geoIndex: 0,
        symbolSize: function (params: number[]) {
          // Adjust scatter point size based on value
          return (params[2] / 100) * 20 + 8
        },
        itemStyle: {
          color: '#409eff',
          opacity: 0.8
        },
        emphasis: {
          itemStyle: {
            color: '#ff6b6b',
            opacity: 1
          }
        },
        encode: {
          tooltip: 2
        },
        data: scatterData.map((item, index) => {
          const cityNames = [
            'London',
            'Edinburgh',
            'Oxford',
            'Manchester',
            'Sheffield',
            'Birmingham'
          ]
          const value = item[2]

          // Conditional animation settings based on value
          let rippleConfig = {}
          if (value > 70) {
            // High value cities - fast, large ripples
            rippleConfig = {
              period: 2,
              scale: 4
            }
          } else if (value > 50) {
            // Medium value cities - normal ripples
            rippleConfig = {
              period: 3,
              scale: 3
            }
          } else {
            // Low value cities - slow, small ripples
            rippleConfig = {
              period: 6,
              scale: 2
            }
          }

          return {
            name: cityNames[index],
            value: item,
            // Add conditional properties to each data point
            itemStyle: {
              color:
                value > 70 ? '#ff4757' : value > 50 ? '#ffa502' : '#409eff',
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
      }
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
  <div class="stats-container">
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
    <LineChart :count="assetCount" id="asset-copy" title="Asset Statistics">
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
      <BarChart id="contact-preference" title="Contact preference">
        <template #icon>
          <el-icon><Message /></el-icon>
        </template>
      </BarChart>
      <BarChart id="contact-preference-copy" title="Contact preference">
        <template #icon>
          <el-icon><Message /></el-icon>
        </template>
      </BarChart>
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

.stats-container {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  align-items: stretch;
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
  min-height: 0;
}

.dashboard-section :deep(.bar-chart-card) {
  flex: 1;
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

  .stats-container {
    flex-direction: column;
    gap: 10px;
  }
}

/* Extra responsive styles for stats container */
@media (max-width: 1200px) {
  .stats-container {
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .stats-container {
    gap: 10px;
    justify-content: center;
  }
}
</style>
