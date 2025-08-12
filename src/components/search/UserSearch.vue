<script setup lang="ts">
import { useUserStore } from '@/stores'
import type { UserSearchBody, UserSearchForm } from '@/types'
import { userConverFormToFilter } from '@/utils/formUtils'
import { Filter } from '@element-plus/icons-vue'
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps<{
  userSearchBody: UserSearchBody
  fetchTableData: () => void
}>()

const emit = defineEmits(['update:userSearchBody'])

const visible = ref<boolean>(false)
const detail = ref<boolean>(false)

const popoverRef = ref<{ popperRef?: { contentRef: HTMLElement } } | null>(null)
const referenceRef = ref<{ $el: HTMLElement } | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)

const handleClickOutside = (e: MouseEvent) => {
  const popoverEl = popoverRef.value?.popperRef?.contentRef
  const referenceEl = referenceRef.value?.$el
  const dropdownEl = document.querySelector('.el-select-dropdown')
  if (
    popoverEl &&
    referenceEl &&
    !popoverEl.contains(e.target as Node) &&
    !referenceEl.contains(e.target as Node) &&
    (!dropdownEl || !dropdownEl.contains(e.target as Node)) &&
    inputRef.value &&
    !inputRef.value.contains(e.target as Node)
  ) {
    visible.value = false
  }
}

const userStore = useUserStore()

const handleRowClick = (id: string) => {
  fuzzySearch(id)
}

const input = ref<string>('')

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !input.value.trim()) {
    clearFilters()
  }
  if (e.key === 'Enter' && input.value.trim()) {
    e.preventDefault()
    fuzzySearch(input.value)
  }
}

const focusInput = () => {
  inputRef.value?.focus()
  visible.value = true
  detail.value = false
}

const handleFilterClick = () => {
  if (detail.value === true && visible.value === true) visible.value = false
  else {
    detail.value = true
    visible.value = true
  }
}

const form = ref<UserSearchForm>({
  id: '',
  name: ''
})

const handleSearch = () => {
  const newObj: UserSearchBody = {
    ...props.userSearchBody,
    filters: userConverFormToFilter(form.value)
  }
  emit('update:userSearchBody', newObj)
  props.fetchTableData()
  visible.value = false
}

const clearFilters = () => {
  form.value = {
    id: '',
    name: ''
  }
  handleSearch()
}

const fuzzySearch = (input: string) => {
  const fuzzyForm = {
    id: input,
    name: ''
  }
  const obj: UserSearchBody = {
    ...props.userSearchBody,
    filters: userConverFormToFilter(fuzzyForm)
  }
  emit('update:userSearchBody', obj)
  const index = userStore.userSearchHistory.indexOf(input)
  if (index === -1) {
    userStore.userSearchHistory.unshift(input)
  } else {
    userStore.userSearchHistory.splice(index, 1)
    userStore.userSearchHistory.unshift(input)
  }
  props.fetchTableData()
  visible.value = false
}

const clearSearchHistory = () => {
  userStore.clearUserSearchHistory()
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

defineExpose({
  form
})
</script>

<template>
  <el-popover
    :offset="0"
    ref="popoverRef"
    :visible="visible"
    placement="bottom-start"
    :width="500"
    :show-arrow="false"
    trigger="manual"
  >
    <template #reference>
      <div class="tag-input-wrapper">
        <input
          @click="focusInput"
          ref="inputRef"
          v-model="input"
          @keydown="handleKeydown"
          class="tag-input"
          placeholder="Search users..."
        />
        <el-button
          ref="referenceRef"
          @click="handleFilterClick"
          class="seamless-button"
        >
          <el-icon><Filter /></el-icon>
        </el-button></div
    ></template>
    <div v-if="detail">
      <el-form
        :model="form"
        label-width="auto"
        label-position="left"
        @submit.prevent
      >
        <el-form-item label="Id">
          <el-input v-model="form.id"></el-input>
        </el-form-item>
        <el-form-item label="Name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
      </el-form>

      <div style="margin-top: 20px">
        <el-button @click="handleSearch">Search</el-button>
        <el-button @click="clearFilters">Clear filters</el-button>
      </div>
    </div>
    <div v-else class="search-history">
      <div class="search-history-header">
        <span>Search history</span>
        <el-button
          v-if="userStore.userSearchHistory.length > 0"
          type="text"
          size="small"
          @click="clearSearchHistory"
          class="clear-history-btn"
        >
          Clear
        </el-button>
      </div>
      <ul v-if="userStore.userSearchHistory.length > 0">
        <li
          v-for="(item, index) in userStore.userSearchHistory.slice(0, 5)"
          :key="index"
          @click="handleRowClick(item)"
        >
          {{ item }}
        </li>
      </ul>
      <div v-else class="empty-history">No search history</div>
    </div>
  </el-popover>
</template>

<style scoped>
.seamless-button {
  border: none;
  border-radius: 0;
}

.tag-input-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding-left: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 32px;
  max-width: 300px;
  cursor: text;
  background-color: white;
}

.tag-input {
  flex: 1;
  border: none;
  outline: none;
  min-width: 80px;
  font-size: 14px;
}

.label {
  margin-top: 5px;
  margin-bottom: 5px;
}

.search-history {
  margin-top: 10px;
  padding: 0;
}
.search-history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.clear-history-btn {
  padding: 0;
  font-size: 12px;
  color: #909399;
}
.clear-history-btn:hover {
  color: #409eff;
}
.search-history ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.search-history li {
  padding: 6px 12px;
  margin-bottom: 4px;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background 0.2s;
}
.search-history li:hover {
  background: #e0e7ef;
  color: #409eff;
}
.empty-history {
  color: #909399;
  font-size: 14px;
  text-align: center;
  padding: 20px 0;
}
</style>
