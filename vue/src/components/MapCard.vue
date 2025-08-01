<script setup lang="ts">
import { onMounted, onBeforeUnmount, watch, ref, computed } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'
import { updateAssetByIdService } from '@/api/assets'
import * as turf from '@turf/turf'
import type { LeafletMouseEvent } from 'leaflet'
import { ElMessage } from 'element-plus'
import type { Feature, MultiPolygon, Polygon, Position } from 'geojson'
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

/**
 * locations is for situations like multiple warnings.
 * In most situation, asset will only have one multipolygon
 */

const props = withDefaults(
  defineProps<{
    mapId: string
    locations: MultiPolygon[]
    mode: string
    styles?: Style[]
    asset: Asset
    display: boolean
  }>(),
  {
    styles: () => []
  }
)

const userStore = useUserStore()

const emit = defineEmits(['update:locations'])

let points: Position[] = []

let map: L.Map | null = null

// saveLayers is a leaflet layer arr, it will store all the layers on mounted for later use
const saveLayer: L.GeoJSON[] = []

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
const DEFAULT_MULTIPOLYGON = {
  type: 'MultiPolygon',
  coordinates: [
    [
      [
        [-0.5103751, 51.2867601],
        [0.3340155, 51.2867601],
        [0.3340155, 51.6918741],
        [-0.5103751, 51.6918741],
        [-0.5103751, 51.2867601]
      ]
    ]
  ]
}
const focusedIndex = ref(0)

const highlightCurrentPolygon = () => {
  // reset layers
  if (!map || !props.locations?.[0]) return
  // destroy all layers on the map
  map.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      map!.removeLayer(layer)
    }
  })

  if (polygons.value.length === 0) return

  if (focusedIndex.value === polygons.value.length) {
    // display all polygons
    polygons.value.forEach((polygon) => {
      const layer = L.geoJSON(polygon)
      layer.addTo(map!)
    })
  } else {
    // highlight focused polygon
    polygons.value.forEach((polygon, idx) => {
      let layer
      if (idx === focusedIndex.value) {
        layer = L.geoJSON(polygon, {
          style: () => {
            return {
              color: 'red',
              weight: 2,
              fillOpacity: 0.5
            }
          }
        })
      } else {
        layer = L.geoJSON(polygon)
      }
      layer.addTo(map!)
    })
  }
}

const extractPolygonsFromMultiPolygon = (
  multiPolygon: MultiPolygon
): Polygon[] => {
  const feature = turf.multiPolygon(multiPolygon.coordinates)
  const polygons: Polygon[] = []

  const flattened = turf.flatten(feature)

  flattened.features.forEach((feature) => {
    if (feature.geometry.type === 'Polygon') {
      polygons.push(feature.geometry)
    }
  })

  return polygons
}

// Convert polygons array to MultiPolygon
const createMultiPolygonFromPolygons = (polygons: Polygon[]): MultiPolygon => {
  const coordinates = polygons.map((polygon) => polygon.coordinates)
  const multiPolygonFeature = turf.multiPolygon(coordinates)
  return multiPolygonFeature.geometry
}

const currentPolygon = computed(() => {
  if (focusedIndex.value >= 0 && focusedIndex.value < polygons.value.length) {
    return polygons.value[focusedIndex.value]
  }
  return null
})

const polygons = ref<Polygon[]>([])

const prevPolygon = () => {
  if (polygons.value.length <= 0 || focusedIndex.value <= 0) return
  focusedIndex.value = focusedIndex.value - 1
  highlightCurrentPolygon()
}

const disablePrev = computed(() => {
  if (focusedIndex.value <= 0) return true
  return false
})

const nextPolygon = () => {
  if (polygons.value.length <= 0 || focusedIndex.value >= polygons.value.length)
    return
  if (focusedIndex.value === polygons.value.length - 1) {
    finishOnePolygon()
    focusedIndex.value = polygons.value.length
  } else {
    focusedIndex.value = focusedIndex.value + 1
  }
  highlightCurrentPolygon()
}

const disableNext = computed(() => {
  if (polygons.value.length === focusedIndex.value) return true
  return false
})

const quickEscapePolygons = () => {
  focusedIndex.value = -1
  highlightCurrentPolygon()
}

const beginDrawing = () => {
  if (!map) return

  // clear place holder
  if (
    props.locations[0] &&
    JSON.stringify(props.locations[0]) === JSON.stringify(DEFAULT_MULTIPOLYGON)
  ) {
    map.eachLayer((layer) => {
      if (!(layer instanceof L.TileLayer)) {
        map!.removeLayer(layer)
      }
    })
  }
  map.on('click', handleClick)
}

