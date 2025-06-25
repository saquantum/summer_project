<script setup>
import { useAssetStore } from '@/stores'
import { useRoute } from 'vue-router'
import { ref, computed } from 'vue'

const route = useRoute()
const assetStore = useAssetStore()
const isEdit = false
const contactOptions = [
  { value: 'Email', label: 'Email' },
  { value: 'SMS', label: 'SMS' },
  { value: 'Discord', label: 'Discord' },
  { value: 'WhatsApp', label: 'WhatsApp' },
  { value: 'Telegram', label: 'Telegram' }
]
const contact = ref('Email')

const asset = ref({})
// get the asset
const id = route.params.id

const item =
  assetStore.userAssets.find((item) => item.asset.id === id) ||
  assetStore.allAssets.find((item) => item.asset.id === id)

asset.value = item.asset
const mapCardRef = ref()

const beginDrawing = () => {
  mapCardRef.value.beginDrawing()
}

const endDrawing = () => {
  mapCardRef.value.endDrawing()
}

const mode = ref('convex')

const location = computed({
  get: () => [item.asset.location]
})
</script>

<template>
  <el-row>
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
        <template #footer>Footer content</template>
      </el-card>
      <el-select v-model="mode">
        <el-option label="convex" value="convex"></el-option>
        <el-option label="sequence" value="sequence"></el-option>
      </el-select>
      <el-button @click="beginDrawing">Draw new asset</el-button>
      <el-button @click="endDrawing">End drawing</el-button>
    </el-col>

    <el-col :span="12">
      <!-- info form -->
      <el-form>
        <el-form-item>
          <el-select
            v-model="contact"
            placeholder="Select warning level"
            size="large"
            style="width: 240px"
            @click="isEdit = true"
          >
            <el-option
              v-for="item in contactOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              @click="isEdit = true"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-input
            v-model="input"
            style="width: 240px"
            placeholder="Please input"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="input"
            style="width: 240px"
            placeholder="Please input"
          />
        </el-form-item>
        <el-form-item>
          <el-button v-if="isEdit">Save</el-button>
        </el-form-item>
        <el-form-item>
          <el-button>Subscribe</el-button>
        </el-form-item>
      </el-form>
      <el-button type="danger">Rest All</el-button>
    </el-col>
  </el-row>

  <div>
    <el-table
      :data="tableData"
      style="width: 100%"
      :row-class-name="tableRowClassName"
    >
      <el-table-column prop="WarningID" label="Warning ID" width="180" />
      <el-table-column prop="name" label="Warning Type" width="180" />
      <el-table-column prop="address" label="Warning Impact" width="180" />
      <el-table-column prop="name" label="Warning Likelihood" width="180" />
      <el-table-column prop="name" label="Valid From" width="180" />
      <el-table-column prop="name" label="Valid To" width="180" />
    </el-table>
  </div>
</template>
