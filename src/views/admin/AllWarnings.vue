<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWarningStore } from '@/stores'
const warnings = ref([])
const router = useRouter()

const warningStore = useWarningStore()
const mapId = 'allWarningsMap'
const handleShowDetail = (row) => {
  router.push(`/warning/${row.id}`)
}
let warningPolygon = []

onMounted(async () => {
  await warningStore.getAllWarnings()
  warnings.value = warningStore.allWarnings.map((item) => {
    return {
      id: item.id,
      weatherType: item.weatherType,
      warningImpact: item.warningImpact,
      warningLevel: item.warningLevel,
      warningLikelihood: item.warningLikelihood,
      validFrom: new Date(item.validFrom * 1000).toLocaleString(),
      validTo: new Date(item.validTo * 1000).toLocaleString()
    }
  })
  warningStore.allWarnings.forEach((item) => {
    item.area.style = setWarningLevelStyle(item.warningLevel)
    warningPolygon.push(item.area)
  })
})

const setWarningLevelStyle = (level) => {
  let style = { weight: 2, fillOpacity: 0.4 }
  if (level) {
    if (level.toLowerCase().includes('yellow')) {
      style.color = '#cc9900'
      style.fillColor = '#ffff00'
    }
    if (level.toLowerCase().includes('amber')) {
      style.color = '#cc6600'
      style.fillColor = '#ffcc00'
    }
    if (level.toLowerCase().includes('red')) {
      style.color = '#800000'
      style.fillColor = '#ff0000'
    }
  }
  return style
}
</script>

<template>
  <div class="map-container">
    <MapCard
      v-if="warningPolygon.length > 0"
      :map-id="mapId"
      :drain-area="warningPolygon"
    ></MapCard>
  </div>

  <el-table :data="warnings" stripe style="width: 100%">
    <el-table-column prop="id" label="Warning ID" width="180" />
    <el-table-column prop="weatherType" label="Weather Type" width="180" />
    <el-table-column prop="warningLevel" label="Warning Level" width="180" />
    <el-table-column prop="warningImpact" label="Warning Impact" width="180" />
    <el-table-column
      prop="warningLikelihood"
      label="Warning Likelihood"
      width="180"
    />
    <el-table-column prop="validFrom" label="Warning Level" width="180" />
    <el-table-column prop="validTo" label="Warning Level" width="180" />
    <el-table-column label="Actions">
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
  <div></div>
</template>

<style>
.map-container {
  height: 500px;
  padding: 16px 0;
  display: flex;
  justify-content: center;
}
</style>
