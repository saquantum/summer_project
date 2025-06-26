<script setup>
import { useAssetStore } from '@/stores'
import { useRoute } from 'vue-router'
import { ref, computed } from 'vue'

const route = useRoute()
const assetStore = useAssetStore()

// get the asset
const id = route.params.id

const item = computed(() => {
  return (
    assetStore.userAssets.find((item) => item.asset.id === id) ||
    assetStore.allAssets.find((item) => item.asset.id === id)
  )
})

// 响应式绑定 asset 和 warnings
const asset = computed(() => item.value?.asset || {})
const tableData = computed(() => {
  if (item.value && item.value.warnings) return item.value.warnings
  else return []
})

const mapCardRef = ref()

const beginDrawing = () => {
  mapCardRef.value.beginDrawing()
}

const endDrawing = () => {
  mapCardRef.value.endDrawing()
}

const finishOneShape = () => {
  mapCardRef.value.finishOneShape()
}

const mode = ref('convex')

const location = computed({
  get: () => [asset.value.location]
})
</script>

<template>
  <el-row :gutter="50">
    <el-col :span="12">
      <el-card style="max-width: 480px">
        <template #header>
          <div class="card-header">
            <span>{{ asset.name }}</span>
          </div>
        </template>
        <div style="height: 300px">
          <MapCard
            ref="mapCardRef"
            :map-id="'mapdetail'"
            :location="location"
            :id="id"
            v-model:mode="mode"
          ></MapCard>
        </div>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-select v-model="mode">
        <el-option label="convex" value="convex"></el-option>
        <el-option label="sequence" value="sequence"></el-option>
      </el-select>
      <el-button @click="beginDrawing">Draw new asset</el-button>
      <el-button @click="finishOneShape">Finish one shape</el-button>
      <el-button @click="endDrawing">End drawing</el-button>
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
</template>
