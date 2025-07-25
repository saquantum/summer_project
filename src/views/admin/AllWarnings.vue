<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useWarningStore } from '@/stores/index.ts'
import type { Style } from '@/types'
import type { MultiPolygon } from 'geojson'
// TODO: warning display visibility function

const activeNames = ref<string[]>(['live', 'expired'])
const outdatedWarnings = ref<TableRow[]>([])
const liveWarnings = ref<TableRow[]>([])
const router = useRouter()
const warningStore = useWarningStore()
const mapId = 'allWarningsMap'
const warningPolygon = ref<MultiPolygon[]>([])
const styles = ref<Style[]>([])
interface TableRow {
  id: number
  weatherType: string
  warningImpact: string
  warningLevel: string
  warningLikelihood: string
  validFrom: string
  validTo: string
  period: string
}

const handleShowDetail = (row: TableRow) => {
  router.push(`/warnings/${row.id}`)
}

const handleDelete = (row: TableRow) => {
  console.log('Delete warning:', row.id)
}

const processWarnings = () => {
  warningPolygon.value = []
  liveWarnings.value = warningStore.liveWarnings.map((item) => {
    styles.value.push(setWarningLevelStyle(item.warningLevel))
    warningPolygon.value.push(item.area)
    return {
      id: item.id,
      weatherType: item.weatherType,
      warningImpact: item.warningImpact,
      warningLevel: item.warningLevel,
      warningLikelihood: item.warningLikelihood,
      validFrom: new Date(item.validFrom * 1000).toLocaleString(),
      validTo: new Date(item.validTo * 1000).toLocaleString(),
      period: `${new Date(item.validFrom * 1000).toLocaleString()} - ${new Date(item.validTo * 1000).toLocaleString()}`
    }
  })

  outdatedWarnings.value = warningStore.outdatedWarnings.map((item) => {
    return {
      id: item.id,
      weatherType: item.weatherType,
      warningImpact: item.warningImpact,
      warningLevel: item.warningLevel,
      warningLikelihood: item.warningLikelihood,
      validFrom: new Date(item.validFrom * 1000).toLocaleString(),
      validTo: new Date(item.validTo * 1000).toLocaleString(),
      period: `${new Date(item.validFrom * 1000).toLocaleString()} - ${new Date(item.validTo * 1000).toLocaleString()}`
    }
  })
}

const getRowClass = (row: TableRow) => {
  const level = row.warningLevel?.toLowerCase() || ''
  if (level.includes('RED')) return 'row-red'
  if (level.includes('AMBER')) return 'row-amber'
  if (level.includes('YELLOW')) return 'row-yellow'
  return ''
}

const setWarningLevelStyle = (level: string): Style => {
  const style = { weight: 2, fillOpacity: 0.4, color: '', fillColor: '' }
  if (level.includes('YELLOW')) {
    style.color = '#cc9900'
    style.fillColor = '#ffff00'
  } else if (level.includes('AMBER')) {
    style.fillOpacity = 0.6
    style.color = '#cc6600'
    style.fillColor = '#f7b733'
  } else if (level.includes('RED')) {
    style.color = '#800000'
    style.fillColor = '#ff0000'
  }
  return style
}

onMounted(async () => {
  await warningStore.getAllWarnings()
  await warningStore.getAllLiveWarnings()
  processWarnings()
})

watch(
  () => warningStore.allWarnings,
  () => {
    processWarnings()
  },
  { deep: true }
)
</script>

