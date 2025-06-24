<script setup>
import { useAssetStore } from '@/stores'
import { ref, onMounted } from 'vue'

const mapId = 'dashboardMap'

const assetStore = useAssetStore()

let assetsPolygon = ref([])

onMounted(async () => {
  await assetStore.getAllAssets()
  assetStore.allAssets.forEach((item) => {
    item.asset.location.style = setWarningLevelStyle(
      item.warnings[0]?.warningLevel
    )
    assetsPolygon.value.push(item.asset.location)
  })
})
console.log(assetsPolygon)
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
      v-if="assetsPolygon.length > 0"
      :map-id="mapId"
      :drain-area="assetsPolygon"
    ></MapCard>
  </div>

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
