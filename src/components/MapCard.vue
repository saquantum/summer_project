<script setup>
import { onMounted, onBeforeUnmount, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
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

const props = defineProps({
  mapId: String,
  locations: Array,
  id: Number,
  mode: String,
  ownerId: String
})

const emit = defineEmits(['update:locations'])

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
    ElMessage.error('You should specify more than 3 points to shape a polygon.')
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
  if (props.mode === 'convex') {
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
  } else {
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

  if (polygonCoordinates.length === 0) return
  const multiPolygon = turf.multiPolygon(polygonCoordinates)
  emit('update:locations', [multiPolygon.geometry])
  if (props.id) {
    // update asset
    await assetUpdateInfoService(props.id, props.ownerId, multiPolygon.geometry)
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
    console.log(newVal)
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

/* Extra small (phones) */
@media (max-width: 575px) {
  /* Styles for very small screens */
}

/* Small (phones in landscape) */
@media (min-width: 576px) and (max-width: 767px) {
  /* Styles for small screens */
}

/* Medium (tablets) */
@media (min-width: 768px) and (max-width: 991px) {
  /* Styles for tablets */
}

/* Large (desktops) */
@media (min-width: 992px) and (max-width: 1199px) {
  /* Styles for desktops */
}

/* Extra Large (large desktops) */
@media (min-width: 1200px) {
  /* Styles for large desktops */
}
</style>
