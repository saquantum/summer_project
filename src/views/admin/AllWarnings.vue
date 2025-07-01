<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useWarningStore } from '@/stores'

const warnings = ref([])
const router = useRouter()
const warningStore = useWarningStore()
const mapId = 'allWarningsMap'
const warningPolygon = ref([])

const handleShowDetail = (row) => {
  router.push(`/warning/${row.id}`)
}

const handleDelete = (row) => {
  console.log('Delete warning:', row.id)
}

const processWarnings = () => {
  warningPolygon.value = []
  warnings.value = warningStore.allWarnings.map((item) => {
    const style = setWarningLevelStyle(item.warningLevel)
    const area = { ...item.area, style }
    warningPolygon.value.push(area)
    return {
      id: item.id,
      weatherType: item.weatherType,
      warningImpact: item.warningImpact,
      warningLevel: item.warningLevel,
      warningLikelihood: item.warningLikelihood,
      validFrom: new Date(item.validFrom * 1000).toLocaleString(),
      validTo: new Date(item.validTo * 1000).toLocaleString(),
      period: `${new Date(item.validFrom * 1000).toLocaleString()} - ${new Date(item.validTo * 1000).toLocaleString()}`,

      area
    }
  })
}

const getRowClass = ({ row }) => {
  const level = row.warningLevel?.toLowerCase() || ''
  if (level.includes('red')) return 'row-red'
  if (level.includes('amber')) return 'row-amber'
  if (level.includes('yellow')) return 'row-yellow'
  return ''
}

const setWarningLevelStyle = (level) => {
  const style = { weight: 2, fillOpacity: 0.4 }
  if (level?.toLowerCase().includes('yellow')) {
    style.color = '#cc9900'
    style.fillColor = '#ffff00'
  } else if (level?.toLowerCase().includes('amber')) {
    style.color = '#cc6600'
    style.fillColor = '#ffcc00'
  } else if (level?.toLowerCase().includes('red')) {
    style.color = '#800000'
    style.fillColor = '#ff0000'
  }
  return style
}

onMounted(async () => {
  await warningStore.getAllWarnings()
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
    <!-- 左侧表格 -->
    <div class="table-section">
      <!-- 上表格 -->
      <div class="warning-card">
        <el-table
          :data="warnings"
          stripe
          style="width: 100%"
          table-layout="auto"
          :row-class-name="getRowClass"
          class="table1"
        >
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="weatherType" label="Type" width="100" />
          <!--el-table-column prop="warningLevel" label="Level" width="80" /-->

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

          <el-table-column prop="warningImpact" label="Impact" width="100" />
          <el-table-column
            prop="warningLikelihood"
            label="Likelihood"
            width="120"
          />
          <!--el-table-column prop="validFrom" label="From" width="140" /-->
          <!--el-table-column prop="validTo" label="To" width="140" /-->
          <el-table-column prop="period" label="Period" width="180" />

          <el-table-column label="Actions" width="180">
            <template #default="scope">
              <el-button
                text
                type="primary"
                size="small"
                @click="handleShowDetail(scope.row)"
                >Show Detail</el-button
              >
              <el-button
                text
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
                >Delete</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- expired table -->
      <div class="expired-card">
        <el-table
          :data="[]"
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
          <el-table-column label="Actions" width="180"> </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- map -->
    <div class="map-section">
      <div class="map-card">
        <div class="map-container">
          <MapCard
            v-if="warningPolygon.length > 0"
            :map-id="mapId"
            :locations="warningPolygon"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-layout {
  display: flex;
  height: calc(100vh - 80px);
  padding: 16px;
  gap: 16px;
  box-sizing: border-box;
  overflow-x: auto;
}

.table-section {
  /*width: 920px;*/
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 100%;
}

::v-deep(.el-table th.is-leaf > .cell) {
  justify-content: center !important;
  display: flex;
  align-items: center;
}

::v-deep(.el-table__header .el-table__cell) {
  justify-content: center !important;
  align-items: center;
}

.warning-card {
  flex: 2;
}
.expired-card {
  flex: 1;
}

/* table */
.warning-card,
.expired-card {
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

::v-deep(.table1 .el-table__body tr:nth-child(even) > td) {
  background-color: rgba(246, 226, 189, 0.1);
}

::v-deep(.table1 .el-table__body tr:nth-child(odd) > td) {
  background-color: #ffffff;
}

.warning-card .el-table,
.expired-card .el-table {
  flex: 1;
  height: 100%;
}

.map-section {
  flex: 1.2;
  display: flex;
  flex-direction: column;
}

.map-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 12px;
  overflow: hidden;
}

.map-container {
  flex: 1;
  display: flex;
  justify-content: center;
  overflow: hidden;
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

::v-deep(.el-table__cell) {
  white-space: nowrap;
}

::v-deep(.table1 td),
::v-deep(.expired-table td) {
  font-size: 13px;
  padding: 6px 8px;
}

::v-deep(.el-table__row) {
  height: 42px;
}

::v-deep(.el-table__header-wrapper th) {
  font-size: 13px;
  padding: 6px 8px;
  background-color: rgba(181, 219, 241, 0.26);
  color: #444;
  font-weight: 600;
}

::v-deep(.table1 .el-button--text) {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 10px;
  font-weight: 300;
  line-height: 1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* Show Detail */
::v-deep(.table1 .el-button--primary) {
  background-color: #ebf5ff;
  color: #409eff;
  border: none;
  transition:
    background-color 0.2s,
    color 0.2s;
}
/* hover  */
::v-deep(.table1 .el-button--primary:hover) {
  background-color: #409eff;
  color: #fff;
}

/* Delete */
::v-deep(.table1 .el-button--danger) {
  background-color: #fff0f0;
  color: #f56c6c;
  border: none;
  transition:
    background-color 0.2s,
    color 0.2s;
}
/* hover */
::v-deep(.table1 .el-button--danger:hover) {
  background-color: #f56c6c;
  color: #fff;
}

::v-deep(.table1 .el-table) {
  background-color: transparent;
  border-radius: 12px;
  overflow: hidden;
}

::v-deep(.table1 .el-table__body-wrapper tr:hover > td) {
  background-color: #f0f7ff;
  transition: background-color 0.2s ease;
}
</style>