<template>
  <div class="main-layout">
    <!-- map -->
    <div class="map-section">
      <div class="map-card">
        <div class="map-container">
          <MapCard
            v-if="warningPolygon.length > 0"
            :map-id="mapId"
            :locations="warningPolygon"
            :styles="styles"
          />
        </div>
      </div>
    </div>
    <el-collapse v-model="activeNames" class="table-section">
      <!-- live-table -->
      <el-collapse-item name="live">
        <template #title>
          <div class="collapse-title">live-warning</div>
        </template>

        <!--div class="table-section"-->

        <div class="collapse-card warning-card">
          <div class="table-scroll">
            <el-table
              :data="liveWarnings"
              stripe
              style="width: 100%"
              table-layout="auto"
              :row-class-name="getRowClass"
              class="table1"
            >
              <el-table-column prop="id" label="ID" width="90" />
              <el-table-column prop="weatherType" label="Type" width="100" />

              <el-table-column label="Level" width="90" align="center">
                <template #default="scope">
                  <el-tooltip :content="scope.row.warningLevel" placement="top">
                    <span
                      class="level-badge"
                      :class="scope.row.warningLevel?.toLowerCase()"
                    ></span>
                  </el-tooltip>
                </template>
              </el-table-column>

              <el-table-column
                prop="warningImpact"
                label="Impact"
                width="100"
              />
              <el-table-column
                prop="warningLikelihood"
                label="Likelihood"
                width="120"
              />

              <el-table-column prop="period" label="Period" width="180" />

              <el-table-column label="Actions" width="180">
                <template #default="scope">
                  <el-button
                    text
                    type="primary"
                    size="small"
                    @click="handleShowDetail(scope.row)"
                    >Show Detail
                  </el-button>
                  <el-button
                    text
                    type="danger"
                    size="small"
                    @click="handleDelete(scope.row)"
                    >Delete
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-collapse-item>

      <el-collapse-item name="expired">
        <template #title>
          <div class="collapse-title">history-warning</div>
        </template>

        <div
          class="collapse-card expired-card"
          v-if="activeNames.includes('expired')"
        >
          <el-table
            :data="outdatedWarnings"
            stripe
            style="width: 100%"
            table-layout="auto"
            class="expired-table"
          >
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="weatherType" label="Type" width="100" />
            <el-table-column prop="warningLevel" label="Level" width="90" />
            <el-table-column prop="warningImpact" label="Impact" width="100" />
            <el-table-column
              prop="warningLikelihood"
              label="Likelihood"
              width="120"
            />
            <el-table-column prop="period" label="Period" width="180" />
            <el-table-column label="Actions" width="180">
              <template #default="scope">
                <el-button
                  text
                  type="primary"
                  size="small"
                  @click="handleShowDetail(scope.row)"
                  >Show Detail
                </el-button>
                <el-button
                  text
                  type="danger"
                  size="small"
                  @click="handleDelete(scope.row)"
                  >Delete
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: row;
  height: calc(100vh - 80px);
  padding: 16px;
  gap: 16px;
  box-sizing: border-box;
  overflow-x: auto;
  overflow-y: auto;
}

.map-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 12px;
  overflow: auto;
}

.map-section {
  flex: 1.2;
  display: flex;
  flex-direction: column;
}

.map-container {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow: hidden;
}

