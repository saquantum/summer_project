<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
import { updateAssetByIdService } from '@/api/assets'
import * as turf from '@turf/turf'
import type { LeafletMouseEvent } from 'leaflet'
import { ElMessage } from 'element-plus'
import type {
  Feature,
  FeatureCollection,
  MultiPolygon,
  Polygon,
  Position
} from 'geojson'
import type { Asset, Style } from '@/types'
import { rewind } from '@turf/turf'
import { useUserStore } from '@/stores'
import { adminUpdateAssetService } from '@/api/admin'

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
  mode: string
  style: Style
  asset: Asset
}>()

const userStore = useUserStore()

const emit = defineEmits(['update:locations'])

let points: Position[] = []
let polygonCoordinates: Position[][] = []
let polygonCoorArr: Position[][][] = []
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

const focusedIndex = ref(0)
const polygonLayers: L.Layer[] = []
const highlightCurrentPolygon = () => {
  // reset layers
  if (!map || !props.locations?.[0]) return
  polygonLayers.forEach((layer) => map!.removeLayer(layer))
  polygonLayers.length = 0

  const polygons = props.locations[0].coordinates
  if (polygons.length === 0) return

  if (focusedIndex.value === polygons.length) {
    // display all polygons
    polygons.forEach((polygon) => {
      const layer = L.polygon(
        polygon.map((ring) => ring.map(([lng, lat]) => [lat, lng]))
      )
      layer.addTo(map!)
      polygonLayers.push(layer)
    })
  } else {
    // highlight focused polygon
    polygons.forEach((polygon, idx) => {
      if (idx === focusedIndex.value) {
        const layer = L.polygon(
          polygon.map((ring) => ring.map(([lng, lat]) => [lat, lng])),
          {
            color: 'red',
            weight: 3,
            fillOpacity: 0.4
          }
        )
        layer.addTo(map!)
        polygonLayers.push(layer)
      }
    })
  }
}

const prevPolygon = () => {
  if (!props.locations?.[0]) return
  const count = props.locations[0].coordinates.length
  focusedIndex.value = (focusedIndex.value - 1 + count) % count
  highlightCurrentPolygon()
}

const nextPolygon = () => {
  if (!props.locations?.[0]) return
  const count = props.locations[0].coordinates.length
  focusedIndex.value = (focusedIndex.value + 1) % count
  highlightCurrentPolygon()
}

const quickEscapePolygons = () => {
  focusedIndex.value = props.locations[0].coordinates.length
  highlightCurrentPolygon()
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
    polygonCoordinates = []
    polygonCoorArr = []
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
    if (polygonCoordinates.length == 0) {
      polygonCoordinates = convexHull.geometry.coordinates
    } else {
      polygonCoordinates.push(convexHull.geometry.coordinates[0])
    }

    // temp is only for display
    const tempMutiPolygoncoor: Position[][][] = []
    polygonCoorArr.forEach((item) => tempMutiPolygoncoor.push(item))
    tempMutiPolygoncoor.push(polygonCoordinates)
    const multiPolygon: Feature = turf.multiPolygon(tempMutiPolygoncoor)
    // add layer
    L.geoJSON(multiPolygon).addTo(m)
  } else {
    // type cast
    const leafletPoints: L.LatLngExpression[] = points.map((p) => [p[1], p[0]])
    const polygonLayer = L.polygon(leafletPoints)
    const geoJSON = polygonLayer.toGeoJSON()

    if (polygonCoordinates.length === 0) {
      polygonCoordinates = geoJSON.geometry.coordinates as Position[][]
    } else {
      polygonCoordinates.push(geoJSON.geometry.coordinates[0] as Position[])
    }
    const polygon = turf.polygon(polygonCoordinates)
    L.geoJSON(polygon).addTo(m)
  }

  points = []
}

const finishOnePolygon = () => {
  if (points.length > 0) {
    finishOneShape()
  }
  const polygon = turf.polygon(polygonCoordinates)
  const fixed = rewind(polygon, { reverse: true }) as Feature<Polygon>
  console.log(fixed)
  polygonCoorArr.push(fixed.geometry.coordinates)
  polygonCoordinates = []
}

const endDrawing = async () => {
  if (polygonCoordinates.length > 0 || points.length > 0) {
    finishOnePolygon()
  }

  if (polygonCoorArr.length === 0) return
  const multiPolygon = turf.multiPolygon(polygonCoorArr)
  console.log(multiPolygon)
  emit('update:locations', [multiPolygon.geometry])
  if (props.asset) {
    const newAsset: Asset = {
      ...props.asset,
      location: multiPolygon.geometry,
      lastModified: Date.now()
    }
    try {
      if (userStore.user?.admin) {
        await adminUpdateAssetService(newAsset)
        ElMessage.success('Asset updated')
      } else {
        if (userStore.user?.id) {
          await updateAssetByIdService(userStore.user.id, newAsset)
          ElMessage.success('Asset updated')
        }
      }
    } catch (e) {
      console.error(e)
      ElMessage.error('Failed to update asset')
    }
  }

  // clear points, turn off click
  points = []
  polygonCoordinates = []
  polygonCoorArr = []
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
  polygonCoordinates = []
  polygonCoorArr = []
  console.log(points)
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

    try {
      m.fitBounds(geoLayer.getBounds())
    } catch (e) {
      console.error(e)
    }

    highlightCurrentPolygon()
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
    try {
      map.fitBounds(saveLayer.getBounds())
    } catch (e) {
      console.error(e)
    }
  }

  highlightCurrentPolygon()
})

onBeforeUnmount(() => {
  if (map) {
    map.remove()
    map = null
  }
})

defineExpose({
  prevPolygon,
  nextPolygon,
  quickEscapePolygons,
  beginDrawing,
  finishOneShape,
  finishOnePolygon,
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
