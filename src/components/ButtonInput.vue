<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
const visible = ref(false)

// custom input
const tags = ref([])
const input = ref('')
const inputRef = ref()
const popoverRef = ref()
const removeTag = (index) => {
  tags.value.splice(index, 1)
}

const handleKeydown = (e) => {
  if (e.key === 'Backspace' && !input.value && tags.value.length) {
    tags.value.pop()
  }
}

const focusInput = () => {
  inputRef.value.focus()
  visible.value = true
}

const tableData = [
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' }
]
const handleRowClick = (row) => {
  tags.value.push(row.date)
  visible.value = false
}

const handleClickOutside = (e) => {
  const popoverEl = popoverRef.value?.popperRef?.contentRef
  const dropdownEl = document.querySelector('.el-select-dropdown') // select 的弹窗
  if (
    !popoverEl.contains(e.target) &&
    (!dropdownEl || !dropdownEl.contains(e.target)) &&
    !inputRef.value.contains(e.target)
  ) {
    visible.value = false
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<template>
  <el-popover
    ref="popoverRef"
    placement="bottom-start"
    title="Suggestion"
    :width="500"
    :show-arrow="false"
    trigger="manual"
    :teleported="false"
    :visible="visible"
    :offset="0"
  >
    <template #reference>
      <div class="tag-input-wrapper" @click="focusInput">
        <span class="tag" v-for="(tag, index) in tags" :key="index">
          {{ tag }}
          <span class="close" @click.stop="removeTag(index)">×</span>
        </span>
        <input
          ref="inputRef"
          v-model="input"
          @keydown="handleKeydown"
          class="tag-input"
          placeholder="Add tags..."
        />
      </div>
    </template>
    <el-table :data="tableData" style="width: 100%" @row-click="handleRowClick">
      <el-table-column prop="date" />
    </el-table>
  </el-popover>
</template>

<style scoped>
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

.tag {
  background-color: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 4px;
  display: flex;
  align-items: center;
  font-size: 12px;
}

.close {
  margin-left: 6px;
  cursor: pointer;
  font-weight: bold;
}

.tag-input {
  flex: 1;
  border: none;
  outline: none;
  min-width: 80px;
  font-size: 14px;
}
</style>
