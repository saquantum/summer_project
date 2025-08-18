<script setup lang="ts">
import { useAssetStore, useUserStore } from '@/stores'
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, type ComponentPublicInstance } from 'vue'
import type { Asset, AssetWithWarnings } from '@/types/asset'
import type { MultiPolygon } from 'geojson'
import type MapCard from '@/components/MapCard.vue'

interface WarningTableRow {
  id: number
  weatherType: string
  warningLevel: string
  warningLikelihood: string
  validFrom: string
  validTo: string
}

const route = useRoute()
const router = useRouter()
const assetStore = useAssetStore()
const userStore = useUserStore()

const id = route.params.id

const item = computed<AssetWithWarnings>(() => {
  const item =
    assetStore.userAssets?.find((item) => item.asset.id === id) ||
    assetStore.allAssets?.find((item) => item.asset.id === id)
  if (!item) throw new Error(`Can find asset ${id}`)
  else return item
})

const isEdit = ref(false)
const assetFormRef = ref()
const submit = () => {
  assetFormRef.value.submit()
  isEdit.value = false
}

const asset = computed<Asset>(() => item.value.asset)

const displayData = computed(() => {
  const warnings = item.value.warnings
  if (!warnings || warnings.length === 0) return []
  return warnings.map((warning) => ({
    id: warning.id,
    weatherType: warning.weatherType,
    warningLevel: warning.warningLevel,
    period: `${new Date(warning.validFrom * 1000).toLocaleString()} - ${new Date(warning.validTo * 1000).toLocaleString()}`
  }))
})

const mapCardRef = ref<ComponentPublicInstance<typeof MapCard> | null>(null)

const mode = ref<'convex' | 'sequence'>('convex')

/**
 * The reason for using an array is to be compatible with warnings.
 */

const locations = computed({
  get: () => {
    return asset.value.location ? [asset.value.location] : []
  },
  set: (val: MultiPolygon[]) => {
    asset.value.location = val[0]
  }
})

const isDrawing = ref(false)

const beginDrawing = () => {
  isDrawing.value = true
  mapCardRef.value?.beginDrawing()
}

const finishOneShape = () => {
  mapCardRef.value?.finishOneShape()
}

const finishOnePolygon = () => {
  mapCardRef.value?.finishOnePolygon()
}

const endDrawing = () => {
  isDrawing.value = false
  mapCardRef.value?.endDrawing()
}

const cancelDrawing = () => {
  isDrawing.value = false
  console.log(locations.value)
  mapCardRef.value?.cancelDrawing()
}

const prevPolygon = () => {
  mapCardRef.value?.prevPolygon()
}

const nextPolygon = () => {
  mapCardRef.value?.nextPolygon()
}

const quickEscapePolygons = () => {
  mapCardRef.value?.quickEscapePolygons()
}

const clearAll = () => {
  mapCardRef.value?.clearAll()
}

const clearCurrentPolygon = () => {
  mapCardRef.value?.clearCurrentPolygon()
}

const handleShowDetail = (row: WarningTableRow) => {
  router.push(`/warnings/${row.id}`)
}

const disableSetPolygon = computed(() => {
  if (
    !userStore.user?.admin &&
    !userStore.user?.accessControlGroup.canSetPolygonOnCreate
  )
    return true
  return false
})

const isMobile = computed(() => {
  if (typeof window !== 'undefined') {
    return window.innerWidth <= 768
  }
  return false
})
</script>

