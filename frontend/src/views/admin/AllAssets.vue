<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores'
const assets = ref([])
const router = useRouter()
const adminStore = useAdminStore()

const handleRowClick = (row) => {
  router.push(`/asset/${row.id}`)
}

onMounted(async () => {
  await adminStore.getAllAssets()
  assets.value = adminStore.allAssets.map((item) => {
    return {
      id: item.asset.id,
      assetName: item.asset.name,
      assetHolderId: item.asset.assetHolderId,
      warningLevel: item.warnings[0]?.warningLevel.toLowerCase() || 'None'
    }
  })
})
</script>

<template>
  <el-table
    @row-click="handleRowClick"
    :data="assets"
    stripe
    style="width: 100%"
  >
    <el-table-column prop="id" label="Asset id" width="180" />
    <el-table-column prop="assetName" label="Asset Name" width="180" />
    <el-table-column prop="assetHolderId" label="Asset Holder ID" width="180" />
    <el-table-column prop="warningLevel" label="Warning Level" />
  </el-table>
  <div></div>
</template>
