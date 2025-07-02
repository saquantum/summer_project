<script setup>
import { ref, onMounted } from 'vue'
import { adminGetUsersService } from '@/api/admin'
import { useRouter } from 'vue-router'

const router = useRouter()
const users = ref([])

const handleEdit = async (row) => {
  router.push({ path: '/admin/user/detail', query: { id: row.assetHolderId } })
}

const handleSortChange = ({ prop, order }) => {
  console.log(prop, order)
}

// const sortParams = ref({
//   prop: '',
//   order: ''
// })
onMounted(async () => {
  const res = await adminGetUsersService('count')
  console.log(res)
  users.value = res.data.map((item) => ({
    uid: item.user.id,
    username: item.user.id,
    assetHolderId: item.user.assetHolderId || 'none',
    assets: item.user.id,
    count: item.count
  }))
})
</script>

<template>
  <el-table
    :data="users"
    stripe
    style="width: 100%"
    @sort-change="handleSortChange"
  >
    <el-table-column prop="uid" label="UID" width="180" sortable="custom" />
    <el-table-column
      prop="assetHolderId"
      label="Asset Holder ID"
      width="180"
      sortable="custom"
    />
    <el-table-column
      prop="username"
      label="Username"
      width="180"
      sortable="custom"
    />
    <el-table-column
      prop="count"
      label="Assets"
      width="180"
      sortable="custom"
    />
    <el-table-column label="Actions">
      <template #default="scope">
        <el-button
          :disabled="scope.row.uid === 'admin'"
          text
          type="primary"
          size="small"
          @click="handleEdit(scope.row)"
          >Edit</el-button
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
