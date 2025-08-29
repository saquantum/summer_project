<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import { userInsertAssetService } from '@/api/user'
import { adminInsertAssetService } from '@/api/admin'
import type { Feature, MultiPolygon } from 'geojson'
import { ElMessage } from 'element-plus'
import type { ComponentPublicInstance } from 'vue'
import type MapCard from '@/components/MapCard.vue'
import type { AssetForm, NominatimResult } from '@/types'
import { createUserRules, trimForm } from '@/utils/formUtils'
import PageTopTabs from '@/components/PageSurfaceTabs.vue'
import type { RouteLocationNormalized } from 'vue-router'

// user store
const userStore = useUserStore()
const assetStore = useAssetStore()
const adminTabs = [
  {
    label: 'All Assets',
    to: { name: 'AdminAllAssets' },
    match: (r: RouteLocationNormalized) => r.name === 'AdminAllAssets'
  },
  { label: 'Add Assets', to: { name: 'AdminAddAsset' } },
  {
    label: 'Asset Types',
    to: { name: 'AdminAssetTypes' },
    match: (r: RouteLocationNormalized) =>
      r.path?.startsWith('/admin/assets/types')
  }
]
const isAdmin = computed(() => !!userStore.user?.admin)

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
    !userStore.user?.accessControlGroup.canCreateAsset
  )
    return true
  return false
})

const disableSetPolygon = computed(() => {
  if (
    !userStore.user?.admin &&
    !userStore.user?.accessControlGroup.canSetPolygonOnCreate
  )
    return true
  return false
})

