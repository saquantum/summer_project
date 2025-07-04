<script setup>
import { ref, watch, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores'
import CodeUtil from '@/utils/codeUtil'
import { userCheckUIDService } from '@/api/user'
import { adminGetUserInfoByUIDService } from '@/api/admin'
import { userInsertAssetService } from '../../api/user'
import { adminInsertAssetService } from '../../api/admin'

const userStore = useUserStore()

const convertToGeoJSON = (data, type = 'point') => {
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

  if (
    type === 'polygon' ||
    (type === 'multipolygon' && Array.isArray(data.boundingbox))
  ) {
    const [south, north, west, east] = data.boundingbox.map(Number)
    const polygon = [
      [west, south],
      [east, south],
      [east, north],
      [west, north],
      [west, south] // close the polygon
    ]

    const coordinates = type === 'multipolygon' ? [[polygon]] : [polygon]

    return {
      type: 'Feature',
      geometry: {
        type: type === 'multipolygon' ? 'MultiPolygon' : 'Polygon',
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

const form = ref({
  username: '',
  name: '',
  typeId: '',
  ownerId: '',
  address: '',
  // london by default
  locations: [
    {
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
  ],
  capacityLitres: '',
  material: '',
  status: '',
  installedAt: '',
  lastInspection: '',
  location: ''
})

const formRef = ref()

const rules = {
  username: [
    { required: true, message: 'Please input username', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        const res = await userCheckUIDService(value)
        // success means find a username called ${value}
        if (CodeUtil.isSuccess(res.code)) {
          if (userStore.user.admin) {
            const res = await adminGetUserInfoByUIDService(value)
            if (res.data.admin) {
              callback(new Error('Can not add asset to admin'))
            }
          }
          callback()
        } else {
          callback(new Error(`Username ${value} does not exists.`))
        }
      },
      trigger: 'blur'
    }
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
const mapCardRef = ref()
const tipVisible = ref(false)

const searchLocation = async (address) => {
  if (!address) return
  const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`
  const data = await request(url)
  if (data.length > 0) {
    const geometry = convertToGeoJSON(data[0], 'multipolygon')
    form.value.locations = []
    form.value.locations.push(geometry.geometry)
  }
  console.log(form.value.locations)
}

const typeOptions = [
  { label: 'Water Tank', value: 'type_001' },
  { label: 'Soakaway', value: 'type_002' },
  { label: 'Green Roof', value: 'type_003' },
  { label: 'Permeable Pavement', value: 'type_004' },
  { label: 'Swale', value: 'type_005' },
  { label: 'Retention Pond', value: 'type_006' },
  { label: 'Rain Garden', value: 'type_007' }
]

const disabledAfterToday = (time) => {
  return time.getTime() > Date.now()
}
const beginDrawing = () => {
  tipVisible.value = true
  mapCardRef.value.beginDrawing()
}

const finishOneShape = () => {
  mapCardRef.value.finishOneShape()
}

const endDrawing = () => {
  tipVisible.value = false
  mapCardRef.value.endDrawing()
}

const cancelDrawing = () => {
  tipVisible.value = false
  mapCardRef.value.cancelDrawing()
}

const userSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  form.value.location = form.value.locations[0]
  form.value.ownerId = userStore.user.assetHolderId

  try {
    await userInsertAssetService(userStore.user.assetHolderId, form.value)
    ElMessage.success('Successfully add an asset')
  } catch {
    ElMessage.error('An error occurs during adding an asset')
  }
}

const adminSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  form.value.location = form.value.locations[0]

  const res = await adminGetUserInfoByUIDService(form.value.username)
  form.value.ownerId = res.data.assetHolderId

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
    // london by default
    locations: [
      {
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
    ],
    capacityLitres: '',
    material: '',
    status: '',
    installedAt: '',
    lastInspection: '',
    location: ''
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
  if (!userStore.user.admin) {
    form.value.username = userStore.user.id
    disableForUser.value = true
  }
})
</script>

<template>
  <div class="container">
    <div>
      <el-form
        :model="form"
        ref="formRef"
        label-width="auto"
        label-position="left"
        :rules="rules"
        style="max-width: 600px"
        hide-required-asterisk
      >
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" :disabled="disableForUser" />
        </el-form-item>
        <el-form-item label="Asset name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Asset type">
          <el-select v-model="form.typeId" placeholder="Select type">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Asset material">
          <el-select v-model="form.material" placeholder="Select material">
            <el-option
              v-for="item in materialOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Asset status">
          <el-select v-model="form.status" placeholder="Select status">
            <el-option
              v-for="item in statusOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Capacity litres">
          <el-input v-model="form.capacityLitres" type="number"></el-input>
        </el-form-item>

        <el-form-item label="Installed at">
          <el-date-picker
            v-model="form.installedAt"
            type="date"
            placeholder="Pick a day"
            :disabled-date="disabledAfterToday"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="Last inspection">
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
      </el-form>

      <div v-if="form.locations.length > 0">
        <div><h3>Customise polygon</h3></div>
        <div v-if="tipVisible">Your are now drawing new polygon</div>
        <div style="height: 400px; max-width: 600px; border: black 1px solid">
          <MapCard
            ref="mapCardRef"
            :map-id="'AddAsset'"
            v-model:locations="form.locations"
            v-model:mode="mode"
          ></MapCard>
        </div>
        <el-select
          :disabled="tipVisible"
          v-model="mode"
          style="margin-top: 10px"
        >
          <el-option label="convex" value="convex"></el-option>
          <el-option label="sequence" value="sequence"></el-option>
        </el-select>
        <div class="map-button">
          <el-button @click="beginDrawing">Draw new asset</el-button>
          <el-button @click="finishOneShape">Finish one shape</el-button>
          <el-button @click="endDrawing">End drawing</el-button>
          <el-button @click="cancelDrawing">Cancel drawing</el-button>
        </div>

        <div class="form-button">
          <el-button
            type="primary"
            @click="adminSubmit"
            v-if="userStore.user.admin"
            >Submit</el-button
          >
          <el-button type="primary" v-else @click="userSubmit"
            >Submit</el-button
          >
          <el-button @click="reset">Reset</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.map-button {
  margin-top: 10px;
}

.form-button {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
