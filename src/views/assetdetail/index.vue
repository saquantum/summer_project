<script setup lang="ts">
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, type ComponentPublicInstance } from 'vue'
import type { Asset, AssetWithWarnings } from '@/types/asset'
import type { Warning } from '@/types/warning'
import type { MultiPolygon } from 'geojson'
import type MapCard from '@/components/MapCard.vue'

interface WarningTableRow {
  id: number
  weatherType: string
  warningLevel: string
  warningLikelihood: string
  validFrom: string
  validTo: string
}

const route = useRoute()
const router = useRouter()
const assetStore = useAssetStore()
const userStore = useUserStore()

const id = route.params.id

const item = computed<AssetWithWarnings>(() => {
  const item =
    assetStore.userAssets?.find((item) => item.asset.id === id) ||
    assetStore.allAssets?.find((item) => item.asset.id === id)
  if (!item) throw new Error(`Can find asset ${id}`)
  else return item
})

const isEdit = ref(false)
const assetFormRef = ref()
const submit = () => {
  assetFormRef.value.submit()
  isEdit.value = false
}

const asset = computed<Asset>(() => item.value.asset)

const tableData = computed<Warning[]>(() => {
  return item.value?.warnings ?? []
})

const mapCardRef = ref<ComponentPublicInstance<typeof MapCard> | null>(null)

const mode = ref<'convex' | 'sequence'>('convex')

/**
 * The reason for using an array is to be compatible with warnings.
 */
const locations = computed({
  get: () => [asset.value.location],
  set: (val: MultiPolygon[]) => {
    asset.value.location = val[0]
  }
})

const beginDrawing = () => {
  mapCardRef.value?.beginDrawing()
}
const endDrawing = () => {
  mapCardRef.value?.endDrawing()
}
const finishOneShape = () => {
  mapCardRef.value?.finishOneShape()
}
const finishOnePolygon = () => {
  mapCardRef.value?.finishOnePolygon()
}
const cancelDrawing = () => {
  mapCardRef.value?.cancelDrawing()
}

const handleShowDetail = (row: WarningTableRow) => {
  router.push(`/warning/${row.id}`)
}

const handleDelete = (row: WarningTableRow) => {
  console.log('delete', row)
}
</script>

<template>
  <el-row :gutter="50" style="margin-bottom: 20px">
    <el-col :span="12">
      <el-card style="max-width: 600px">
        <template #header>
          <div class="card-header">
            <span>{{ asset?.name }}</span>
          </div>
        </template>
        <div style="height: 600px">
          <MapCard
            ref="mapCardRef"
            :map-id="'mapdetail'"
            v-model:locations="locations"
            :asset="asset"
            v-model:mode="mode"
          ></MapCard>
        </div>
      </el-card>
    </el-col>

    <el-col :span="12">
      <AssetForm
        ref="assetFormRef"
        v-model:isEdit="isEdit"
        :item="item"
      ></AssetForm>
      <el-button v-if="!isEdit" @click="isEdit = true">Edit</el-button>
      <el-button v-else @click="isEdit = false">Cancel</el-button>
      <el-button @click="submit">Submit</el-button>
    </el-col>
  </el-row>

  <div>
    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column prop="id" label="Warning ID" width="180" />
      <el-table-column prop="weatherType" label="Weather Type" width="180" />
      <el-table-column prop="warningLevel" label="Warning Level" width="180" />
      <el-table-column
        prop="warningImpact"
        label="Warning Impact"
        width="180"
      />
      <el-table-column
        prop="warningLikelihood"
        label="Warning Likelihood"
        width="180"
      />
      <el-table-column prop="validFrom" label="Warning Level" width="180" />
      <el-table-column prop="validTo" label="Warning Level" width="180" />
      <el-table-column label="Actions">
        <template #default="scope">
          <el-button
            text
            type="primary"
            size="small"
            @click="handleShowDetail(scope.row)"
            >Show Detail</el-button
          >
          <el-button
            text
            type="danger"
            size="small"
            @click="handleDelete(scope.row)"
            >Delete</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </div>

  <div
    v-if="
      userStore.user?.admin ||
      userStore.user?.permissionConfig.canUpdateAssetPolygon
    "
  >
    <h3>action</h3>
    <el-select v-model="mode">
      <el-option label="convex" value="convex"></el-option>
      <el-option label="sequence" value="sequence"></el-option>
    </el-select>
    <el-button @click="beginDrawing">Draw new asset</el-button>
    <el-button @click="finishOneShape">Finish one shape</el-button>
    <el-button @click="finishOnePolygon">Finish one polygon</el-button>
    <el-button @click="endDrawing">End drawing</el-button>
    <el-button @click="cancelDrawing">Cancel drawing</el-button>
  </div>
</template>

<style scoped>
.multiline-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