const form = ref<AssetForm>({
  username: '',
  name: '',
  typeId: '',
  ownerId: '',
  address: '',
  locations: [
    {
      type: 'MultiPolygon',
      coordinates: []
    }
  ],
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
  username: createUserRules(userStore.user?.admin ?? false),
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

const clearAll = () => {
  mapCardRef.value?.clearAll()
}

const clearCurrentPolygon = () => {
  mapCardRef.value?.clearCurrentPolygon()
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
  if (userStore.user) {
    form.value.ownerId = userStore.user.id
  }
  form.value.capacityLitres = Number(form.value.capacityLitres)

  try {
    if (userStore.user) {
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

  form.value.ownerId = form.value.username

  form.value.capacityLitres = Number(form.value.capacityLitres)

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
    locations: [
      {
        type: 'MultiPolygon',
        coordinates: []
      }
    ],
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

onMounted(() => {
  if (!userStore.user?.admin && userStore.user?.id) {
    form.value.username = userStore.user?.id
    disableForUser.value = true
  }
})
</script>

<template>
  <div class="page-surface">
    <PageTopTabs v-if="isAdmin" :tabs="adminTabs" />

    <div class="container">
      <h3 v-if="disableAddAsset">You can not add asset right now</h3>
      <el-form
        class="asset-form"
        :model="form"
        ref="formRef"
        label-width="auto"
        label-position="left"
        :rules="rules"
        :disabled="disableAddAsset"
      >
        <el-row :gutter="16" class="content-grid">
          <el-col
            v-if="form.locations.length"
            :xs="24"
            :sm="24"
            :md="12"
            :lg="12"
          >
            <el-card class="card-elevated map-card">
              <template #header>
                <div class="card-header">
                  <span>Map</span>
                  <span class="hint" v-if="!form.locations.length"
                    >Search an address to preview polygon</span
                  >
                </div>
              </template>

              <div class="map-container">
                <MapCard
                  ref="mapCardRef"
                  :map-id="'AddAsset'"
                  v-model:locations="form.locations"
                  v-model:mode="mode"
                />
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="24" :md="12" :lg="12" class="right-col">
            <div class="right-stack">
              <el-card class="card-elevated draw-card">
                <template #header>
                  <div class="card-header">
                    <span>Drawing tools</span>
                    <HelpTip
                      title="How to draw an asset"
                      content="An asset is a multipolygon, a multipolygon is consist of different polygons, while a polygon is consist of diffenrt rings. As a reusult, in order to draw a multipolygon, you need to draw a ring and then click the 'finish one shape' button, once a polygon is finish, you need to click finish one polygon button"
                    ></HelpTip>
                    <span class="hint" v-if="isDrawing"
                      >You are now drawing a polygon</span
                    >
                  </div>
                </template>

                <div class="admin-controls">
                  <div class="control-row">
                    <label>Mode</label>
                    <el-select
                      :disabled="isDrawing || disableSetPolygon"
                      v-model="mode"
                      class="mode-select"
                    >
                      <el-option label="convex" value="convex"></el-option>
                      <el-option label="sequence" value="sequence"></el-option>
                    </el-select>

                    <div class="arrow-group">
                      <el-button
                        @click="prevPolygon"
                        :disabled="mapCardRef?.disablePrev"
                        class="arrow"
                        >⬅</el-button
                      >
                      <el-button
                        @click="nextPolygon"
                        :disabled="mapCardRef?.disableNext"
                        class="arrow"
                        >➡</el-button
                      >
                    </div>
                  </div>
                  <div class="toolbar">
                    <div class="toolbar-group" :class="{ drawing: isDrawing }">
                      <el-button
                        @click="quickEscapePolygons"
                        class="styled-btn reset-btn"
                        >reset display</el-button
                      >
                      <el-button
                        v-if="!isDrawing"
                        @click="beginDrawing"
                        class="styled-btn"
                        :disabled="disableSetPolygon"
                        >Draw new polygon</el-button
                      >
                    </div>
                    <div class="else-btn">
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
                        @click="clearCurrentPolygon"
                        :disabled="disableSetPolygon"
                        >Clear current polygon</el-button
                      >
                      <el-button
                        v-if="isDrawing"
                        @click="clearAll"
                        :disabled="disableSetPolygon"
                        >Clear all</el-button
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
                </div>
              </el-card>

              <el-card class="card-elevated form-card">
                <template #header>
                  <div class="card-header"><span>Asset info</span></div>
                </template>

                <el-row :gutter="16" class="asset-grid">
                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Username" prop="username">
                      <el-input
                        v-model="form.username"
                        :disabled="disableForUser"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Asset name" prop="name">
                      <el-input v-model="form.name" />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Asset type" prop="typeId">
                      <el-select
                        v-model="form.typeId"
                        placeholder="Select type"
                      >
                        <el-option
                          v-for="item in assetStore.typeOptions"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Asset material" prop="material">
                      <el-select
                        v-model="form.material"
                        placeholder="Select material"
                      >
                        <el-option
                          v-for="item in materialOption"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Asset status" prop="status">
                      <el-select
                        v-model="form.status"
                        placeholder="Select status"
                      >
                        <el-option
                          v-for="item in statusOption"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Capacity litres" prop="capacityLitres">
                      <el-input-number v-model="form.capacityLitres" :min="0" />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Installed at" prop="installedAt">
                      <el-date-picker
                        v-model="form.installedAt"
                        type="date"
                        placeholder="Pick a day"
                        :disabled-date="disabledAfterToday"
                        value-format="YYYY-MM-DD"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="12">
                    <el-form-item label="Last inspection" prop="lastInspection">
                      <el-date-picker
                        v-model="form.lastInspection"
                        type="date"
                        placeholder="Pick a day"
                        :disabled-date="disabledAfterToday"
                        value-format="YYYY-MM-DD"
                      />
                    </el-form-item>
                  </el-col>

                  <el-col :xs="24" :sm="24">
                    <el-form-item label="Address">
                      <el-input
                        v-model="form.address"
                        type="textarea"
                        placeholder="Please input address"
                      /><el-button @click="searchLocation(form.address)"
                        >Search</el-button
                      >
                    </el-form-item>
                  </el-col>

                  <el-col :span="24">
                    <div class="form-button">
                      <el-button
                        type="primary"
                        @click="adminSubmit"
                        v-if="userStore.user?.admin"
                        class="styled-btn"
                        >Submit</el-button
                      >
                      <el-button
                        type="primary"
                        v-else
                        @click="userSubmit"
                        class="styled-btn"
                        >Submit</el-button
                      >
                      <el-button @click="reset" class="styled-btn btn-cancel"
                        >Reset</el-button
                      >
                    </div>
                  </el-col>
                </el-row>
              </el-card>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /*width: 1100px;*/
  width: min(80vw, 1600px);
  max-width: calc(100% - 2rem);
  box-sizing: border-box;
}

.card-elevated {
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
  background: #fff;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  min-height: 40px;
  font-weight: 600;
  color: #1f2d3d;
}
.card-header .hint {
  margin-left: auto;
  font-size: 12px;
  color: #94a3b8;
}

.map-card {
  height: calc(var(--panel-h) + var(--card-header-h) + var(--card-body-p) * 2);
}
.form-card,
.admin-card {
  height: 100%;
}

.map-card :deep(.el-card__body),
.form-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;

  width: 100%;
}

.map-container {
  width: 100%;
  height: 600px;
}

.right-col {
  margin-top: var(--gap);
}
.right-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  height: calc(var(--panel-h) + var(--card-header-h) + var(--card-body-p) * 2);
}

.form-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  padding: var(--card-body-p) 16px;
}
.form-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.tool-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tool-buttons.drawing {
  gap: 6px;
}

.admin-controls {
  display: grid;
  gap: 14px;
  margin-bottom: 15px;
}

.control-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  align-items: center;
  column-gap: 8px;
}

.control-row label {
  min-width: 52px;
  font-weight: 500;
  color: var(--muted);
}

