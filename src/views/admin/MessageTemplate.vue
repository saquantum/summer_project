<script setup>
import { ref, onMounted, watch } from 'vue'

import { userTemplateStore } from '@/stores/modules/template'

const templateStore = userTemplateStore()
const warningTypeOption = [
  { label: 'Rain', value: 'Rain' },
  { label: 'Thunderstorms', value: 'Thunderstorms' },
  { label: 'Lightning', value: 'Lightning' },
  { label: 'Snow', value: 'Snow' },
  { label: 'Ice', value: 'Ice' },
  { label: 'Fog', value: 'Fog' },
  { label: 'Wind', value: 'Wind' },
  { label: 'Extreme Heat', value: 'Extreme Heat' }
]

const typeOptions = [
  { label: 'Water Tank', value: 'type_001' },
  { label: 'Soakaway', value: 'type_002' },
  { label: 'Green Roof', value: 'type_003' },
  { label: 'Permeable Pavement', value: 'type_004' },
  { label: 'Swale', value: 'type_005' },
  { label: 'Retention Pond', value: 'type_006' },
  { label: 'Rain Garden', value: 'type_007' }
]

const warningLevelOptions = [
  {
    value: 'Yellow',
    label: 'Yellow Warning'
  },
  {
    value: 'Amber',
    label: 'Amber Warning'
  },
  {
    value: 'Red',
    label: 'Red Warning'
  }
]

const warningType = ref('Rain')
const warningLevel = ref('Yellow')
const assetType = ref('type_001')

const isEdit = ref(false)
const templateText = ref(`You haven't set message for this.`)

const finish = () => {
  isEdit.value = false
}

const cancel = () => {
  isEdit.value = false
}

onMounted(async () => {
  await templateStore.getTemplates()
})

watch(
  [warningType, warningLevel, assetType],
  ([newWarningType, newWarningLevel, newAssetType]) => {
    console.log(newWarningType, newWarningLevel, newAssetType)
    const item = templateStore.templates.find(
      (item) =>
        item.warningType === newWarningType &&
        item.severity === newWarningLevel &&
        item.assetTypeId === newAssetType
    )
    console.log(item)
    templateText.value = item?.message || `You haven't set message for this.`
  },
  {
    immediate: true
  }
)
</script>

<template>
  <el-select v-model="warningType">
    <el-option
      v-for="item in warningTypeOption"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select v-model="assetType" placeholder="Please choose asset type">
    <el-option
      v-for="item in typeOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>

  <el-select v-model="warningLevel" placeholder="Select warning level">
    <el-option
      v-for="item in warningLevelOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <div class="edit-area">
    <el-input
      v-if="isEdit"
      v-model="templateText"
      style="width: 240px"
      :rows="2"
      type="textarea"
      placeholder="Please input"
    />
    <span v-else>{{ templateText }}</span>
  </div>

  <el-button @click="isEdit = true">Edit</el-button>

  <el-button v-if="isEdit" @click="finish">Finish</el-button>
  <el-button v-if="isEdit" @click="cancel">Cancel</el-button>
</template>

<style scoped>
.text-area {
  margin-top: 20px;
}
</style>