const finishOneShape = async () => {
  /**
   * find current polygon, modify on it
   */

  console.log(currentPolygon.value)
  const m = map
  const layers = saveLayer
  if (!m || !layers) return
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
    // Re-add all saved layers
    layers.forEach((layer) => layer.addTo(m))
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
      // Re-add all saved layers
      layers.forEach((layer) => layer.addTo(m))
      return
    }
    if (!currentPolygon.value) {
      polygons.value.push(convexHull.geometry)
    } else {
      polygons.value[focusedIndex.value].coordinates.push(
        convexHull.geometry.coordinates[0]
      )
    }
    console.log(polygons.value)

    polygons.value.forEach((polygon) => {
      const layer = L.geoJSON(polygon)
      layer.addTo(map!)
    })
  } else {
    // type cast
    const leafletPoints: L.LatLngExpression[] = points.map((p) => [p[1], p[0]])
    const polygonLayer = L.polygon(leafletPoints)
    const geoJSON = polygonLayer.toGeoJSON()
    const polygon = geoJSON.geometry as Polygon
    if (!currentPolygon.value) {
      polygons.value.push(polygon)
    } else {
      polygons.value[focusedIndex.value].coordinates.push(
        polygon.coordinates[0]
      )
    }
  }

  // reset point
  points = []
  highlightCurrentPolygon()
}

const finishOnePolygon = () => {
  if (points.length > 0) {
    finishOneShape()
  }

  try {
    if (currentPolygon.value) {
      const fixed = rewind(currentPolygon.value, {
        reverse: true
      }) as Polygon
      console.log(fixed)
      polygons.value[focusedIndex.value] = fixed
      focusedIndex.value = polygons.value.length
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('Fail to form polygon')
  }
  highlightCurrentPolygon()
}

const endDrawing = async () => {
  if (points.length > 0) {
    finishOnePolygon()
  }

  if (polygons.value.length === 0) return

  // Create MultiPolygon from polygons array
  const multiPolygon = createMultiPolygonFromPolygons(polygons.value)
  console.log(multiPolygon)
  emit('update:locations', [multiPolygon])
  if (props.asset) {
    const newAsset: Asset = {
      ...props.asset,
      location: multiPolygon
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
  if (map) {
    map.off('click', handleClick)
  }
}

const cancelDrawing = () => {
  const m = map
  const layers = saveLayer
  if (!m || !layers) return
  // destroy all layers on the map
  m.eachLayer((layer) => {
    if (!(layer instanceof L.TileLayer)) {
      m.removeLayer(layer)
    }
  })
  // Re-add all saved layers
  layers.forEach((layer) => layer.addTo(m))
  // clear points, turn off click
  points = []
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

    // Create separate layers for each MultiPolygon
    const layers: L.GeoJSON[] = []

    props.locations.forEach((geometry, idx) => {
      const feature: Feature = {
        type: 'Feature',
        geometry,
        properties: { index: idx }
      }

      const layer = L.geoJSON(feature, {
        style: () => {
          if (props.styles?.length > 0 && props.styles[idx]) {
            return props.styles[idx]
          }
          return {}
        }
      })

      layer.addTo(m)
      layers.push(layer)
    })

    // Fit bounds to all layers
    if (layers.length > 0) {
      try {
        const group = L.featureGroup(layers)
        m.fitBounds(group.getBounds())
      } catch (e) {
        console.error(e)
      }
    }

    // highlightCurrentPolygon()
  },
  {
    deep: true
  }
)

onMounted(async () => {
  map = L.map(props.mapId).setView([0, 0], 13)
  // if in display mode, disable interactive function
  if (props.display) {
    map.dragging.disable()
    map.touchZoom.disable()
    map.doubleClickZoom.disable()
    map.scrollWheelZoom.disable()
    map.boxZoom.disable()
    map.keyboard.disable()
  }

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map)

  // Create separate layers for each MultiPolygon
  const layers: L.GeoJSON[] = []

  props.locations.forEach((geometry, idx) => {
    const feature: Feature = {
      type: 'Feature',
      geometry,
      properties: { index: idx }
    }

    const layer = L.geoJSON(feature, {
      style: () => {
        if (props.styles?.length > 0 && props.styles[idx]) {
          return props.styles[idx]
        }
        return {}
      }
    })

    if (map) {
      layer.addTo(map)
    }

    layers.push(layer)
  })

  // Store all layers for later use
  saveLayer.length = 0 // Clear existing layers
  saveLayer.push(...layers)

  console.log(saveLayer)

  // Fit bounds to all layers
  if (layers.length > 0 && map) {
    try {
      const group = L.featureGroup(layers)
      map.fitBounds(group.getBounds())
    } catch (e) {
      console.error(e)
    }
  }

  if (
    props.locations[0] &&
    JSON.stringify(props.locations[0]) !== JSON.stringify(DEFAULT_MULTIPOLYGON)
  ) {
    // conver multipolygon to polygon, this is only for asset
    polygons.value = extractPolygonsFromMultiPolygon(props.locations[0])
  }

  // highlightCurrentPolygon()
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
  map,
  disablePrev,
  disableNext
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
