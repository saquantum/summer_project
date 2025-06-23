<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useAssetsStore } from '@/stores'
import { assetUpdateInfoService } from '@/api/assets'
const assetsStore = useAssetsStore()

const props = defineProps({
  mapId: String,
  drainArea: Array,
  id: Number
})

const item =
  assetsStore.userAssets.find((item) => item.asset.id === props.id) ||
  assetsStore.allAssets.find((item) => item.asset.id === props.id)

let points = []
let map = null
let mode = 'sequence'

const handleClick = (e) => {
  const { lat, lng } = e.latlng
  const index = points.length
  points.push([lat, lng])
  const marker = L.marker([lat, lng]).addTo(map)
  marker.on('dragend', () => {
    points[index] = [marker.getLatLng().lat, marker.getLatLng().lng]
  })
}

const beginDrawing = () => {
  map.on('click', handleClick)
}

const endDrawing = async () => {
  // if there are less than 3 points, reset map
  if (points.length < 3) {
    points = []
    window.alert('You should specify more than 3 points to shape a polygon.')
  }
  // destroy all layers on the map
  map.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      map.removeLayer(layer)
    }
  })
  // then generate the new polygon area according to the mode
  if (mode === 'convex') {
    console.log(111)
  } else if (mode === 'sequence') {
    let polygonLayer = L.polygon(points).addTo(map)
    let geoJSON = polygonLayer.toGeoJSON()
    // updateDrainArea(a)

    if (item) {
      item.asset.drainArea = geoJSON
      await assetUpdateInfoService(props.id, JSON.stringify(geoJSON.geometry))
    }
  }
  map.off('click', handleClick)
}

onMounted(() => {
  map = L.map(props.mapId).setView([0, 0], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map)
  const geoLayer = L.geoJSON(props.drainArea, {
    style: (feature) => feature.geometry.style
  }).addTo(map)
  map.fitBounds(geoLayer.getBounds())
})

onBeforeUnmount(() => {
  if (map) {
    map.remove()
    map = null
  }
})

defineExpose({
  beginDrawing,
  endDrawing,
  map
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
