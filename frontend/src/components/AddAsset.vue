<script setup>
import { ref, reactive, watch, computed } from 'vue'
import request from '@/utils/request'

function convertToGeoJSON(data, type = 'point') {
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

  if (type === 'polygon' && Array.isArray(data.boundingbox)) {
    const [south, north, west, east] = data.boundingbox.map(Number)
    const coordinates = [
      [
        [west, south],
        [east, south],
        [east, north],
        [west, north],
        [west, south] // close the polygon
      ]
    ]

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

const props = defineProps({
  visible: Boolean
})
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})
const emit = defineEmits(['update:visible'])

const activeStep = ref(0)
const form = reactive({
  assetName: '',
  assetType: '',
  address: '',
  drainArea: ''
})
const mapCardRef = ref()

const searchLocation = async (address) => {
  if (!address) return
  const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`
  const data = await request(url)
  if (data.length > 0) {
    form.drainArea = convertToGeoJSON(data[0], 'polygon')
    console.log(form.drainArea)
  }
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

const beginDrawing = () => {
  mapCardRef.value.beginDrawing()
}

const endDrawing = () => {
  mapCardRef.value.endDrawing()
}

watch(
  activeStep,
  async (newVal) => {
    if (newVal === 2) {
      if (form.address) {
        await searchLocation(form.address)
      }
    }
  },
  { immediate: false }
)

function nextStep() {
  activeStep.value++
}

function prevStep() {
  activeStep.value--
}

function submit() {
  emit('update:addAssetVisible', false)
  console.log('value:', form)
  ElMessage.success('Successfully add an asset')
}
</script>

<template>
  <el-dialog
    v-model="visible"
    title="Add new asset"
    width="700px"
    @close="$emit('update:visible', false)"
  >
    <el-steps :active="activeStep" finish-status="success" simple>
      <el-step title="Step 1" />
      <el-step title="Step 2" />
      <el-step title="Step 3 (Optional)" />
      <el-step title="Step 4" />
    </el-steps>

    <div class="step-content" style="margin-top: 20px">
      <div v-if="activeStep === 0">
        <el-form>
          <el-form-item label="Asset Name">
            <el-input v-model="form.assetName" />
          </el-form-item>
          <el-form-item label="Asset Type">
            <el-select
              v-model="form.assetType"
              placeholder="Please choose asset type"
            >
              <el-option
                v-for="item in typeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="activeStep === 1">
        <el-form>
          <el-form-item label="Address">
            <el-input
              v-model="form.address"
              type="textarea"
              placeholder="Please input address"
            />
          </el-form-item>
        </el-form>
      </div>

      <div v-else-if="activeStep === 2">
        <div><h3>draw polygon</h3></div>
        <!-- <div
        id="test"
        style="width: 100%; height: 400px; border: 1px solid #ddd"
      ></div> -->
        <div style="height: 300px">
          <MapCard
            v-if="form.drainArea"
            ref="mapCardRef"
            :map-id="'testid'"
            :drain-area="[form.drainArea]"
          ></MapCard>
        </div>
        <el-button @click="beginDrawing">Draw new asset</el-button>
        <el-button @click="endDrawing">End drawing</el-button>
      </div>

      <div v-else-if="activeStep === 3"></div>
    </div>

    <div style="margin-top: 20px">
      <el-button :disabled="activeStep === 0" @click="prevStep">Back</el-button>
      <el-button v-if="activeStep < 3" type="primary" @click="nextStep"
        >Next step</el-button
      >
      <el-button v-else @click="submit">Submit</el-button>
    </div>
  </el-dialog>
</template>
