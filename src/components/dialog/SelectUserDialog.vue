<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { adminGetUsersTotalService, adminSearchUsersService } from '@/api/admin'

import type { PermissionGroup, UserItem, UserSearchBody } from '@/types'

import { ElMessage } from 'element-plus'

interface User {
  uid: string
  name: string
  assets: string
  count: number
  role: 'admin' | 'user'
  permission: PermissionGroup
}

const visible = defineModel('visible')
const selectAll = defineModel('selectAll')

const emit = defineEmits(['confirm'])

const rawUsers = ref<UserItem[]>([])

const isLoading = ref(true)

const users = computed(() => {
  if (!rawUsers.value || rawUsers.value.length <= 0) return []
  return rawUsers.value.map((item: UserItem) => ({
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

    const res1 = await adminSearchUsersService('count', userSearchBody.value)
    if (res1.data) {
      rawUsers.value = res1.data
    }

    const res2 = await adminGetUsersTotalService({
      filters: userSearchBody.value.filters
    })
    total.value = res2.data
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

const mutipleSelection = ref<User[]>([])
const handleSelectionChange = (val: User[]) => {
  mutipleSelection.value = val
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

const handleConfirm = () => {
  if (selectAll.value) {
    const filters = {
      filters: userSearchBody.value.filters
    }
    emit('confirm', filters)
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
    emit('confirm', filters)
  }
}
onMounted(async () => {
  fetchTableData()
})

defineExpose({
  users
})
</script>

<template>
  <el-dialog v-model="visible">
    <el-checkbox v-model="selectAll">Select All ({{ total }})</el-checkbox>
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

    <template #footer>
      <div>
        <el-button @click="visible = false">Cancel</el-button>
        <el-button type="primary" @click="handleConfirm" data-test="confirm">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>
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
</style>
