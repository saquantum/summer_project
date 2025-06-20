<template>
  <div :id="mapId" class="map"></div>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import L from 'leaflet'

const props = defineProps({
  mapId: String,
  drainArea: Object
})

let mapInstance = null

onMounted(() => {
  mapInstance = L.map(props.mapId).setView([0, 0], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(mapInstance)

  const geoLayer = L.geoJSON(props.drainArea).addTo(mapInstance)
  mapInstance.fitBounds(geoLayer.getBounds())
})

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<style scoped>
.map {
  height: 200px;
  width: 100%;
}
</style>
