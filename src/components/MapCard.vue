<script setup>
import { onMounted, onBeforeUnmount, computed, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
import { useAssetStore, useUserStore } from '@/stores'
import { assetUpdateInfoService } from '@/api/assets'
import * as turf from '@turf/turf'

const customIcon = new L.Icon({
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

const assetStore = useAssetStore()
const userStore = useUserStore()

const props = defineProps({
  mapId: String,
  locations: Array,
  id: Number,
  mode: String
})

const emit = defineEmits(['update:mode', 'update:locations'])

const mode = computed({
  get: () => props.mode,
  set: (val) => emit('update:mode', val)
})

let item = null
if (props.id) {
  item =
    assetStore.userAssets?.find((item) => item.asset.id === props.id) ||
    assetStore.allAssets?.find((item) => item.asset.id === props.id)
}

let points = []
const polygonCoordinates = []
let map = null
let saveLayer = null
const handleClick = (e) => {
  const { lat, lng } = e.latlng
  const index = points.length
  points.push([lat, lng])
  const marker = L.marker([lat, lng], { icon: customIcon }).addTo(map)
  marker.on('dragend', () => {
    points[index] = [marker.getLatLng().lat, marker.getLatLng().lng]
  })
}

const beginDrawing = () => {
  map.on('click', handleClick)
}

const finishOneShape = async () => {
  // if there are less than 3 points, reset map
  if (points.length < 3) {
    points = []
    window.alert('You should specify more than 3 points to shape a polygon.')
    // destroy all layers on the map
    map.eachLayer((layer) => {
      if (!(layer instanceof L.TileLayer)) {
        map.removeLayer(layer)
      }
    })
    saveLayer.addTo(map)
    return
  }
  // destroy all layers on the map
  map.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      map.removeLayer(layer)
    }
  })
  // then generate the new polygon area according to the mode
  if (mode.value === 'convex') {
    // turning points to geoJSON
    const pointsGeo = turf.featureCollection(
      points.map((p) => turf.point([p[1], p[0]]))
    )

    const convexHull = turf.convex(pointsGeo)

    if (!convexHull) {
      alert('can not create polygon')
      return
    }
    polygonCoordinates.push(convexHull.geometry.coordinates)
    const multiPolygon = turf.multiPolygon(polygonCoordinates)
    // add layer
    L.geoJSON(multiPolygon).addTo(map)
  } else if (mode.value === 'sequence') {
    let polygonLayer = L.polygon(points)
    let geoJSON = polygonLayer.toGeoJSON()

    polygonCoordinates.push(geoJSON.geometry.coordinates)
    const multiPolygon = turf.multiPolygon(polygonCoordinates)
    L.geoJSON(multiPolygon).addTo(map)
  }

  // reset point
  points = []
}

const endDrawing = async () => {
  if (points.length > 0) {
    finishOneShape()
  }
  if (item) {
    // update asset
    if (polygonCoordinates.length === 0) return
    const multiPolygon = turf.multiPolygon(polygonCoordinates)
    item.asset.location = multiPolygon.geometry
    await assetUpdateInfoService(
      props.id,
      item.asset.ownerId,
      multiPolygon.geometry
    )
    await assetStore.getUserAssets(userStore.user.id)
  } else {
    // add asset
    if (polygonCoordinates.length === 0) return
    const multiPolygon = turf.multiPolygon(polygonCoordinates)
    emit('update:locations', [multiPolygon.geometry])
  }

  // clear points, turn off click
  points = []
  map.off('click', handleClick)
}

const cancelDrawing = () => {
  // destroy all layers on the map
  map.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      map.removeLayer(layer)
    }
  })
  saveLayer.addTo(map)
  // clear points, turn off click
  points = []
  map.off('click', handleClick)
}
watch(
  () => props.locations,
  (newVal) => {
    // destroy all layers on the map
    map.eachLayer((layer) => {
      if (!(layer instanceof L.TileLayer)) {
        map.removeLayer(layer)
      }
    })
    const geoLayer = L.geoJSON(newVal, {
      style: (feature) => feature.geometry.style
    }).addTo(map)
    if (geoLayer) {
      map.fitBounds(geoLayer.getBounds())
    }
  },
  {
    deep: true
  }
)

onMounted(async () => {
  map = L.map(props.mapId).setView([0, 0], 13)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map)
  saveLayer = L.geoJSON(props.locations, {
    style: (feature) => feature.geometry.style
  }).addTo(map)
  if (saveLayer) {
    map.fitBounds(saveLayer.getBounds())
  }
})

onBeforeUnmount(() => {
  if (map) {
    map.remove()
    map = null
  }
})

defineExpose({
  beginDrawing,
  finishOneShape,
  endDrawing,
  cancelDrawing,
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
