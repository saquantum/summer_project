<script setup lang="ts">
import { useAssetStore, useUserStore } from '@/stores/index.ts'
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
    console.log('locations getter', asset.value.location)
    return asset.value.location ? [asset.value.location] : []
  },
  set: (val: MultiPolygon[]) => {
    console.log('locations setter', val)
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
    !userStore.user?.permissionConfig.canSetPolygonOnCreate
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
  <el-card class="map-card">
    <template #header>
      <div class="card-header">
        <span>{{ asset?.name }}</span>
      </div>
    </template>
    <div class="map-container">
      <MapCard
        ref="mapCardRef"
        :map-id="'mapdetail'"
        v-model:locations="locations"
        :asset="asset"
        v-model:mode="mode"
      ></MapCard>
    </div>
  </el-card>

  <!-- action -->
  <div
    v-if="
      userStore.user?.admin ||
      userStore.user?.permissionConfig.canUpdateAssetPolygon
    "
    class="admin-section"
  >
    <h3>Admin Actions</h3>
    <div class="admin-controls">
      <div class="control-row">
        <label>Mode:</label>
        <el-select v-model="mode" class="mode-select">
          <el-option label="convex" value="convex"></el-option>
          <el-option label="sequence" value="sequence"></el-option>
        </el-select>
      </div>

      <div class="control-buttons">
        <el-button @click="prevPolygon" :disabled="mapCardRef?.disablePrev"
          >⬅</el-button
        >
        <el-button @click="nextPolygon" :disabled="mapCardRef?.disableNext"
          >➡</el-button
        >
        <el-button @click="quickEscapePolygons">reset display</el-button>

        <el-button
          v-if="!isDrawing"
          @click="beginDrawing"
          :disabled="disableSetPolygon"
          >Draw new polygon</el-button
        >
        <el-button
          v-if="isDrawing"
          @click="finishOneShape"
          :disabled="disableSetPolygon"
          >Finish one shape</el-button
        >
        <el-button
          v-if="isDrawing"
          @click="finishOnePolygon"
          :disabled="disableSetPolygon"
          >Finish one polygon</el-button
        >
        <el-button
          v-if="isDrawing"
          @click="clearCurrentPolygon"
          :disabled="disableSetPolygon"
          >Clear current polygon</el-button
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
          >End drawing</el-button
        >
        <el-button
          v-if="isDrawing"
          @click="cancelDrawing"
          :disabled="disableSetPolygon"
          >Cancel drawing</el-button
        >
      </div>
    </div>
  </div>

  <!-- form -->

  <div class="table-section">
    <h3>Warning Details</h3>
    <div class="table-container">
      <el-table
        :data="displayData"
        stripe
        class="responsive-table"
        :size="isMobile ? 'small' : 'default'"
      >
        <el-table-column
          prop="weatherType"
          label="Weather Type"
          :width="isMobile ? undefined : 180"
          :min-width="120"
        />
        <el-table-column
          prop="warningLevel"
          label="Warning Level"
          :width="isMobile ? undefined : 180"
          :min-width="120"
        />

        <el-table-column
          prop="period"
          label="Period"
          :width="isMobile ? undefined : 180"
          :min-width="110"
        />
        <el-table-column label="Actions" :min-width="150" fixed="right">
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
  </div>

  <div class="form-section">
    <AssetForm
      ref="assetFormRef"
      v-model:isEdit="isEdit"
      :item="item"
    ></AssetForm>

    <div class="action-buttons">
      <el-button v-if="!isEdit" @click="isEdit = true" type="primary">
        Edit
      </el-button>
      <el-button v-else @click="isEdit = false"> Cancel </el-button>
      <el-button v-if="isEdit" @click="submit" type="success">
        Submit
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.main-content {
  margin-bottom: 20px;
}

.map-card {
  height: 100%;
  min-height: 400px;
}

.map-container {
  height: 600px;
}

.form-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.table-section {
  margin-bottom: 30px;
}

.table-section h3 {
  margin-bottom: 15px;
  color: #303133;
}

.table-container {
  overflow-x: auto;
}

.responsive-table {
  width: 100%;
  min-width: 800px;
}

.action-cell {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.admin-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.admin-controls {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.control-row label {
  min-width: 60px;
  font-weight: 500;
}

.mode-select {
  width: 150px;
}

.control-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.multiline-text {
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .main-content {
    margin-bottom: 15px;
  }

  .map-container {
    height: 400px;
  }

  .action-buttons {
    margin-top: 15px;
  }

  .action-buttons .el-button {
    flex: 1;
    min-width: 80px;
  }

  .table-container {
    margin: 0 -10px;
  }

  .admin-section {
    padding: 15px;
    margin: 0 -10px;
  }

  .control-buttons {
    justify-content: center;
  }

  .control-buttons .el-button {
    flex: 1;
    min-width: 100px;
    margin-bottom: 5px;
  }

  .control-row {
    justify-content: center;
  }

  .mode-select {
    width: 120px;
  }
}

@media (max-width: 480px) {
  :deep(.el-row) {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  :deep(.el-col) {
    padding-left: 5px !important;
    padding-right: 5px !important;
  }

  .map-container {
    height: 300px;
  }

  .responsive-table {
    min-width: 600px;
  }

  .control-buttons .el-button {
    min-width: 90px;
    font-size: 12px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .map-container {
    height: 500px;
  }
}
</style>
