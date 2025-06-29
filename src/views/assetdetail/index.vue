<script setup>
import { useAssetStore, useUserStore } from '@/stores'
import { useRoute, useRouter } from 'vue-router'
import { ref, computed } from 'vue'

const route = useRoute()
const router = useRouter()
const assetStore = useAssetStore()
const userStore = useUserStore()
// get the asset
const id = route.params.id

const item = computed(() => {
  return (
    assetStore.userAssets.find((item) => item.asset.id === id) ||
    assetStore.allAssets.find((item) => item.asset.id === id)
  )
})
console.log(item)
const asset = computed(() => item.value?.asset || {})
const tableData = computed(() => {
  if (item.value && item.value.warnings) return item.value.warnings
  else return []
})

const mapCardRef = ref()

const mode = ref('convex')

const locations = computed({
  get: () => [asset.value.location]
})

const displayData = [
  { label: 'ID', value: asset.value.id },
  { label: 'Name', value: asset.value.name },
  { label: 'Type', value: asset.value.type.name },
  { label: 'Capacity litres', value: asset.value.capacityLitres },
  {
    label: 'Material',
    value: asset.value.material
  },
  {
    label: 'status',
    value: asset.value.status
  },
  { label: 'Installed at', value: asset.value.installedAt },
  { label: 'Last inspection', value: asset.value.lastInspection }
]

const beginDrawing = () => {
  mapCardRef.value.beginDrawing()
}

const endDrawing = () => {
  mapCardRef.value.endDrawing()
}

const finishOneShape = () => {
  mapCardRef.value.finishOneShape()
}

const handleShowDetail = (row) => {
  router.push(`/warning/${row.id}`)
}
</script>

<template>
  <el-row :gutter="50" style="margin-bottom: 20px">
    <el-col :span="12">
      <el-card style="max-width: 600px">
        <template #header>
          <div class="card-header">
            <span>{{ asset.name }}</span>
          </div>
        </template>
        <div style="height: 600px">
          <MapCard
            ref="mapCardRef"
            :map-id="'mapdetail'"
            :locations="locations"
            :id="id"
            v-model:mode="mode"
          ></MapCard>
        </div>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-descriptions
        title="Asset Detail"
        :column="1"
        direction="vertical"
        border
      >
        <el-descriptions-item
          v-for="(item, index) in displayData"
          :key="index"
          :label="item.label"
          class-name="custom-item"
          label-class-name="custom-label"
        >
          <span :class="{ 'multiline-text': item.isMultiline }">{{
            item.value
          }}</span>
        </el-descriptions-item>
      </el-descriptions>
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

  <div v-if="userStore.user.admin">
    <h3>action</h3>
    <el-select v-model="mode">
      <el-option label="convex" value="convex"></el-option>
      <el-option label="sequence" value="sequence"></el-option>
    </el-select>
    <el-button @click="beginDrawing">Draw new asset</el-button>
    <el-button @click="finishOneShape">Finish one shape</el-button>
    <el-button @click="endDrawing">End drawing</el-button>
  </div>
</template>

<style scoped>
:deep(.custom-item) {
}

:deep(.custom-label) {
}
/* Your existing multiline text style */
.multiline-text {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
