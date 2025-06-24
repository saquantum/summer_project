<script setup>
import { useRoute } from 'vue-router'
import { useAdminStore } from '@/stores'

const route = useRoute()
const adminStore = useAdminStore()

const mapId = 'map-' + route.params.id
const warning = adminStore.allWarnings.find(
  (item) => item.id === Number(route.params.id)
)

const setWarningLevelStyle = (level) => {
  const style = { weight: 2, fillOpacity: 0.4 }
  const l = level?.toLowerCase()
  if (l?.includes('yellow')) {
    style.color = '#cc9900'
    style.fillColor = '#ffff00'
  } else if (l?.includes('amber')) {
    style.color = '#cc6600'
    style.fillColor = '#ffcc00'
  } else if (l?.includes('red')) {
    style.color = '#800000'
    style.fillColor = '#ff0000'
  }
  return style
}
warning.area.style = setWarningLevelStyle(warning.warningLevel)

const displayData = [
  { label: 'Warning ID', value: warning.id },
  { label: 'Weather Type', value: warning.weatherType },
  { label: 'Warning Level', value: warning.warningLevel },
  { label: 'Warning HeadLine', value: warning.warningHeadLine },
  {
    label: 'Valid From',
    value: new Date(warning.validFrom * 1000).toLocaleString()
  },
  {
    label: 'Valid To',
    value: new Date(warning.validTo * 1000).toLocaleString()
  },
  { label: 'Warning Impact', value: warning.warningImpact },
  { label: 'Warning Likelihood', value: warning.warningLikelihood },
  {
    label: 'Affected Areas',
    value: warning.affectedAreas.replace(/\\n/g, '\n'),
    isMultiline: true
  },
  {
    label: 'Further Details',
    value: warning.warningFurtherDetails.replace(/\\n/g, '\n'),
    isMultiline: true
  },
  { label: 'Update Description', value: warning.warningUpdateDescription }
]
</script>

<template>
  <div class="map-container">
    <MapCard :map-id="mapId" :drain-area="[warning.area]" />
  </div>

  <el-descriptions title="Warning Detail" :column="1" direction="vertical">
    <el-descriptions-item
      v-for="(item, index) in displayData"
      :key="index"
      :label="item.label"
    >
      <span :class="{ 'multiline-text': item.isMultiline }">{{
        item.value
      }}</span>
    </el-descriptions-item>
  </el-descriptions>
</template>

<style>
.map-container {
  height: 500px;
  padding: 16px 0;
  display: flex;
  justify-content: center;
}

.multiline-text {
  white-space: pre-wrap;
}
</style>
