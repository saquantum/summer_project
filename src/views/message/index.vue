<script setup>
import { ref, computed } from 'vue'
const searchKeyword = ref('')
const dialogVisible = ref(false)
const selectedMail = ref(null)
const currentPage = ref(1)
const pageSize = 10
const popoverVisible = ref(false)

const mails = ref([
  {
    id: 1,
    subject: 'Welcome to your inbox',
    sender: 'System Notification',
    time: '2025-06-25 10:00',
    content: 'This is the content of the first email',
    read: false
  },
  {
    id: 2,
    subject: 'Your account has been activated',
    sender: 'Administrator',
    time: '2025-06-24 15:30',
    content: 'Please confirm the information is correct',
    read: true
  }
])

const selectedMails = ref([])

const handleSelectionChange = (val) => {
  selectedMails.value = val
}

const openMail = (mail) => {
  selectedMail.value = mail
  dialogVisible.value = true
  mail.read = true
}

const filteredMails = computed(() => {
  let result = mails.value
  if (searchKeyword.value) {
    result = result.filter((mail) =>
      mail.subject.toLowerCase().includes(searchKeyword.value.toLowerCase())
    )
  }
  const start = (currentPage.value - 1) * pageSize
  return result.slice(start, start + pageSize)
})

const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<template>
  <div
    class="search-dropdown"
    style="display: flex; align-items: center; width: 260px"
  >
    <el-input
      placeholder="Search something..."
      style="flex: 1"
      v-model="searchText"
    />
    <el-popover
      v-model:visible="popoverVisible"
      placement="bottom"
      width="260"
      trigger="manual"
    >
      <div>detail</div>
      <template #reference>
        <el-button icon="Filter" @click="popoverVisible = !popoverVisible" />
      </template>
    </el-popover>
  </div>

  <el-card shadow="hover">
    <template #header>
      <div class="header">Inbox</div>
    </template>

    <el-table
      :data="filteredMails"
      @selection-change="handleSelectionChange"
      style="width: 100%"
      height="400"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column label="Subject" prop="subject">
        <template #default="{ row }">
          <span
            :style="{ fontWeight: row.read ? 'normal' : 'bold' }"
            @click="openMail(row)"
          >
            {{ row.subject }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="Sender" prop="sender" width="120" />
      <el-table-column label="Time" prop="time" width="180" />
    </el-table>

    <el-pagination
      layout="prev, pager, next"
      :page-size="pageSize"
      :total="filteredMails.length"
      @current-change="handlePageChange"
      style="margin-top: 20px; text-align: right"
    />

    <!-- mail detail -->
    <el-dialog v-model="dialogVisible" width="50%">
      <h3>{{ selectedMail?.subject }}</h3>
      <p><strong>Sender:</strong>{{ selectedMail?.sender }}</p>
      <p><strong>Time:</strong>{{ selectedMail?.time }}</p>
      <div style="margin-top: 10px">
        {{ selectedMail?.content }}
      </div>
    </el-dialog>
  </el-card>
</template>
