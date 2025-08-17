<script setup lang="ts">
import { useMailStore } from '@/stores/modules/mail.ts'
import type { Mail } from '@/types'
import {
  Filter,
  Sort,
  Search,
  Delete,
  Check,
  ArrowLeft
} from '@element-plus/icons-vue'
import { ref, computed, onMounted, watch } from 'vue'

import { deleteMailService, readMailService } from '@/api/mail.ts'
import { useUserStore } from '@/stores'
import { useResponsiveAction } from '@/composables/useResponsiveAction.ts'
const mailStore = useMailStore()
const userStore = useUserStore()

const isWideScreen = ref(true)

// sort and filter
const filter = ref<'all' | 'unread'>('all')
const sort = ref<'date' | 'subject'>('date')
const sortOrder = ref<'asc' | 'desc'>('asc')
const searchKeyword = ref('')

const selectedMail = ref<Mail | null>(null)
const currentPage = ref(1)
const pageSize = 10

const mailDetailVisible = ref(false)

const currentMails = ref<Mail[]>([])

const currentPageMails = computed(() => {
  if (!currentMails.value || currentMails.value.length === 0) return []
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  const arr = currentMails.value.slice(start, end)
  return arr
})

const handleRowClick = async (mail: Mail) => {
  selectedMail.value = mail
  mailDetailVisible.value = true
  const rawMail = mailStore.mails.find((item) => item.rowId === mail.rowId)
  if (rawMail) rawMail.hasRead = true
  try {
    if (userStore.user && rawMail) {
      await readMailService(userStore.user.id, String(rawMail.rowId))
      mailStore.getMails()
    }
  } catch (e) {
    console.error(e)
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

// delete dialog
const dialogVisible = ref(false)

const deleteId = ref<string[]>([])

const triggerDelete = (rows: string[]) => {
  dialogVisible.value = true
  deleteId.value = [...rows]
}

const handleDelete = async () => {
  if (deleteId.value.length === 0 || !userStore.user) return
  const res = await deleteMailService(userStore.user.id, deleteId.value)
  console.log(res)
  deleteId.value = []

  mailStore.getMails()
  dialogVisible.value = false
}

onMounted(() => {
  mailStore.getMails()
})

watch(
  [searchKeyword, filter, sort, sortOrder, () => mailStore.mails],
  () => {
    if (!mailStore.mails || mailStore.mails.length === 0) return
    let mails =
      filter.value === 'all'
        ? mailStore.mails.slice()
        : mailStore.mails.filter((item) => item.hasRead === false)

    const keyword = searchKeyword.value.trim().toLowerCase()
    if (keyword) {
      mails = mails.filter(
        (item) =>
          (item.title && item.title.toLowerCase().includes(keyword)) ||
          (item.message && item.message.toLowerCase().includes(keyword))
      )
    }

    if (sort.value === 'date') {
      mails.sort((a, b) => {
        const t1 =
          typeof a.issuedDate === 'number'
            ? a.issuedDate
            : new Date(a.issuedDate).getTime()
        const t2 =
          typeof b.issuedDate === 'number'
            ? b.issuedDate
            : new Date(b.issuedDate).getTime()
        return sortOrder.value === 'asc' ? t1 - t2 : t2 - t1
      })
    } else if (sort.value === 'subject') {
      mails.sort((a, b) => {
        const s1 = a.title?.toLowerCase() || ''
        const s2 = b.title?.toLowerCase() || ''
        return sortOrder.value === 'asc'
          ? s1.localeCompare(s2)
          : s2.localeCompare(s1)
      })
    }
    currentMails.value = mails
    currentPage.value = 1
  },
  {
    immediate: true
  }
)

useResponsiveAction((width) => {
  if (width < 576) {
    isWideScreen.value = false
  } else if (width >= 576 && width < 768) {
    isWideScreen.value = false
  } else if (width >= 768 && width < 992) {
    isWideScreen.value = false
  } else {
    isWideScreen.value = true
  }
})
</script>

<template>
  <div class="mail-layout">
    <!-- list -->
    <div
      v-if="(!isWideScreen && !mailDetailVisible) || isWideScreen"
      class="mail-list-panel"
    >
      <el-card shadow="never">
        <template #header>
          <div class="header-container">
            <span class="header">Inbox</span>
            <div class="search-dropdown">
              <el-input
                placeholder="Search something..."
                v-model="searchKeyword"
                style="max-width: 300px"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
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
                    <el-dropdown-item @click="filter = 'all'">
                      <span
                        style="
                          display: inline-block;
                          width: 18px;
                          text-align: center;
                          margin-right: 4px;
                        "
                      >
                        <el-icon v-if="filter === 'all'"><Check /></el-icon>
                        <span v-else style="visibility: hidden"><Check /></span>
                      </span>
                      All
                    </el-dropdown-item>
                    <el-dropdown-item @click="filter = 'unread'">
                      <span
                        style="
                          display: inline-block;
                          width: 18px;
                          text-align: center;
                          margin-right: 4px;
                        "
                      >
                        <el-icon v-if="filter === 'unread'"><Check /></el-icon>
                        <span v-else style="visibility: hidden"><Check /></span>
                      </span>
                      Unread
                    </el-dropdown-item>
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
                    <el-dropdown-item @click="sort = 'date'">
                      <span
                        style="
                          display: inline-block;
                          width: 18px;
                          text-align: center;
                          margin-right: 4px;
                        "
                      >
                        <el-icon v-if="sort === 'date'"><Check /></el-icon>
                        <span v-else style="visibility: hidden"><Check /></span>
                      </span>
                      Date
                    </el-dropdown-item>
                    <el-dropdown-item @click="sort = 'subject'">
                      <span
                        style="
                          display: inline-block;
                          width: 18px;
                          text-align: center;
                          margin-right: 4px;
                        "
                      >
                        <el-icon v-if="sort === 'subject'"><Check /></el-icon>
                        <span v-else style="visibility: hidden"><Check /></span>
                      </span>
                      Subject
                    </el-dropdown-item>
                    <div style="padding: 8px 16px; font-weight: bold">
                      Sort order
                    </div>
                    <div v-if="sort === 'date'">
                      <el-dropdown-item @click="sortOrder = 'asc'">
                        <span
                          style="
                            display: inline-block;
                            width: 18px;
                            text-align: center;
                            margin-right: 4px;
                          "
                        >
                          <el-icon v-if="sortOrder === 'asc'"
                            ><Check
                          /></el-icon>
                          <span v-else style="visibility: hidden"
                            ><Check
                          /></span>
                        </span>
                        Oldest on top
                      </el-dropdown-item>
                      <el-dropdown-item @click="sortOrder = 'desc'">
                        <span
                          style="
                            display: inline-block;
                            width: 18px;
                            text-align: center;
                            margin-right: 4px;
                          "
                        >
                          <el-icon v-if="sortOrder === 'desc'"
                            ><Check
                          /></el-icon>
                          <span v-else style="visibility: hidden"
                            ><Check
                          /></span>
                        </span>
                        Newest on top
                      </el-dropdown-item>
                    </div>

                    <div v-if="sort === 'subject'">
                      <el-dropdown-item @click="sortOrder = 'asc'">
                        <span
                          style="
                            display: inline-block;
                            width: 18px;
                            text-align: center;
                            margin-right: 4px;
                          "
                        >
                          <el-icon v-if="sortOrder === 'asc'"
                            ><Check
                          /></el-icon>
                          <span v-else style="visibility: hidden"
                            ><Check
                          /></span>
                        </span>
                        A-Z
                      </el-dropdown-item>
                      <el-dropdown-item @click="sortOrder = 'desc'">
                        <span
                          style="
                            display: inline-block;
                            width: 18px;
                            text-align: center;
                            margin-right: 4px;
                          "
                        >
                          <el-icon v-if="sortOrder === 'desc'"
                            ><Check
                          /></el-icon>
                          <span v-else style="visibility: hidden"
                            ><Check
                          /></span>
                        </span>
                        Z-A
                      </el-dropdown-item>
                    </div>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>

        <!-- Empty state message -->
        <div v-if="currentPageMails.length === 0" class="empty-state">
          <div class="empty-content">
            <el-icon size="48" color="#c0c4cc">
              <Search />
            </el-icon>
            <p>
              {{
                searchKeyword
                  ? 'No messages found matching your search'
                  : 'No messages in your inbox'
              }}
            </p>
          </div>
        </div>

        <div
          v-for="mail in currentPageMails"
          :key="mail.rowId"
          class="mail-item"
          :class="{
            unread: !mail.hasRead,
            selected: mail.rowId === selectedMail?.rowId
          }"
        >
          <div class="mail-content" @click="handleRowClick(mail)">
            <div class="mail-header">
              <span class="mail-title">{{ mail.title }}</span>
            </div>
            <div>
              <span class="mail-date">{{
                typeof mail.issuedDate === 'number'
                  ? new Date(mail.issuedDate).toLocaleString()
                  : mail.issuedDate
              }}</span>
            </div>
          </div>
          <div class="mail-actions">
            <el-button
              @click="triggerDelete([String(mail.rowId)])"
              style="border: none"
              ><el-icon><Delete /></el-icon
            ></el-button>
          </div>
        </div>

        <el-pagination
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="currentMails.length"
          @current-change="handlePageChange"
          style="
            margin-top: 20px;
            text-align: right;
            display: flex;
            justify-content: center;
          "
        />
      </el-card>
    </div>
    <!-- big screen detail -->
    <div v-if="isWideScreen && selectedMail" class="mail-detail-panel">
      <el-card shadow="never">
        <template #header>
          <div>
            <h3>{{ selectedMail?.title }}</h3>
          </div>
        </template>
        <p>
          <strong>Time:</strong>
          {{
            typeof selectedMail?.issuedDate === 'number'
              ? new Date(selectedMail?.issuedDate).toLocaleString()
              : selectedMail?.issuedDate
          }}
        </p>
        <div style="margin-top: 10px" v-html="selectedMail?.message"></div>
      </el-card>
    </div>
    <!-- small screen detail -->
    <el-card
      shadow="never"
      v-if="!isWideScreen && mailDetailVisible"
      class="mail-detail-panel"
    >
      <template #header>
        <div>
          <el-button @click="mailDetailVisible = false" data-test="back-button">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h3>{{ selectedMail?.title }}</h3>
        </div>
      </template>
      <p>
        <strong>Time:</strong>
        {{
          typeof selectedMail?.issuedDate === 'number'
            ? new Date(selectedMail?.issuedDate).toLocaleString()
            : selectedMail?.issuedDate
        }}
      </p>
      <div style="margin-top: 10px" v-html="selectedMail?.message"></div>
    </el-card>
  </div>

  <!-- delete dialog -->
  <ConfirmDialog
    v-model="dialogVisible"
    title="Warning"
    content="Are you sure you want to delete this message"
    :countdown-duration="0"
    @confirm="handleDelete"
    @cancel="dialogVisible = false"
  />
</template>

<style scoped>
.mail-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 80vh;
  margin-top: 10px;
}

