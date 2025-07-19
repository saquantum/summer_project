<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import { adminGetUserInfoService } from '@/api/admin'
import { userCheckUIDService, userInsertAssetService } from '@/api/user'
import { adminInsertAssetService } from '@/api/admin'
import type { Feature, MultiPolygon } from 'geojson'
import { ElMessage, type FormItemRule } from 'element-plus'
import type { ComponentPublicInstance } from 'vue'
import type MapCard from '@/components/MapCard.vue'
import type { AssetForm, NominatimResult } from '@/types'
import { trimForm } from '@/utils/formUtils'
import CodeUtil from '@/utils/codeUtil'

// user store
const userStore = useUserStore()
const assetStore = useAssetStore()

const convertToGeoJSON = (
  data: NominatimResult,
  type: 'point' | 'polygon' | 'multipolygon' = 'point'
): Feature => {
  if (!data || !data.lat || !data.lon) {
    throw new Error('Invalid input data: missing lat/lon')
  }

  const properties = {
    place_id: data.place_id,
    name: data.name,
    display_name: data.display_name,
    class: data.class,
    type: data.type,
    importance: data.importance
  }

  if (!Array.isArray(data.boundingbox) || data.boundingbox.length < 4) {
    throw new Error('Invalid boundingbox')
  }
  const [south, north, west, east] = data.boundingbox.map(Number) as [
    number,
    number,
    number,
    number
  ]
  if (type === 'multipolygon' && data.boundingbox) {
    const polygon = [
      [west, south],
      [east, south],
      [east, north],
      [west, north],
      [west, south] // close the polygon
    ]

    const coordinates = [[polygon]]

    return {
      type: 'Feature',
      geometry: {
        type: 'MultiPolygon',
        coordinates
      },
      properties
    }
  } else if (type === 'polygon' && data.boundingbox) {
    const polygon = [
      [west, south],
      [east, south],
      [east, north],
      [west, north],
      [west, south] // close the polygon
    ]

    const coordinates = [polygon]

    return {
      type: 'Feature',
      geometry: {
        type: 'Polygon',
        coordinates
      },
      properties
    }
  }

  // default to Point
  return {
    type: 'Feature',
    geometry: {
      type: 'Point',
      coordinates: [parseFloat(data.lon), parseFloat(data.lat)]
    },
    properties
  }
}

const disableForUser = ref(false)

const disableAddAsset = computed(() => {
  if (
    !userStore.user?.admin &&
    !userStore.user?.permissionConfig.canCreateAsset
  )
    return true
  return false
})

const disableSetPolygon = computed(() => {
  if (
    !userStore.user?.admin &&
    !userStore.user?.permissionConfig.canSetPolygonOnCreate
  )
    return true
  return false
})

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

const form = ref<AssetForm>({
  username: '',
  name: '',
  typeId: '',
  ownerId: '',
  address: '',
  locations: [JSON.parse(JSON.stringify(DEFAULT_MULTIPOLYGON))],
  capacityLitres: 0,
  material: '',
  status: '',
  installedAt: '',
  lastInspection: '',
  location: {
    type: 'MultiPolygon',
    coordinates: []
  }
})

const formRef = ref<InstanceType<
  (typeof import('element-plus'))['ElForm']
> | null>(null)

const rules = {
  username: [
    { required: true, message: 'Please input username', trigger: 'blur' },
    {
      validator: async (
        rule: FormItemRule,
        value: string,
        callback: (_error?: Error) => void
      ) => {
        const res = await userCheckUIDService(value)
        if (CodeUtil.isSuccess(res.code)) {
          if (userStore.user?.admin) {
            const res = await adminGetUserInfoService(value)
            if (res.data.admin) {
              callback(new Error('Can not add asset to admin'))
              return
            }
          }
          callback()
        } else {
          callback(new Error(`Username ${value} does not exist.`))
        }
      },
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: 'Please input asset name', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: 'Please input asset type', trigger: 'blur' }
  ],
  material: [
    { required: true, message: 'Please input asset material', trigger: 'blur' }
  ],
  status: [
    { required: true, message: 'Please input asset status', trigger: 'blur' }
  ],
  capacityLitres: [
    {
      required: true,
      message: 'Please input asset capacity litres',
      trigger: 'blur'
    }
  ],
  installedAt: [
    { required: true, message: 'Please input time', trigger: 'blur' }
  ],
  lastInspection: [
    { required: true, message: 'Please input time', trigger: 'blur' }
  ]
}

