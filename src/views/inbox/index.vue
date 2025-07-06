<script setup lang="ts">
import { Filter, Sort, Search } from '@element-plus/icons-vue'
import { ref, computed } from 'vue'
const searchKeyword = ref('')
const dialogVisible = ref(false)
const selectedMail = ref(null)
const currentPage = ref(1)
const pageSize = 10

const mailDetailVisible = ref(false)
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

const handleRowClick = (row) => {
  selectedMail.value = row
  mailDetailVisible.value = true
}
const handlePageChange = (page) => {
  currentPage.value = page
}
</script>

<template>
  <div class="card-container">
    <el-card shadow="hover">
      <template #header>
        <div class="header-container">
          <span class="header">Inbox</span>
          <div class="search-dropdown">
            <el-input
              placeholder="Search something..."
              v-model="searchText"
              style="max-width: 300px"
            >
              <template #append>
                <el-popover>
                  <el-button :icon="Search"></el-button>
                </el-popover>
              </template>
            </el-input>
          </div>
          <div>
            <el-dropdown placement="bottom-start" trigger="click">
              <el-button class="button">
                <Filter class="icon" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="() => console.log(123)"
                    >All</el-dropdown-item
                  >
                  <el-dropdown-item>Unread</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-dropdown placement="bottom-start" trigger="click">
              <el-button class="button"><Sort class="icon" /> </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <div style="padding: 8px 16px; font-weight: bold">
                    Sort by
                  </div>
                  <el-dropdown-item @click="() => console.log(123)"
                    >Date</el-dropdown-item
                  >
                  <el-dropdown-item>From</el-dropdown-item>
                  <el-dropdown-item>Importance</el-dropdown-item>
                  <el-dropdown-item>Subject</el-dropdown-item>
                  <div style="padding: 8px 16px; font-weight: bold">
                    Sort order
                  </div>
                  <el-dropdown-item>Oldest on top</el-dropdown-item>
                  <el-dropdown-item>Newest on top</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredMails"
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        height="400"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="Subject" prop="subject">
          <template #default="{ row }">
            <span :style="{ fontWeight: row.read ? 'normal' : 'bold' }">
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
        style="
          margin-top: 20px;
          text-align: right;
          display: flex;
          justify-content: center;
        "
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
    <el-card shadow="hover" v-show="mailDetailVisible">
      <template #header>
        <div>
          <h3>{{ selectedMail?.subject }}</h3>
        </div>
      </template>
      <p><strong>Sender:</strong>{{ selectedMail?.sender }}</p>
      <p><strong>Time:</strong>{{ selectedMail?.time }}</p>
      <div style="margin-top: 10px">
        {{ selectedMail?.content }}
      </div>
    </el-card>
  </div>
</template>

<style scoped>
/* .mailbox {
}
.search-dropdown {
} */
.card-container {
  display: flex;
  justify-content: center;
  width: 80vw;
}
.button {
  border: none;
  padding: 4px 8px;
}

/* .button:hover {
  background-color: transparent;
} */

.button svg {
  width: 18px;
  height: 18px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header {
  font-weight: bold;
  font-size: 18px;
}
</style>
