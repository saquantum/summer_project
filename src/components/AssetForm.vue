<script setup lang="ts">
import { ref, onMounted, computed, watchEffect } from 'vue'
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import { updateAssetByIdService } from '@/api/assets'

import type { AssetInfoForm, AssetWithWarnings } from '@/types/asset'

import {
  adminGetUserInfoByAIDService,
  adminGetUserInfoService,
  adminUpdateAssetService
} from '@/api/admin'
import { createAssetHolderRules } from '@/utils/formUtils'
import { ElMessage } from 'element-plus'

const props = defineProps<{ isEdit: boolean; item: AssetWithWarnings }>()

const assetStore = useAssetStore()
const userStore = useUserStore()

const uid = ref('')

const fetchUserId = async () => {
  if (userStore.user && !userStore.user.admin) {
    uid.value = userStore.user.id
  } else {
    try {
      const res = await adminGetUserInfoByAIDService(props.item.asset.ownerId)
      uid.value = res.data.id
    } catch (error) {
      console.error('Failed to fetch user info:', error)
    }
  }
}

const form = ref<AssetInfoForm>({
  id: '',
  name: '',
  typeId: '',
  capacityLitres: 0,
  material: '',
  status: '',
  installedAt: '',
  lastInspection: '',

  // only for admin
  ownerId: ''
})
const formRef = ref()

const rules = {
  ownerId: createAssetHolderRules(userStore.user?.admin ?? false),
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

const descriptionsItem = computed(() => {
  if (!props.item) return []

  return [
    { label: 'OwnerId', value: uid.value },
    { label: 'Name', value: props.item.asset.name },
    { label: 'Type', value: props.item.asset.type.name },
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
    lastInspection: item.asset.lastInspection ?? '',
    ownerId: item.asset.ownerId ?? ''
  }
}

const disabledAfterToday = (time: Date) => {
  return time.getTime() > Date.now()
}

const submit = async () => {
  try {
    if (userStore.user && userStore.user.admin) {
      const ownerId = form.value.ownerId
      if (ownerId) {
        const res = await adminGetUserInfoService(ownerId)
        form.value.ownerId = res.data.assetHolderId as string
      }
      await adminUpdateAssetService(form.value)
    } else if (userStore.user) {
      await updateAssetByIdService(userStore.user.id, form.value)
    }
    assetStore.updateAssetById(form.value.id)
    ElMessage.success('Asset updated')
  } catch (e) {
    console.error(e)
    ElMessage.error('Fail to update asset')
  }
}

onMounted(async () => {
  await fetchUserId()
  form.value = assetToForm(props.item)
  if (uid.value) {
    form.value.ownerId = uid.value
  }
})

// Watch for changes in user store or props and refetch user ID
watchEffect(() => {
  if (userStore.user || props.item?.asset?.ownerId) {
    fetchUserId()
  }
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
    <el-form-item label="Owner Id" prop="ownerId">
      <el-input
        v-model="form.ownerId"
        :disabled="userStore.user && !userStore.user.admin"
      />
    </el-form-item>

    <el-form-item label="Name" prop="name">
      <el-input v-model="form.name" />
    </el-form-item>

    <el-form-item label="Asset material" prop="material">
      <el-select v-model="form.material" placeholder="Select material">
        <el-option
          v-for="item in materialOption"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
    </el-form-item>

    <el-form-item label="Asset status" prop="status">
      <el-select v-model="form.status" placeholder="Select status">
        <el-option
          v-for="item in statusOption"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        ></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Capacity Litres">
      <el-input-number v-model="form.capacityLitres" :min="0" />
    </el-form-item>

    <el-form-item label="Installed at" prop="installedAt">
      <el-date-picker
        v-model="form.installedAt"
        type="date"
        placeholder="Pick a day"
        :disabled-date="disabledAfterToday"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>

    <el-form-item label="Last inspection" prop="lastInspection">
      <el-date-picker
        v-model="form.lastInspection"
        type="date"
        placeholder="Pick a day"
        :disabled-date="disabledAfterToday"
        value-format="YYYY-MM-DD"
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
