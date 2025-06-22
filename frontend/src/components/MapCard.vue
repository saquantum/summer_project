<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const props = defineProps({
  mapId: String,
  drainArea: Array
})

let mapInstance = null

onMounted(() => {
  mapInstance = L.map(props.mapId).setView([0, 0], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(mapInstance)

  const geoLayer = L.geoJSON(props.drainArea, {
    style: (feature) => feature.geometry.style
  }).addTo(mapInstance)
  mapInstance.fitBounds(geoLayer.getBounds())
})

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<template>
  <div :id="mapId" class="map"></div>
</template>

<style scoped>
.map {
  width: 100%;
  height: 100%;
}
</style>
