<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'

import { userTemplateStore } from '@/stores/modules/template'
import { adminGetTemplateByTypes } from '@/api/admin'

const templateStore = userTemplateStore()
const warningTypeOption = [
  { label: 'Rain', value: 'Rain' },
  { label: 'Thunderstorm', value: 'Thunderstorm' },
  { label: 'Lightning', value: 'Lightning' },
  { label: 'Snow', value: 'Snow' },
  { label: 'Ice', value: 'Ice' },
  { label: 'Fog', value: 'Fog' },
  { label: 'Wind', value: 'Wind' },
  { label: 'Extreme Heat', value: 'Heat' }
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

const contactChannelOptions = [
  {
    value: 'Email',
    label: 'Email'
  },
  {
    value: 'SMS',
    label: 'SMS'
  }
]

const warningType = ref('Rain')
const warningLevel = ref('YELLOW')
const assetType = ref('type_001')
const contactChannel = ref('Email')

const isEdit = ref(false)
const templateText = ref(`You haven't set message for this.`)
const allowedVariables = ['asset-model', 'contact_name', 'post_town']

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
  [warningType, warningLevel, assetType, contactChannel],
  async ([
    newWarningType,
    newWarningLevel,
    newAssetType,
    newContactChannel
  ]) => {
    const res = await adminGetTemplateByTypes(
      newAssetType,
      newWarningType,
      newWarningLevel,
      newContactChannel
    )
    console.log(res.data)

    templateText.value = res.data[0].body ?? `You haven't set message for this.`
  },
  {
    immediate: true
  }
)
</script>

<template>
  <div>Allowed variable:</div>
  <ul>
    <li v-for="item in allowedVariables" :key="item">{{ item }}</li>
  </ul>

  <el-select v-model="warningType">
    <el-option
      v-for="item in warningTypeOption"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    ></el-option>
  </el-select>

  <el-select v-model="contactChannel">
    <el-option
      v-for="item in contactChannelOptions"
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

  <TiptapEditor v-model:content="templateText"></TiptapEditor>

  <el-button @click="isEdit = true">Edit</el-button>

  <el-button v-if="isEdit" @click="finish">Finish</el-button>
  <el-button v-if="isEdit" @click="cancel">Cancel</el-button>
</template>

<style scoped>
.text-area {
  margin-top: 20px;
}
</style>
