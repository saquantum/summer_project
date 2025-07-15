<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import { updateAssetByIdService } from '@/api/assets'

import type { AssetInfoForm, AssetWithWarnings } from '@/types/asset'

import { adminUpdateAssetService } from '@/api/admin'

const props = defineProps<{ isEdit: boolean; item: AssetWithWarnings }>()

const assetStore = useAssetStore()
const userStore = useUserStore()

const form = ref<AssetInfoForm>({
  id: '',
  name: '',
  typeId: '',
  capacityLitres: 0,
  material: '',
  status: '',
  installedAt: '',
  lastInspection: ''
})
const formRef = ref()

const rules = ref()

const descriptionsItem = computed(() => {
  if (!props.item) return []
  return [
    { label: 'Name', value: props.item.asset.name },
    { label: 'TypeID', value: props.item.asset.type.name },
    { label: 'Capacity litres', value: props.item.asset.capacityLitres },
    { label: 'Material', value: props.item.asset.material },
    { label: 'Status', value: props.item.asset.status },
    { label: 'Installed at', value: props.item.asset.installedAt },
    { label: 'Last inspection', value: props.item.asset.lastInspection }
  ]
})

const assetToForm = (item: AssetWithWarnings): AssetInfoForm => {
  return {
    id: item.asset.id,
    name: item.asset.name ?? '',
    typeId: item.asset.typeId ?? '',
    capacityLitres: item.asset.capacityLitres ?? '',
    material: item.asset.material ?? '',
    status: item.asset.status ?? '',
    installedAt: item.asset.installedAt ?? '',
    lastInspection: item.asset.lastInspection ?? ''
  }
}

const submit = async () => {
  if (userStore.user?.admin) {
    await adminUpdateAssetService(form.value)
  } else {
    if (userStore.user?.id) {
      await updateAssetByIdService(userStore.user.id, form.value)
    }
  }
  assetStore.updateAssetById(form.value.id)
}

onMounted(async () => {
  form.value = assetToForm(props.item)
})

defineExpose({
  submit
})
</script>

<template>
  <!-- display -->
  <el-descriptions v-if="!isEdit" :column="1" border class="asset-descriptions">
    <el-descriptions-item
      v-for="(item, index) in descriptionsItem"
      :key="index"
      :label="item.label"
    >
      {{ item.value }}
    </el-descriptions-item>
  </el-descriptions>

  <!-- edit -->
  <el-form
    v-else
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    class="asset-form"
  >
    <el-form-item label="Name" prop="name">
      <el-input v-model="form.name" />
    </el-form-item>

    <el-form-item label="Material" prop="material">
      <el-input v-model="form.material" />
    </el-form-item>

    <el-form-item label="Capacity Litres">
      <el-input-number v-model="form.capacityLitres" :min="0" />
    </el-form-item>

    <el-form-item label="Status">
      <el-input v-model="form.status" />
    </el-form-item>

    <el-form-item label="Installed At">
      <el-date-picker
        v-model="form.installedAt"
        type="datetime"
        placeholder="Select date and time"
      />
    </el-form-item>

    <el-form-item label="Last Inspection">
      <el-date-picker
        v-model="form.lastInspection"
        type="datetime"
        placeholder="Select date and time"
      />
    </el-form-item>
  </el-form>
</template>

<style scoped>
.asset-descriptions {
  margin-bottom: 20px;
}

.asset-form {
  max-width: 600px;
}
</style>