.mode-select {
  width: 100%;
  min-width: 300px;
}
.toolbar {
  display: grid;
  gap: 10px;
}
.toolbar-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.arrow {
  margin-left: 30px;
  width: 70px;
}
.table-container {
  overflow-x: auto;
}
.responsive-table {
  width: 100%;
  min-width: 720px;
}
.action-cell {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-grid .el-col > .el-card + .el-card {
  margin-top: var(--gap);
}

.form-card .card-header.card-header-actions {
  justify-content: space-between;
}

.form-card .header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 8px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  border: 1px solid #fff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel:hover {
  background-image: linear-gradient(
    to bottom,
    #782d2d 0%,
    #852e2e 10%,
    #903737 100%
  );
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.reset-btn {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.37);
  border: 1px solid #fff;
}

.reset-btn:hover {
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.equal-table :deep(.el-table__cell) {
  border-right: 0 !important;
}

.equal-table :deep(.el-table__header th.el-table__cell) {
  background-color: #f5f7fb;
  font-weight: 600;
}

.equal-table :deep(.el-table__body td.el-table__cell) {
  background-color: #fbfcff;
}

.equal-table
  :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #f2f6ff;
}

.equal-table :deep(.el-table__body tr:hover > td) {
  background-color: #eaf3ff;
}

.asset-form .form-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 8px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeef3;
}

.asset-grid {
  margin-top: 6px;
  padding: 20px;
}

.asset-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.asset-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
}

.asset-form :deep(.el-input__wrapper),
.asset-form :deep(.el-select__wrapper),
.asset-form :deep(.el-textarea__inner),
.asset-form :deep(.el-date-editor.el-input__wrapper),
.asset-form :deep(.el-input-number) {
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  box-shadow: none;
}
.asset-form :deep(.el-input__wrapper:hover),
.asset-form :deep(.el-select__wrapper:hover),
.asset-form :deep(.el-textarea__inner:hover),
.asset-form :deep(.el-date-editor.el-input__wrapper:hover),
.asset-form :deep(.el-input-number:hover) {
  border-color: #cbd5e1;
}
.asset-form :deep(.is-focus),
.asset-form :deep(.el-textarea__inner:focus) {
  border-color: #4c7dd1 !important;
  box-shadow: 0 0 0 3px rgba(76, 125, 209, 0.18) !important;
  background: #fff !important;
}

.asset-form :deep(.el-form-item.is-required > .el-form-item__label:before) {
  color: #ef4444;
}

.address-item :deep(.el-form-item__content) {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: start;
}
.search-btn {
  height: 36px;
  padding: 0 16px;
  margin-top: 20px;
}

.form-button {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px dashed #e5e7eb;
  margin-top: 6px;
}

@media (max-width: 575px) {
  .page-surface {
    padding: 12px;
    margin: 24px auto;
  }

  .content-grid {
    row-gap: 12px;
  }

  .map-card :deep(.el-card__body) {
    padding: 10px;
  }

  .form-card :deep(.el-card__body) {
    padding: 10px !important;
  }

  .asset-form :deep(.el-form-item__label) {
    float: none;
    display: block;
    width: auto !important;
    text-align: left;
    padding: 0 0 6px 0;
    line-height: 1.2;
  }
  .asset-form :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }

  .asset-form :deep(.el-input__wrapper),
  .asset-form :deep(.el-select__wrapper),
  .asset-form :deep(.el-textarea__inner),
  .asset-form :deep(.el-date-editor.el-input__wrapper),
  .asset-form :deep(.el-input-number) {
    width: 100%;
  }

  .asset-grid {
    padding: 10px !important;
    margin-top: 0;
  }
  .asset-form :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  .address-item :deep(.el-form-item__content) {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .search-btn {
    width: 100%;
    height: 36px;
    margin-top: 0;
  }

  .toolbar {
    gap: 8px;
  }
  .toolbar-group {
    gap: 8px;
  }
  .arrow {
    margin-left: auto;
    width: 48px;
    padding: 0;
  }

  .form-button {
    justify-content: stretch;
    flex-direction: column;
    gap: 8px;
    padding-top: 6px;
    margin-left: 0;
  }
  .form-button .styled-btn {
    width: 100%;
  }

  .asset-form :deep(.el-form-item__label) {
    font-weight: 600;
    font-size: 14px;
  }

  .control-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .mode-select {
    min-width: 120px;
    max-width: 200px;
  }
  .arrow {
    width: 44px;
    height: 36px;
    padding: 0;
  }

  .arrow-group {
    grid-template-columns: 1fr 1fr;
    gap: 6px;
  }

  .toolbar {
    display: grid;
    gap: 10px;
  }
  .toolbar-group,
  .else-btn {
    display: grid;
    grid-template-columns: 1fr;
    gap: 10px;
    justify-items: center;
  }

  .toolbar-group .el-button,
  .else-btn .el-button {
    width: auto;
    min-width: 180px;
    max-width: 90%;
    height: 40px;
    white-space: nowrap;
  }
  .styled-btn {
    margin: 0;
  }
}
</style>
