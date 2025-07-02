<script setup>
import { ref, onMounted, watch } from 'vue'
import { adminGetNotificationService } from '@/api/admin'
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
    value: 'No',
    label: 'No Warning'
  },
  {
    value: 'YELLOW',
    label: 'Yellow Warning'
  },
  {
    value: 'AMBER',
    label: 'Amber Warning'
  },
  {
    value: 'RED',
    label: 'Red Warning'
  }
]

const warningType = ref()
const warningLevel = ref()
const assetType = ref()

const isEdit = ref(false)
const templateText = ref('Maintenance activity scheduled soon.')

const finish = () => {
  isEdit.value = false
}

const cancel = () => {
  isEdit.value = false
}

onMounted(async () => {
  await adminGetNotificationService()
})

watch([warningType, warningLevel, assetType], () => {
  templateText.value = 'aaa'
})
</script>

<template>
  <el-select>
    <el-option
      v-for="item in warningTypeOption"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select placeholder="Please choose asset type">
    <el-option
      v-for="item in typeOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>

  <el-select v-model="assetWarningLevel" placeholder="Select warning level">
    <el-option
      v-for="item in warningLevelOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <div>
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
