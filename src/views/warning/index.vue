<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useWarningStore } from '@/stores'
import { onMounted, computed, ref } from 'vue'
import type { Style } from '@/types'
const route = useRoute()
const warningStore = useWarningStore()

const mapId = 'map-' + route.params.id

const warning = computed(() =>
  warningStore.allWarnings.find((item) => item.id === Number(route.params.id))
)

const style = ref<Style>({
  weight: 1,
  fillOpacity: 0,
  color: '',
  fillColor: ''
})

const setWarningLevelStyle = (level: string): Style => {
  const style = { weight: 2, fillOpacity: 0.4, color: '', fillColor: '' }
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

if (warning.value) {
  style.value = setWarningLevelStyle(warning.value.warningLevel)
}

const displayData = computed(() => {
  if (!warning.value) return []
  return [
    { label: 'Warning ID', value: warning.value.id },
    { label: 'Weather Type', value: warning.value.weatherType },
    { label: 'Warning Level', value: warning.value.warningLevel },
    { label: 'Warning HeadLine', value: warning.value.warningHeadLine },
    {
      label: 'Valid From',
      value: new Date(warning.value.validFrom * 1000).toLocaleString()
    },
    {
      label: 'Valid To',
      value: new Date(warning.value.validTo * 1000).toLocaleString()
    },
    { label: 'Warning Impact', value: warning.value.warningImpact },
    { label: 'Warning Likelihood', value: warning.value.warningLikelihood },
    {
      label: 'Affected Areas',
      value: warning.value.affectedAreas.replace(/\\n/g, '\n'),
      isMultiline: true
    },
    {
      label: 'Further Details',
      value: warning.value.warningFurtherDetails.replace(/\\n/g, '\n'),
      isMultiline: true
    },
    {
      label: 'Update Description',
      value: warning.value.warningUpdateDescription
    }
  ]
})

onMounted(() => {
  // get warnings if not exist
  if (warningStore.allWarnings.length === 0) {
    warningStore.getAllWarnings()
  }
})
</script>

<template>
  <div class="map-container">
    <MapCard :map-id="mapId" :locations="[warning?.area]" :style="style" />
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
