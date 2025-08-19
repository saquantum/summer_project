<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  adminAssignUsersToGroup,
  adminDeleteUserService,
  adminGetUsersTotalService
} from '@/api/admin'
import { useRouter } from 'vue-router'
import type { PermissionGroup, UserItem, UserSearchBody } from '@/types'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

interface User {
  uid: string
  username: string
  assets: string
  count: number
  role: 'admin' | 'user'
  permission: PermissionGroup
}

const router = useRouter()

const userStore = useUserStore()

const isLoading = ref(true)

const users = computed(() => {
  if (!userStore.users || userStore.users.length <= 0) return []
  return userStore.users.map((item: UserItem) => ({
    uid: item.user.id,
    name: item.user.name ?? 'Null',
    assets: item.user.id,
    count: item.accumulation,
    role: item.user.admin ? 'admin' : 'user',
    permission: item.user.accessControlGroup
  }))
})

const multiSort = ref<{ prop: string; order: string }[]>([])
const columns = ref([
  { prop: 'uid', label: 'UID' },
  { prop: 'name', label: 'Name' },
  { prop: 'count', label: 'Asset' }
])

const handleEdit = (row: User) => {
  router.push({ path: '/admin/user/detail', query: { id: row.uid } })
}

const fetchTableData = async () => {
  try {
    isLoading.value = true
    const propOrderList: string[] = []

    for (const { prop, order } of multiSort.value) {
      let dbField = ''
      if (prop === 'uid') dbField = 'user_id'
      else if (prop === 'assetHolderId') dbField = 'asset_holder_id'
      else if (prop === 'count') dbField = 'accumulation'
      else if (prop === 'name') dbField = 'user_name'
      else continue

      const sortDir = order === 'descending' ? 'desc' : 'asc'
      propOrderList.push(`${dbField},${sortDir}`)
    }

    const sortStr =
      propOrderList.length > 0 ? propOrderList.join(',') : 'user_id,asc'

    userSearchBody.value.offset = (currentPage.value - 1) * pageSize.value
    userSearchBody.value.limit = pageSize.value
    userSearchBody.value.orderList = sortStr

    const [, res] = await Promise.all([
      userStore.getUsers('count', userSearchBody.value),
      adminGetUsersTotalService({ filters: userSearchBody.value.filters })
    ])

    total.value = res.data
  } catch (error) {
    console.error('Failed to fetch table data:', error)
    ElMessage.error('Failed to fetch table data')
  } finally {
    isLoading.value = false
  }
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

const triggerDelete = (rows: User[]) => {
  dialogVisible.value = true
  rows.forEach((element) => {
    deleteId.value.push(element.uid)
  })
}

const handleDelete = async () => {
  if (deleteId.value.length === 0) return
  await adminDeleteUserService(deleteId.value)
  deleteId.value = []
  await fetchTableData()
}

const handleCancel = () => {
  deleteId.value = []
}

const mutipleSelection = ref<User[]>([])
const handleSelectionChange = (val: User[]) => {
  mutipleSelection.value = val
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
  const user = users.value.find((user) => user.uid === uid)
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

const permissionDialogVisible = ref(false)
const selectAll = ref(false)

const handleUpdatePermissionGroup = async (groupName: string) => {
  try {
    if (selectAll.value) {
      const filters = {
        filters: userSearchBody.value.filters
      }
      await adminAssignUsersToGroup(groupName, filters)
    } else {
      const filters = {
        filters: {
          user_id: {
            op: 'in',
            list: mutipleSelection.value.map((item) => {
              return item.uid
            })
          }
        }
      }

      await adminAssignUsersToGroup(groupName, filters)
    }
    permissionDialogVisible.value = false
    fetchTableData()
  } catch {
    ElMessage.error('Fail to update permission')
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
  fetchTableData()
})

defineExpose({
  users
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
    <el-button
      @click="permissionDialogVisible = true"
      data-test="set-permission"
      >Set permission</el-button
    >
    <el-button
      v-show="mutipleSelection.length > 0"
      type="danger"
      @click="triggerDelete(mutipleSelection)"
      data-test="delete-selected"
      >Delete</el-button
    >
  </div>

  <div class="collapse-wrapper">
    <UserCollapse :users="users"></UserCollapse>
  </div>
  <el-table
    v-loading="isLoading"
    :data="users"
    stripe
    style="width: 100%"
    @sort-change="handleSortChange"
    :default-sort="multiSort[0] || {}"
    @selection-change="handleSelectionChange"
  >
    <el-table-column
      type="selection"
      :selectable="(row: User) => row.role !== 'admin'"
    >
    </el-table-column>
    <el-table-column
      v-for="(item, index) in columns"
      :key="index"
      :label="item.label"
      :prop="item.prop"
      width="auto"
      sortable="custom"
    ></el-table-column>
    <el-table-column label="Role" prop="role"> </el-table-column>
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
          :disabled="scope.row.role === 'admin'"
          text
          type="primary"
          size="small"
          @click="handleEdit(scope.row)"
          data-test="edit"
          >Edit</el-button
        >
        <el-button
          :disabled="scope.row.role === 'admin'"
          text
          type="danger"
          size="small"
          @click="triggerDelete([scope.row])"
          data-test="delete"
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
    @cancel="handleCancel"
  />

  <PermissionDialog
    v-model:permissionDialogVisible="permissionDialogVisible"
    v-model:selectAll="selectAll"
    :total="total"
    @update-permission-group="handleUpdatePermissionGroup"
  ></PermissionDialog>
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
