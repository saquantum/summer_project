<script setup>
import { ref, onMounted } from 'vue'
import { adminGetUsersService } from '@/api/admin'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'

const router = useRouter()
const users = ref([])
const userStore = useUserStore()
const handleShowDetail = async (row) => {
  userStore.setProxyId(row.assetHolderId)

  // goto user interface
  router.push('/myassets/manage')
}
onMounted(async () => {
  const res = await adminGetUsersService()
  console.log(res)
  users.value = res.data.map((item) => ({
    uid: item.id,
    username: item.id,
    assetHolderId: item.assetHolderId || 'none',
    assets: item.id
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
    <el-table-column prop="username" label="Username" width="180" />
    <el-table-column prop="assets" label="Assets" width="180" />
    <el-table-column label="Actions">
      <template #default="scope">
        <el-button
          :disabled="scope.row.uid === 'admin'"
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
</template>
