<script setup>
import { ref, onMounted } from 'vue'
import { adminGetUsersService } from '@/api/admin'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/modules/admin'

const router = useRouter()
const users = ref([])
const adminStore = useAdminStore()
const handleRowClick = async (row) => {
  console.log(row.uid)
  console.log(`triggered`)
  // const res = await adminProxyUserService(row.uid)
  adminStore.setProxyId(row.assetHolderId)

  // goto user interface
  router.push('/myassets/manage')
}
onMounted(async () => {
  const res = await adminGetUsersService()
  users.value = res.data.data.map((item) => ({
    uid: item.user.id,
    username: item.user.username,
    assetHolderId: item.user.assetHolderId,
    assets: item.assetCount
  }))
})
</script>

<template>
  <el-table
    @row-click="handleRowClick"
    :data="users"
    stripe
    style="width: 100%"
  >
    <el-table-column prop="uid" label="UID" width="180" />
    <el-table-column prop="assetHolderId" label="Asset Holder ID" width="180" />
    <el-table-column prop="username" label="Username" />
    <el-table-column prop="assets" label="Assets" />
  </el-table>
</template>