<template>
  <div class="asset-detail-page">
    <div class="page-header">
      <div class="title-wrap">
        <div class="asset-name">{{ asset?.name }}</div>
        <div v-if="displayData.length" class="badge">
          {{ displayData.length }} warnings
        </div>
      </div>
    </div>

    <!-- Warnings table -->
    <el-card class="card-elevated table-card">
      <template #header>
        <div class="card-header">
          <span>Warning Details</span>
        </div>
      </template>

      <div class="table-container">
        <el-table
          :data="displayData"
          class="responsive-table equal-table"
          :size="isMobile ? 'small' : 'default'"
          table-layout="fixed"
          :fit="true"
          style="width: 100%"
        >
          <el-table-column
            prop="weatherType"
            label="Weather Type"
            align="center"
            header-align="center"
          />
          <el-table-column
            prop="warningLevel"
            label="Warning Level"
            align="center"
            header-align="center"
          />
          <el-table-column
            prop="period"
            label="Period"
            align="center"
            header-align="center"
          />
          <el-table-column label="Actions" align="center" header-align="center">
            <template #default="scope">
              <div class="action-cell">
                <el-button
                  text
                  type="primary"
                  :size="isMobile ? 'small' : 'default'"
                  @click="handleShowDetail(scope.row)"
                >
                  Detail
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-row :gutter="16" class="content-grid">
      <!-- Left: Map -->
      <el-col :xs="24" :sm="24" :md="14" :lg="14">
        <el-card class="card-elevated map-card">
          <template #header>
            <div class="card-header">
              <span>Location</span>
            </div>
          </template>
          <div class="map-container">
            <MapCard
              ref="mapCardRef"
              :map-id="'mapdetail'"
              v-model:locations="locations"
              :asset="asset"
              v-model:mode="mode"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10" :lg="10" class="right-col">
        <!-- Admin -->
        <el-card
          v-if="
            userStore.user?.admin ||
            userStore.user?.accessControlGroup.canUpdateAssetPolygon
          "
          class="card-elevated admin-card"
        >
          <template #header>
            <div class="card-header">
              <span>Admin Actions</span>
            </div>
          </template>

          <div class="admin-controls">
            <div class="control-row">
              <label>Mode</label>
              <el-select v-model="mode" class="mode-select">
                <el-option label="convex" value="convex" />
                <el-option label="sequence" value="sequence" />
              </el-select>
            </div>

            <div class="toolbar">
              <div class="toolbar-group">
                <el-button
                  @click="prevPolygon"
                  :disabled="mapCardRef?.disablePrev"
                  >⬅</el-button
                >
                <el-button
                  @click="nextPolygon"
                  :disabled="mapCardRef?.disableNext"
                  >➡</el-button
                >
                <el-button
                  class="styled-btn reset-btn"
                  @click="quickEscapePolygons"
                  >Reset display</el-button
                >
                <div class="toolbar-group">
                  <el-button
                    class="styled-btn"
                    v-if="!isDrawing"
                    @click="beginDrawing"
                    :disabled="disableSetPolygon"
                    type="primary"
                    >Draw polygon</el-button
                  >
                </div>
                <div class="toolbar-group" v-if="isDrawing">
                  <el-button
                    v-if="isDrawing"
                    @click="finishOneShape"
                    :disabled="disableSetPolygon"
                    >Finish shape</el-button
                  >
                  <el-button
                    v-if="isDrawing"
                    @click="finishOnePolygon"
                    :disabled="disableSetPolygon"
                    >Finish polygon</el-button
                  >
                  <el-button
                    v-if="isDrawing"
                    @click="clearCurrentPolygon"
                    :disabled="disableSetPolygon"
                    >Clear current</el-button
                  >
                  <el-button
                    v-if="isDrawing"
                    @click="clearAll"
                    :disabled="disableSetPolygon"
                    >Clear all</el-button
                  >
                  <el-button
                    v-if="isDrawing"
                    @click="endDrawing"
                    :disabled="disableSetPolygon"
                    type="success"
                    >End drawing</el-button
                  >
                  <el-button
                    v-if="isDrawing"
                    @click="cancelDrawing"
                    :disabled="disableSetPolygon"
                    type="warning"
                    >Cancel</el-button
                  >
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- Asset form -->
        <el-card class="card-elevated form-card">
          <template #header>
            <div class="card-header card-header-actions">
              <span>Asset Details</span>
              <div class="header-actions">
                <el-button
                  v-if="!isEdit"
                  @click="isEdit = true"
                  class="styled-btn"
                  type="primary"
                  size="small"
                  >Edit</el-button
                >

                <template v-else>
                  <el-button
                    class="styled-btn btn-cancel"
                    @click="isEdit = false"
                    size="small"
                    >Cancel</el-button
                  >
                  <el-button
                    @click="submit"
                    class="styled-btn"
                    type="success"
                    size="small"
                    >Submit</el-button
                  >
                </template>
              </div>
            </div>
          </template>

          <div class="form-section">
            <AssetForm
              ref="assetFormRef"
              v-model:isEdit="isEdit"
              :item="item"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.asset-detail-page {
  --gap: 16px;
  --gap-lg: 20px;
  --radius: 14px;
  --shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  --card-bg: #fff;
  --soft-border: #ebeef5;
  --header-bg: rgba(255, 255, 255, 0.88);
  --header-border: #e8eaef;
  --title: #1f2d3d;
  --muted: #606266;
  display: block;
  padding: var(--gap);
}