/* .mail-list-panel {
  min-width: 396px !important;
} */

.mail-list-panel .el-card {
  min-width: 396px !important;
}

@media (max-width: 991px) {
  /* .mail-list-panel {
    width: 100% !important;
  }
  .mail-list-panel .el-card {
    min-width: unset !important;
    width: 100% !important;
    /* max-width: 500px !important; */
  /* } */

  .mail-detail-panel {
    max-width: none !important;
  }
}

.mail-detail-panel {
  flex: 1 1 0%;
  min-width: 350px;
  max-width: 500px;
}

@media (min-width: 992px) {
  .mail-layout {
    flex-direction: row;
    align-items: flex-start;
    width: 100%;
  }

  .mail-list-panel .el-card {
    min-width: none !important;
    max-width: 400px !important;
    width: 100% !important;
  }
}

.button {
  border: none;
  padding: 4px 8px;
}

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

.mail-item {
  width: 100%;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
  box-sizing: border-box;
}
.mail-item.unread {
  font-weight: bold;
}
.mail-item.selected {
  background: #f5faff;
}
.mail-content {
  flex: 1;
  min-width: 0; /* Allow text truncation */
}
.mail-actions {
  width: 44px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.mail-header {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.mail-title {
  font-size: 16px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mail-date {
  color: #888;
  font-size: 13px;
  margin-left: 8px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  padding: 40px 20px;
}

.empty-content {
  text-align: center;
  color: #909399;
}

.empty-content p {
  margin-top: 16px;
  font-size: 14px;
  color: #909399;
}
</style>