.map-section {
  min-width: 400px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.table-section {
  display: flex;
  flex-direction: column;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
}

/* warning table cards */
.warning-card,
.expired-card {
  display: flex;
  flex-direction: column;
  background-color: #fff;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  overflow: auto;
}

.warning-card .el-table,
.expired-card .el-table {
  flex: 1;
  height: 100%;
}

.collapse-title {
  font-size: 16px;
  font-weight: bold;
  background-color: #ffffff;
  margin: 0;
}

.collapse-card {
  background-color: #fff;
  padding: 16px;

  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

::v-deep(.leaflet-container) {
  border-radius: 12px;
  width: 100%;
  height: 100%;
}

.level-badge {
  display: inline-block;
  width: 28px;
  height: 10px;
  border-radius: 6px;
}

.level-badge.yellow {
  background-color: #f7e359;
}

.level-badge.amber {
  background-color: #f7b733;
}

.level-badge.red {
  background-color: #e74c3c;
}

/* table tweaks */
::v-deep(.el-table) {
  width: 100% !important;
  table-layout: fixed;
  background-color: transparent;
  border-radius: 0;
  box-shadow: none;
}
::v-deep(.el-table__header-wrapper) {
  width: 100% !important;
  overflow: auto;
}
::v-deep(.el-table th.is-leaf > .cell),
::v-deep(.el-table__header .el-table__cell) {
  justify-content: center !important;
  display: flex;
  align-items: center;
}

::v-deep(.el-table__cell) {
  white-space: nowrap;
}
::v-deep(.table1 td),
::v-deep(.expired-table td),
::v-deep(.el-table__header-wrapper th) {
  font-size: 13px;
  padding: 6px 8px;
}
::v-deep(.el-table__header-wrapper th) {
  background-color: rgba(181, 219, 241, 0.26);
  color: #444;
  font-weight: 600;
}
::v-deep(.el-table__row) {
  height: 42px;
}

::v-deep(.table1 .el-table__body tr:nth-child(even) > td) {
  background-color: rgba(246, 226, 189, 0.1);
}
::v-deep(.table1 .el-table__body tr:nth-child(odd) > td) {
  background-color: #ffffff;
}
::v-deep(.table1 .el-button--text) {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 10px;
  font-weight: 300;
  line-height: 1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
::v-deep(.table1 .el-button--primary) {
  background-color: #ebf5ff;
  color: #409eff;
  border: none;
}
::v-deep(.table1 .el-button--primary:hover) {
  background-color: #409eff;
  color: #fff;
}

::v-deep(.table1 .el-button--danger) {
  background-color: #fff0f0;
  color: #f56c6c;
  border: none;
}
::v-deep(.table1 .el-button--danger:hover) {
  background-color: #f56c6c;
  color: #fff;
}
::v-deep(.table1 .el-table__body-wrapper tr:hover > td) {
  background-color: #f0f7ff;
  transition: background-color 0.2s ease;
}

::v-deep(.el-collapse-item) {
  margin-bottom: 16px;
}

::v-deep(.el-collapse-item__header) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  font-size: 16px;
  font-weight: bold;
  background-color: #ffffff;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  cursor: pointer;
  width: 100%;
  box-sizing: border-box;
}

::v-deep(.el-collapse-item__wrap) {
  background-color: transparent;
  overflow: auto;
  padding: 0;
}

::v-deep(.el-collapse-item__content) {
  padding: 0 !important;
}

::v-deep(.el-collapse-item) {
  margin-bottom: 0;
}

@media (min-width: 769px) and (max-width: 1024px) {
  html,
  body {
    margin: 0;
    padding: 0;
    width: 100%;
    overflow-x: hidden;
  }

  .main-layout {
    flex-direction: column;
    width: 100%;
    height: 100vh;
    overflow-x: hidden;
  }

  .map-card,
  .warning-card,
  .expired-card {
    flex: 1;
    overflow: auto;
  }

  .map-container {
    width: 100%;
    height: 100%;
    overflow: hidden;
  }

  .el-collapse,
  .map-section,
  .table-section,
  .map-card,
  .warning-card,
  .expired-card,
  .el-collapse-item,
  .el-collapse-item__wrap,
  .el-collapse-item__content {
    width: 100% !important;
    min-width: 0;
    box-sizing: border-box;
  }

  ::v-deep(.leaflet-container) {
    width: 100% !important;
    height: 100% !important;
    border-radius: 12px;
  }

  ::v-deep(.el-table) {
    width: 100% !important;
    table-layout: fixed;
  }
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    width: 100%;
    max-width: 100vw;
    padding: 16px;
    gap: 16px;
    box-sizing: border-box;
  }

  .map-card {
    min-height: 300px;
  }

  .map-container {
    height: 40vh;
    min-height: 240px;
    width: 100%;
    overflow: hidden;
  }

  .map-section,
  .map-card,
  .warning-card,
  .expired-card,
  .table-section,
  .el-collapse-item,
  .el-collapse-item__header,
  .el-collapse-item__wrap,
  .el-collapse-item__content {
    width: 100%;
    box-sizing: border-box;
  }

  .card {
    max-height: 800px;
    overflow: auto;
  }

  ::v-deep(.leaflet-container) {
    width: 100% !important;
    height: 100% !important;
  }
}
</style>
