<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWarningStore } from '@/stores/index.ts'
import type { Style } from '@/types'
import type { MultiPolygon } from 'geojson'
import PageTopTabs from '@/components/PageSurfaceTabs.vue'

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

const route = useRoute()
const tabsConfig = [
  { label: 'Warnings', to: route.fullPath, match: () => true }
]
const handleShowDetail = (row: TableRow) => {
  router.push(`/warnings/${row.id}`)
}

const deleteId = ref<number[]>([])
const handleDelete = (row: TableRow) => {
  deleteId.value.push(row.id)
}

const processWarnings = () => {
  warningPolygon.value = []
  styles.value = []
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
  const level = (row.warningLevel || '').toLowerCase()
  if (level.includes('red')) return 'row-red'
  if (level.includes('amber')) return 'row-amber'
  if (level.includes('yellow')) return 'row-yellow'
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
  <div class="page-surface">
    <PageTopTabs :tabs="tabsConfig" />
    <div class="two-col">
      <section class="left-map">
        <div class="map-card">
          <div class="map-container">
            <MapCard
              :map-id="mapId"
              :locations="warningPolygon"
              :styles="styles"
            />
          </div>
        </div>
      </section>

      <section class="right-tables">
        <el-card class="table-card">
          <template #header>
            <div class="card-hd">Live warnings</div>
          </template>

          <el-table
            :data="liveWarnings"
            stripe
            class="tbl"
            table-layout="auto"
            :row-class-name="getRowClass"
            height="100%"
          >
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="weatherType" label="Type" width="110" />
            <el-table-column label="Level" width="90" align="center">
              <template #default="scope">
                <el-tooltip :content="scope.row.warningLevel" placement="top">
                  <span
                    class="level-badge"
                    :class="scope.row.warningLevel?.toLowerCase()"
                  />
                </el-tooltip>
              </template>
            </el-table-column>

            <el-table-column prop="warningImpact" label="Impact" width="120" />
            <el-table-column
              prop="warningLikelihood"
              label="Likelihood"
              width="130"
            />

            <el-table-column prop="period" label="Period" min-width="220" />

            <el-table-column label="Actions" fixed="right" min-width="180">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleShowDetail(scope.row)"
                  class="btn-edit"
                  >Show Detail
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(scope.row)"
                  class="btn-del"
                  >Delete
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="table-card">
          <template #header
            ><div class="card-hd">History warnings</div></template
          >

          <el-table
            :data="outdatedWarnings"
            stripe
            class="tbl"
            table-layout="auto"
            height="100%"
          >
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="weatherType" label="Type" width="110" />
            <el-table-column prop="warningLevel" label="Level" width="100" />
            <el-table-column prop="warningImpact" label="Impact" width="120" />
            <el-table-column
              prop="warningLikelihood"
              label="Likelihood"
              width="130"
            />
            <el-table-column prop="period" label="Period" min-width="220" />
            <el-table-column label="Actions" fixed="right" width="180">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  class="btn-edit"
                  @click="handleShowDetail(scope.row)"
                >
                  Show Detail
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  class="btn-del"
                  @click="handleDelete(scope.row)"
                >
                  Delete
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </section>
    </div>
  </div>
</template>

<style scoped>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /*width: 1100px;*/
  width: min(80vw, 1600px);
  max-width: calc(100% - 2rem);
  box-sizing: border-box;
}

.two-col {
  display: flex;
  gap: 16px;
  align-items: stretch;
}

.left-map {
  flex: 1;
  min-width: 380px;
}
.map-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 12px;
  height: 620px;
  min-height: 420px;
  display: flex;
  flex-direction: column;
}
.map-container {
  flex: 1;
}
::v-deep(.leaflet-container) {
  width: 100%;
  height: 100%;
  border-radius: 12px;
}

.right-tables {
  flex: 1.4;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-hd {
  font-weight: 700;
  font-size: 16px;
  padding: 6px 0;
}

.tbl {
  width: 100%;
}
::v-deep(.el-table__header-wrapper th) {
  background: #f4f7fb;
  font-weight: 600;
  color: #445;
}
::v-deep(.el-table .cell) {
  white-space: nowrap;
}
::v-deep(.el-table__body tr:hover > td) {
  background: #f0f7ff;
}

.level-badge {
  display: inline-block;
  width: 28px;
  height: 10px;
  border-radius: 6px;
}
.level-badge.yellow {
  background: #f7e359;
}
.level-badge.amber {
  background: #f7b733;
}
.level-badge.red {
  background: #e74c3c;
}

.row-yellow > td {
  background: rgba(247, 227, 89, 0.08) !important;
}
.row-amber > td {
  background: rgba(247, 183, 51, 0.08) !important;
}
.row-red > td {
  background: rgba(231, 76, 60, 0.08) !important;
}

.btn-del {
  padding: 8px 10px;
  border-radius: 8px;
  color: rgba(104, 5, 5, 0.72);
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgba(234, 215, 184, 0.5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
}

.btn-del:hover {
  color: rgba(151, 5, 5, 0.72);
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgb(255, 255, 255);
}

.btn-edit {
  padding: 8px 10px;
  border-radius: 8px;
  color: #ffffff;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background: rgba(122, 164, 194, 0.78);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
  border: none;
}
.btn-edit:hover {
  background: rgba(244, 249, 251, 0.78);
  color: rgb(34, 63, 112);
  border: none;
}

.two-col {
  --panel-h: 620px;
}

.map-card {
  height: var(--panel-h);
}

.right-tables {
  height: var(--panel-h);
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 6px;
}

.table-card {
  display: flex;
  flex-direction: column;
  height: 300px;
  overflow: hidden;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.table-card :deep(.el-card__body) {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.tbl {
  flex: 1 1 auto;
  min-height: 0;
}

.right-tables::-webkit-scrollbar {
  width: 8px;
}
.right-tables::-webkit-scrollbar-thumb {
  background: #c7ced9;
  border-radius: 4px;
}
.right-tables::-webkit-scrollbar-track {
  background: transparent;
}

@media (max-width: 900px) {
  .two-col {
    flex-direction: column;
  }
  .right-tables {
    height: auto;
    overflow: visible;
  }
  .map-card {
    height: 40vh;
    min-height: 300px;
  }
}
</style>
