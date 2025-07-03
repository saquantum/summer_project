<script setup>
import { ref, onMounted } from 'vue'
import { adminGetUsersService } from '@/api/admin'
import { useRouter } from 'vue-router'

const router = useRouter()
const users = ref([])

const multiSort = ref([])
const columns = ref([
  { prop: 'uid', label: 'UID' },
  { prop: 'assetHolderId', label: 'Asset Holder Id' },
  { prop: 'role', label: 'Role' },
  { prop: 'count', label: 'Asset' }
])
const handleEdit = async (row) => {
  router.push({ path: '/admin/user/detail', query: { id: row.assetHolderId } })
}

const handleSortChange = ({ prop, order }) => {
  const index = multiSort.value.findIndex((item) => item.prop === prop)
  if (index !== -1) {
    if (!order) {
      multiSort.value.splice(index, 1)
    } else {
      multiSort.value[index].order = order
    }
  } else {
    if (order) {
      multiSort.value.push({ prop, order })
    }
  }
  fetchTableData()
}

const fetchTableData = async () => {
  const propOrderList = []

  for (const { prop, order } of multiSort.value) {
    let dbField = ''
    if (prop === 'uid') dbField = 'user_id'
    else if (prop === 'assetHolderId') dbField = 'asset_holder_id'
    else if (prop === 'count') dbField = 'accumulation'
    // if no field, skip
    else continue

    const sortDir = order === 'descending' ? 'desc' : 'asc'
    propOrderList.push(`${dbField},${sortDir}`)
  }

  // order by id asc by default
  const sortStr =
    propOrderList.length > 0 ? propOrderList.join(',') : 'user_id,asc'

  const res = await adminGetUsersService(
    'count',
    (currentPage.value - 1) * pageSize.value,
    pageSize.value,
    sortStr
  )

  users.value = res.data.map((item) => {
    return {
      uid: item.user.id,
      username: item.user.id,
      assetHolderId: item.user.assetHolderId || 'none',
      assets: item.user.id,
      count: item.count,
      role: item.user.admin ? 'admin' : 'user'
    }
  })
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const handlePageChange = (page) => {
  currentPage.value = page
  fetchTableData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTableData()
}

onMounted(async () => {
  await fetchTableData()
})
</script>

<template>
  <div class="collapse-wrapper">
    <UserCollapse :users="users"></UserCollapse>
  </div>

  <SortTool
    v-model:multiSort="multiSort"
    :columns="columns"
    :fetch-table-data="fetchTableData"
  ></SortTool>
  <el-table
    :data="users"
    stripe
    style="width: 100%"
    @sort-change="handleSortChange"
    :default-sort="multiSort[0] || {}"
    class="table"
  >
    <el-table-column
      v-for="(item, index) in columns"
      :key="index"
      :label="item.label"
      :prop="item.prop"
      width="auto"
      sortable="custom"
    ></el-table-column>
    <el-table-column label="permission">
      <template #default>
        <div style="display: flex; gap: 3px">
          <PermissionIndicator :status="true"></PermissionIndicator>
          <PermissionIndicator></PermissionIndicator>
          <PermissionIndicator></PermissionIndicator>
          <PermissionIndicator></PermissionIndicator>
          <PermissionIndicator></PermissionIndicator>
        </div>
      </template>
    </el-table-column>
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

  <el-pagination
    background
    layout="total, prev, pager, next, sizes"
    :current-page="currentPage"
    :page-size="pageSize"
    :total="total"
    @current-change="handlePageChange"
    @size-change="handleSizeChange"
  />
</template>

<style scoped>
@media (min-width: 768px) {
  .collapse-wrapper {
    display: none !important;
  }
}
@media (max-width: 768px) {
  .table {
    display: none !important;
  }
}
</style>
