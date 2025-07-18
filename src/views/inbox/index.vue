<script setup lang="ts">
import { useMailStore } from '@/stores/modules/mail'
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

import { readMailService } from '@/api/mail'
import { useUserStore } from '@/stores'
const mailStore = useMailStore()
const userStore = useUserStore()

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
  if (!currentMails.value || currentMails.value.length === 0) return
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

// delelte
const handleDelete = () => console.log(111)

watch(
  [filter, sort, sortOrder, () => mailStore.mails],
  () => {
    if (!mailStore.mails || mailStore.mails.length === 0) return
    const mails =
      filter.value === 'all'
        ? mailStore.mails.slice()
        : mailStore.mails.filter((item) => item.hasRead === false)

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

onMounted(() => {
  mailStore.getMails()
})
</script>

<template>
  <div class="card-container">
    <el-card v-if="!mailDetailVisible" class="mail-card">
      <template #header>
        <div class="header-container">
          <span class="header">Inbox</span>
          <div class="search-dropdown">
            <el-input
              placeholder="Search something..."
              v-model="searchKeyword"
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
                  <el-dropdown-item @click="filter = 'all'">
                    <el-icon v-if="filter === 'all'" style="margin-right: 4px"
                      ><Check
                    /></el-icon>
                    All</el-dropdown-item
                  >
                  <el-dropdown-item @click="filter = 'unread'">
                    <el-icon
                      v-if="filter === 'unread'"
                      style="margin-right: 4px"
                      ><Check
                    /></el-icon>
                    Unread</el-dropdown-item
                  >
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
                    <el-icon v-if="sort === 'date'" style="margin-right: 4px"
                      ><Check /></el-icon
                    >Date</el-dropdown-item
                  >
                  <el-dropdown-item @click="sort = 'subject'"
                    ><el-icon
                      v-if="sort === 'subject'"
                      style="margin-right: 4px"
                      ><Check /></el-icon
                    >Subject</el-dropdown-item
                  >
                  <div style="padding: 8px 16px; font-weight: bold">
                    Sort order
                  </div>
                  <div v-if="sort === 'date'">
                    <el-dropdown-item @click="sortOrder = 'asc'"
                      ><el-icon
                        v-if="sortOrder === 'asc'"
                        style="margin-right: 4px"
                        ><Check /></el-icon
                      >Oldest on top</el-dropdown-item
                    >
                    <el-dropdown-item @click="sortOrder = 'desc'"
                      ><el-icon
                        v-if="sortOrder === 'desc'"
                        style="margin-right: 4px"
                        ><Check /></el-icon
                      >Newest on top</el-dropdown-item
                    >
                  </div>

                  <div v-if="sort === 'subject'">
                    <el-dropdown-item @click="sortOrder = 'asc'"
                      ><el-icon
                        v-if="sortOrder === 'asc'"
                        style="margin-right: 4px"
                        ><Check /></el-icon
                      >A-Z</el-dropdown-item
                    >
                    <el-dropdown-item @click="sortOrder = 'desc'"
                      ><el-icon
                        v-if="sortOrder === 'desc'"
                        style="margin-right: 4px"
                        ><Check /></el-icon
                      >Z-A</el-dropdown-item
                    >
                  </div>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <div>
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
            <el-button @click="handleDelete"
              ><el-icon><Delete /></el-icon
            ></el-button>
          </div>
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

      <!-- mail detail -->
    </el-card>
    <el-card v-else>
      <template #header>
        <div>
          <el-button @click="mailDetailVisible = false">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <h3>{{ selectedMail?.title }}</h3>
        </div>
      </template>
      <p>
        <strong>Time:</strong
        >{{
          typeof selectedMail?.issuedDate === 'number'
            ? new Date(selectedMail?.issuedDate).toLocaleString()
            : selectedMail?.issuedDate
        }}
      </p>
      <div style="margin-top: 10px">
        {{ selectedMail?.message }}
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.card-container {
  display: flex;
  justify-content: center;
  width: 80vw;
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
  width: 356px;
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
  width: 316px;
}
.mail-actions {
  width: 44px;
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
</style>
