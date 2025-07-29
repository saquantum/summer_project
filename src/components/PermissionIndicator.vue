<script setup lang="ts">
import { computed } from 'vue'

type PermissionField =
  | 'canCreateAsset'
  | 'canSetPolygonOnCreate'
  | 'canUpdateAssetFields'
  | 'canUpdateAssetPolygon'
  | 'canDeleteAsset'
  | 'canUpdateProfile'

const props = defineProps<{
  status: boolean
  field: PermissionField
}>()

const permissionDescriptions: Record<PermissionField, string> = {
  canCreateAsset: 'create assets',
  canSetPolygonOnCreate: 'set polygon on creation',
  canUpdateAssetFields: 'update asset fields',
  canUpdateAssetPolygon: 'update asset polygon',
  canDeleteAsset: 'delete assets',
  canUpdateProfile: 'update profile'
}

const statusClass = computed(() => {
  return `indicator--${props.status}`
})
</script>

<template>
  <el-tooltip
    effect="dark"
    :content="permissionDescriptions[field]"
    placement="bottom"
  >
    <div class="indicator" :class="statusClass"></div>
  </el-tooltip>
</template>

<style scoped>
.indicator {
  background-color: rgb(48, 164, 108);
  border-radius: 9999px;
  width: 8px;
  height: 8px;
  box-sizing: border-box;
}

.indicator--true {
  background-color: rgb(48, 164, 108);
}

.indicator--false {
  background-color: rgb(229, 72, 77);
}
</style>
