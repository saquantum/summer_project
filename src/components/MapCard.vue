<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
import { assetUpdateInfoService } from '@/api/assets'
import * as turf from '@turf/turf'
import type { LeafletMouseEvent } from 'leaflet'
import { ElMessage } from 'element-plus'
import type {
  Feature,
  FeatureCollection,
  MultiPolygon,
  Position
} from 'geojson'
import type { Style } from '@/types'

const customIcon = new L.Icon({
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

const props = defineProps<{
  mapId: string
  locations: MultiPolygon[]
  id: string
  mode: string
  ownerId: string
  style: Style
}>()

const emit = defineEmits(['update:locations'])

let points: Position[] = []
const polygonCoordinates: Position[][][] = []
let map: L.Map | null = null
let saveLayer: L.GeoJSON | null = null
const handleClick = (e: LeafletMouseEvent) => {
  if (!map) return
  const { lat, lng } = e.latlng
  const index = points.length
  const position: Position = [lng, lat]
  points.push(position)
  const marker = L.marker([lat, lng], { icon: customIcon }).addTo(map)
  marker.on('dragend', () => {
    points[index] = [marker.getLatLng().lat, marker.getLatLng().lng]
  })
}

const beginDrawing = () => {
  if (!map) return
  map.on('click', handleClick)
}

const finishOneShape = async () => {
  const m = map
  const layer = saveLayer
  if (!m || !layer) return
  // if there are less than 3 points, reset map
  if (points.length < 3) {
    points = []
    ElMessage.error('You should specify more than 3 points to shape a polygon.')
    // destroy all layers on the map
    m.eachLayer((layer) => {
      if (!(layer instanceof L.TileLayer)) {
        m.removeLayer(layer)
      }
    })
    layer.addTo(m)
    return
  }
  // destroy all layers on the map
  m.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      m.removeLayer(layer)
    }
  })
  // then generate the new polygon area according to the mode
  if (props.mode === 'convex') {
    // turning points to geoJSON
    const pointsGeo = turf.featureCollection(
      points.map((p) => turf.point([p[0], p[1]]))
    )

    const convexHull = turf.convex(pointsGeo)

    if (!convexHull) {
      points = []
      ElMessage.error('can not create polygon')
      // destroy all layers on the map
      m.eachLayer((layer) => {
        if (!(layer instanceof L.TileLayer)) {
          m.removeLayer(layer)
        }
      })
      layer.addTo(m)
      return
    }
    polygonCoordinates.push(convexHull.geometry.coordinates)
    const multiPolygon: Feature = turf.multiPolygon(polygonCoordinates)
    // add layer
    L.geoJSON(multiPolygon).addTo(m)
  } else {
    // type cast
    const leafletPoints: L.LatLngExpression[] = points.map((p) => [p[1], p[0]])
    const polygonLayer = L.polygon(leafletPoints)
    const geoJSON = polygonLayer.toGeoJSON()

    polygonCoordinates.push(geoJSON.geometry.coordinates as Position[][])
    const multiPolygon = turf.multiPolygon(polygonCoordinates)
    L.geoJSON(multiPolygon).addTo(m)
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
  if (map) {
    map.off('click', handleClick)
  }
}

const cancelDrawing = () => {
  const m = map
  const layer = saveLayer
  if (!m || !layer) return
  // destroy all layers on the map
  m.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      m.removeLayer(layer)
    }
  })
  layer.addTo(m)
  // clear points, turn off click
  points = []
  m.off('click', handleClick)
}

watch(
  () => props.locations,
  (newVal) => {
    const m = map
    if (!m) return

    m.eachLayer((layer) => {
      if (!(layer instanceof L.TileLayer)) {
        m.removeLayer(layer)
      }
    })

    if (!newVal || newVal.length === 0) return

    const featureCollection: FeatureCollection = {
      type: 'FeatureCollection',
      features: newVal.map((geometry) => ({
        type: 'Feature',
        geometry,
        properties: {}
      }))
    }

    const geoLayer = L.geoJSON(featureCollection, {
      style: props.style
    }).addTo(m)

    m.fitBounds(geoLayer.getBounds())
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

  const featureCollection: FeatureCollection = {
    type: 'FeatureCollection',
    features: props.locations?.map((geometry) => ({
      type: 'Feature',
      geometry,
      properties: {}
    }))
  }

  saveLayer = L.geoJSON(featureCollection, {
    style: props.style
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