.page-header {
  top: 0;
  display: flex;
  gap: var(--gap);
  padding: 12px 16px;
  background: var(--header-bg);
  border-radius: 12px;
  margin-bottom: var(--gap-lg);
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.asset-name {
  font-size: 22px;
  font-weight: 600;
  color: var(--title);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge {
  padding: 2px 8px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 999px;
  background: rgba(237, 174, 174, 0.39);
  color: #7c1b05;
  border: 1px solid #fffbfb;
}

.header-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.content-grid {
  margin-top: var(--gap);
}

.card-elevated {
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  border: 1px solid var(--soft-border);
  background: var(--card-bg);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  min-height: 40px;
  font-weight: 600;
  color: var(--title);
}

.map-card {
  height: 100%;
}

.map-container {
  height: clamp(320px, 60vh, 680px);
}

.right-col > .el-card + .el-card {
  margin-top: var(--gap);
}

.admin-controls {
  display: grid;
  gap: 14px;
  margin-bottom: 15px;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.control-row label {
  min-width: 52px;
  font-weight: 500;
  color: var(--muted);
}

.mode-select {
  width: 180px;
  max-width: 100%;
}

.toolbar {
  display: grid;
  gap: 10px;
}
.toolbar-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.table-container {
  overflow-x: auto;
}
.responsive-table {
  width: 100%;
  min-width: 720px;
}
.action-cell {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-grid .el-col > .el-card + .el-card {
  margin-top: var(--gap);
}

.form-card .card-header.card-header-actions {
  justify-content: space-between;
}

.form-card .header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 8px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.btn-cancel:hover {
  background-image: linear-gradient(
    to bottom,
    #782d2d 0%,
    #852e2e 10%,
    #903737 100%
  );
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.reset-btn {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.37);
  border: 1px solid #fff;
}

.reset-btn:hover {
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.equal-table :deep(.el-table__cell) {
  border-right: 0 !important;
}

.equal-table :deep(.el-table__header th.el-table__cell) {
  background-color: #f5f7fb;
  font-weight: 600;
}

.equal-table :deep(.el-table__body td.el-table__cell) {
  background-color: #fbfcff;
}

.equal-table
  :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #f2f6ff;
}

.equal-table :deep(.el-table__body tr:hover > td) {
  background-color: #eaf3ff;
}

@media (min-width: 1024px) {
  .asset-detail-page {
    width: 1380px;
    margin: 10px auto;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .admin-card {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .asset-detail-page {
    --gap: 12px;
    --gap-lg: 16px;
    padding: var(--gap);
    width: auto;
  }
  .page-header {
    padding: 10px 12px;
    border-radius: 10px;
  }
  .map-card {
    margin-bottom: 20px;
  }
  .map-container {
    height: clamp(260px, 48vh, 420px);
  }
  .mode-select {
    width: 140px;
  }
  .responsive-table {
    min-width: 600px;
  }
  .admin-card {
    margin-top: 20px;
  }
}
</style>
