<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminDeleteTemplateByIdService } from '@/api/admin'
import { useRouter } from 'vue-router'
import { useTemplateStore } from '@/stores'
import type { Template } from '@/types'

interface TableRow {
  id: number
  title: string
}

const router = useRouter()

const templateStore = useTemplateStore()

const multiSort = ref<{ prop: string; order: string }[]>([])
const columns = ref([
  { prop: 'id', label: 'id' },
  { prop: 'assetTypeId', label: 'Asset type' },
  { prop: 'warningType', label: 'Warning type' },
  { prop: 'severity', label: 'Severity' },
  { prop: 'contactChannel', label: 'Channel' }
])

const handleEdit = (row: Template) => {
  router.push({
    path: '/admin/message/template',
    query: {
      id: row.id
    }
  })
}

const handleDelete = async (row: TableRow) => {
  console.log(row)
  try {
    await adminDeleteTemplateByIdService(deleteId.value)
    fetchTableData()
  } catch (e) {
    console.error(e)
  }
}

const fetchTableData = async () => {
  const propOrderList: string[] = []

  for (const { prop, order } of multiSort.value) {
    let dbField = ''
    if (prop === 'id') dbField = 'user_id'
    else if (prop === 'assetHolderId') dbField = 'asset_holder_id'
    else if (prop === 'count') dbField = 'accumulation'
    else continue

    const sortDir = order === 'descending' ? 'desc' : 'asc'
    propOrderList.push(`${dbField},${sortDir}`)
  }

  const sortStr =
    propOrderList.length > 0 ? propOrderList.join(',') : 'user_id,asc'

  templateStore.getTemplates(
    (currentPage.value - 1) * pageSize.value,
    pageSize.value,
    sortStr
  )
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
const deleteId = ref<number[]>([])

const triggerDelete = (row: TableRow) => {
  dialogVisible.value = true
  deleteId.value.push(row.id)
}

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(50)

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchTableData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTableData()
}

onMounted(() => {
  fetchTableData()
})
</script>

<template>
  <div class="search-wrapper">
    <FilterSearch></FilterSearch>
    <SortTool
      v-model:multiSort="multiSort"
      :columns="columns"
      :fetch-table-data="fetchTableData"
    ></SortTool>
  </div>

  <el-table
    :data="templateStore.templates"
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