const materialOption = [
  { label: 'Steel', value: 'Steel' },
  { label: 'Concrete', value: 'Concrete' },
  { label: 'Plastic', value: 'Plastic' },
  { label: 'Composite', value: 'Composite' }
]

const statusOption = [
  { label: 'inactive', value: 'inactive' },
  { label: 'active', value: 'active' },
  { label: 'maintenance', value: 'maintenance' }
]

const mode = ref('convex')
const mapCardRef = ref<ComponentPublicInstance<typeof MapCard> | null>(null)
const isDrawing = ref(false)

const searchLocation = async (address: string) => {
  if (!address) return
  const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`
  const data = (await request(url)) as NominatimResult[]
  if (data.length > 0) {
    const multiPolygon = convertToGeoJSON(data[0]!, 'multipolygon')
    form.value.locations = []
    form.value.locations.push(multiPolygon.geometry as MultiPolygon)
  }
  console.log(form.value.locations)
}

const disabledAfterToday = (time: Date) => {
  return time.getTime() > Date.now()
}

const beginDrawing = () => {
  isDrawing.value = true
  mapCardRef.value?.beginDrawing()
}

const finishOneShape = () => {
  mapCardRef.value?.finishOneShape()
}

const finishOnePolygon = () => {
  mapCardRef.value?.finishOnePolygon()
}

const endDrawing = () => {
  isDrawing.value = false
  mapCardRef.value?.endDrawing()
}

const cancelDrawing = () => {
  isDrawing.value = false
  mapCardRef.value?.cancelDrawing()
}

const prevPolygon = () => {
  mapCardRef.value?.prevPolygon()
}

const nextPolygon = () => {
  mapCardRef.value?.nextPolygon()
}

const quickEscapePolygons = () => {
  mapCardRef.value?.quickEscapePolygons()
}

const userSubmit = async () => {
  trimForm(form.value)
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (fields) {
    if (fields) {
      const firstErrorField = Object.keys(fields)[0]
      formRef.value.scrollToField(firstErrorField)
      return
    }
  }
  form.value.location = form.value.locations[0] ?? ''
  if (userStore.user?.assetHolderId) {
    form.value.ownerId = userStore.user.assetHolderId
  }
  form.value.capacityLitres = Number(form.value.capacityLitres)

  // if user did not set polygon, clear
  if (
    JSON.stringify(form.value.location) === JSON.stringify(DEFAULT_MULTIPOLYGON)
  ) {
    form.value.location = {
      type: 'MultiPolygon',
      coordinates: []
    }
  }
  try {
    if (userStore.user?.assetHolderId) {
      await userInsertAssetService(userStore.user?.id, form.value)
      ElMessage.success('Successfully add an asset')
    }
  } catch {
    ElMessage.error('An error occurs during adding an asset')
  }
}

const adminSubmit = async () => {
  trimForm(form.value)
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (fields) {
    if (fields) {
      const firstErrorField = Object.keys(fields)[0]
      formRef.value.scrollToField(firstErrorField)
      return
    }
  }
  form.value.location = form.value.locations[0] ?? ''

  const res = await adminGetUserInfoService(form.value.username)
  if (res.data.assetHolderId) {
    form.value.ownerId = res.data.assetHolderId
  }

  form.value.capacityLitres = Number(form.value.capacityLitres)
  // if user did not set polygon, clear
  if (
    JSON.stringify(form.value.location) === JSON.stringify(DEFAULT_MULTIPOLYGON)
  ) {
    form.value.location = {
      type: 'MultiPolygon',
      coordinates: []
    }
  }
  try {
    await adminInsertAssetService(form.value)
    ElMessage.success('Successfully add an asset')
  } catch {
    ElMessage.error('An error occurs during adding an asset')
  }
}

const reset = () => {
  form.value = {
    username: '',
    name: '',
    typeId: '',
    ownerId: '',
    address: '',
    locations: [JSON.parse(JSON.stringify(DEFAULT_MULTIPOLYGON))],
    capacityLitres: 0,
    material: '',
    status: '',
    installedAt: '',
    lastInspection: '',
    location: {
      type: 'MultiPolygon',
      coordinates: []
    }
  }
}

watch(
  () => form.value.locations,
  (newVal) => {
    console.log(newVal)
  },
  {
    deep: true
  }
)

onMounted(() => {
  if (!userStore.user?.admin && userStore.user?.id) {
    form.value.username = userStore.user?.id
    disableForUser.value = true
  }
})
</script>

<template>
  <div class="container">
    <div>
      <h3 v-if="disableAddAsset">You can not add asset right now</h3>
      <el-form
        :model="form"
        ref="formRef"
        label-width="auto"
        label-position="left"
        :rules="rules"
        :disabled="disableAddAsset"
      >
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" :disabled="disableForUser" />
        </el-form-item>
        <el-form-item label="Asset name" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Asset type" prop="typeId">
          <el-select v-model="form.typeId" placeholder="Select type">
            <el-option
              v-for="item in assetStore.typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Asset material" prop="material">
          <el-select v-model="form.material" placeholder="Select material">
            <el-option
              v-for="item in materialOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Asset status" prop="status">
          <el-select v-model="form.status" placeholder="Select status">
            <el-option
              v-for="item in statusOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Capacity litres" prop="capacityLitres">
          <el-input v-model="form.capacityLitres" type="number"></el-input>
        </el-form-item>

        <el-form-item label="Installed at" prop="installedAt">
          <el-date-picker
            v-model="form.installedAt"
            type="date"
            placeholder="Pick a day"
            :disabled-date="disabledAfterToday"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Last inspection" prop="lastInspection">
          <el-date-picker
            v-model="form.lastInspection"
            type="date"
            placeholder="Pick a day"
            :disabled-date="disabledAfterToday"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Address">
          <el-input
            v-model="form.address"
            type="textarea"
            placeholder="Please input address"
          /><el-button @click="searchLocation(form.address)">Search</el-button>
        </el-form-item>

        <el-form-item>
          <div v-if="form.locations.length > 0">
            <div><h3>Customise polygon</h3></div>
            <div v-if="isDrawing">Your are now drawing new polygon</div>
            <div class="map-container">
              <MapCard
                ref="mapCardRef"
                :map-id="'AddAsset'"
                v-model:locations="form.locations"
                v-model:mode="mode"
              ></MapCard>
            </div>
            <el-select
              :disabled="isDrawing || disableSetPolygon"
              v-model="mode"
              style="margin-top: 10px"
            >
              <el-option label="convex" value="convex"></el-option>
              <el-option label="sequence" value="sequence"></el-option>
            </el-select>
            <div class="map-button">
              <el-button @click="prevPolygon">⬅</el-button>
              <el-button @click="nextPolygon">➡</el-button>
              <el-button @click="quickEscapePolygons">reset display</el-button>

              <el-button
                v-if="!isDrawing"
                @click="beginDrawing"
                :disabled="disableSetPolygon"
                >Draw new polygon</el-button
              >
              <el-button
                v-if="isDrawing"
                @click="finishOneShape"
                :disabled="disableSetPolygon"
                >Finish one shape</el-button
              >
              <el-button
                v-if="isDrawing"
                @click="finishOnePolygon"
                :disabled="disableSetPolygon"
                >Finish one polygon</el-button
              >
              <el-button
                v-if="isDrawing"
                @click="endDrawing"
                :disabled="disableSetPolygon"
                >End drawing</el-button
              >
              <el-button
                v-if="isDrawing"
                @click="cancelDrawing"
                :disabled="disableSetPolygon"
                >Cancel drawing</el-button
              >
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-button">
            <el-button
              type="primary"
              @click="adminSubmit"
              v-if="userStore.user?.admin"
              >Submit</el-button
            >
            <el-button type="primary" v-else @click="userSubmit"
              >Submit</el-button
            >
            <el-button @click="reset">Reset</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  justify-content: center;
}

.map-container {
  width: 600px;
  height: 600px;
}

/* Extra small (phones) */
@media (max-width: 575px) {
  /* Styles for very small screens */
  .map-container {
    width: 375px;
    height: 375px;
  }
}
</style>
