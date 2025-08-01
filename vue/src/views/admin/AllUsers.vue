<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  adminDeleteUserService,
  adminGetPermissionByUIDService,
  adminGetUsersTotalService,
  adminUpdatePermissionService
} from '@/api/admin'
import { useRouter } from 'vue-router'
import type { Permission, UserItem, UserSearchBody } from '@/types'
import { useUserStore } from '@/stores'

interface TableRow {
  uid: string
  username: string
  assetHolderId: string
  assets: string
  count: number
  role: 'admin' | 'user'
  permission: Permission
}

const router = useRouter()

const userStore = useUserStore()

const userTable = computed(() => {
  if (!userStore.users || userStore.users.length <= 0) return []
  return userStore.users.map((item: UserItem) => ({
    uid: item.user.id,
    name: item.user.assetHolder?.name ?? 'Null',
    assets: item.user.id,
    count: item.accumulation,
    role: item.user.admin ? 'admin' : 'user',
    permission: item.user.permissionConfig
  }))
})

const multiSort = ref<{ prop: string; order: string }[]>([])
const columns = ref([
  { prop: 'uid', label: 'UID' },
  { prop: 'name', label: 'Name' },
  { prop: 'role', label: 'Role' },
  { prop: 'count', label: 'Asset' }
])

const handleEdit = (row: TableRow) => {
  router.push({ path: '/admin/user/detail', query: { id: row.uid } })
}

const handleDelete = async (row: TableRow) => {
  console.log(row)
  try {
    await adminDeleteUserService(deleteId.value)
    fetchTableData()
  } catch (e) {
    console.error(e)
  }
}

const fetchTableData = async () => {
  const propOrderList: string[] = []

  for (const { prop, order } of multiSort.value) {
    let dbField = ''
    if (prop === 'uid') dbField = 'user_id'
    else if (prop === 'assetHolderId') dbField = 'asset_holder_id'
    else if (prop === 'count') dbField = 'accumulation'
    else continue

    const sortDir = order === 'descending' ? 'desc' : 'asc'
    propOrderList.push(`${dbField},${sortDir}`)
  }

  const sortStr =
    propOrderList.length > 0 ? propOrderList.join(',') : 'user_id,asc'

  userSearchBody.value.offset = (currentPage.value - 1) * pageSize.value
  userSearchBody.value.limit = pageSize.value
  userSearchBody.value.orderList = sortStr
  userStore.getUsers('count', userSearchBody.value)

  const userTotalBody = { filters: userSearchBody.value.filters }
  const res = await adminGetUsersTotalService(userTotalBody)
  total.value = res.data
}

const handleSortChange = (sort: { prop: string; order: string | null }) => {
  const index = multiSort.value.findIndex((item) => item.prop === sort.prop)
  if (index !== -1) {
    if (!sort.order) {
      multiSort.value.splice(index, 1)
    } else {
      multiSort.value[index].order = sort.order
    }
  } else {
    if (sort.order) {
      multiSort.value.push({ prop: sort.prop, order: sort.order })
    }
  }
  fetchTableData()
}

// delete confirm
const dialogVisible = ref(false)
const deleteId = ref<string[]>([])

const triggerDelete = (row: TableRow) => {
  dialogVisible.value = true
  deleteId.value.push(row.uid)
}

const permissionFields = [
  'canCreateAsset',
  'canSetPolygonOnCreate',
  'canUpdateAssetFields',
  'canUpdateAssetPolygon',
  'canDeleteAsset',
  'canUpdateProfile'
] as const

function getPermission(uid: string) {
  const user = userTable.value.find((user) => user.uid === uid)
  if (user?.role === 'admin') {
    return {
      userId: user?.uid,
      canCreateAsset: true,
      canSetPolygonOnCreate: true,
      canUpdateAssetFields: true,
      canUpdateAssetPolygon: true,
      canDeleteAsset: true,
      canUpdateProfile: true
    }
  }
  return user?.permission
}

const permission = ref<Permission[] | null>(null)
const checkboxOptions = ref<{ label: string; value: boolean }[]>([])

const submit = async () => {
  if (permission.value && permission.value.length > 0) {
    const p = permission.value[0]
    p.canCreateAsset = checkboxOptions.value[0].value
    p.canSetPolygonOnCreate = checkboxOptions.value[1].value
    p.canUpdateAssetPolygon = checkboxOptions.value[2].value
    p.canUpdateAssetFields = checkboxOptions.value[3].value
    p.canDeleteAsset = checkboxOptions.value[4].value
    p.canUpdateProfile = checkboxOptions.value[5].value
    console.log(p)
    await adminUpdatePermissionService(p)
  }
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userSearchBody = ref<UserSearchBody>({
  filters: {},
  orderList: '',
  limit: pageSize.value,
  offset: 0
})

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchTableData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTableData()
}

onMounted(async () => {
  if (userStore.user) {
    const res = await adminGetPermissionByUIDService(userStore.user.id)
    permission.value = res.data
  }

  if (permission.value && permission.value.length > 0) {
    const p = permission.value[0]
    checkboxOptions.value = [
      { label: 'Add new asset', value: p.canCreateAsset },
      { label: 'Add polygon', value: p.canSetPolygonOnCreate },
      { label: 'Update polygon', value: p.canUpdateAssetPolygon },
      { label: 'Update basic information', value: p.canUpdateAssetFields },
      { label: 'Delete asset', value: p.canDeleteAsset },
      { label: 'Update profile', value: p.canUpdateProfile }
    ]
  }
  fetchTableData()
})
</script>

<template>
  <div class="search-wrapper">
    <UserSearch
      :fetch-table-data="fetchTableData"
      v-model:user-search-body="userSearchBody"
    ></UserSearch>

    <SortTool
      v-model:multiSort="multiSort"
      :columns="columns"
      :fetch-table-data="fetchTableData"
    ></SortTool>
  </div>

  <div>
    <h3>Access control</h3>
    <el-checkbox
      v-for="(item, index) in checkboxOptions"
      :key="index"
      :label="item.label"
      v-model="item.value"
    />
    <div>
      <el-button @click="submit">Submit</el-button>
    </div>
  </div>

  <div class="collapse-wrapper">
    <UserCollapse :users="userTable"></UserCollapse>
  </div>
  <el-table
    :data="userTable"
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
    <el-table-column label="Permission">
      <template #default="scope">
        <div style="display: flex; gap: 3px">
          <PermissionIndicator
            v-for="(field, index) in permissionFields"
            :key="index"
            :status="getPermission(scope.row.uid)?.[field] || false"
            :field="field"
          />
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
          @click="triggerDelete(scope.row)"
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

  <ConfirmDialog
    v-model="dialogVisible"
    title="Warning"
    content="This will permanently delete this user"
    :countdown-duration="5"
    @confirm="handleDelete"
    @cancel="dialogVisible = false"
  />
</template>

<style scoped>
.search-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  height: 35px;
  gap: 10px;
  margin-bottom: 10px;
}

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
