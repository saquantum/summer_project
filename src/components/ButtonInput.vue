<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import type { ElPopover } from 'element-plus'

const visible = ref(false)

const tags = ref<string[]>([])

const input = ref('')

const inputRef = ref<HTMLInputElement | null>(null)

const popoverRef = ref<InstanceType<typeof ElPopover> | null>(null)

const removeTag = (index: number) => {
  tags.value.splice(index, 1)
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Backspace' && !input.value && tags.value.length) {
    tags.value.pop()
  }
}

const focusInput = () => {
  inputRef.value?.focus()
  visible.value = true
}

const tableData = [
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' }
]

const handleRowClick = (row: { date: string }) => {
  tags.value.push(row.date)
  visible.value = false
}

const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as Node | null
  const popoverEl = popoverRef.value?.popperRef
    ?.contentRef as HTMLElement | null
  const dropdownEl = document.querySelector(
    '.el-select-dropdown'
  ) as HTMLElement | null

  if (
    target &&
    popoverEl &&
    !popoverEl.contains(target) &&
    (!dropdownEl || !dropdownEl.contains(target)) &&
    inputRef.value &&
    !inputRef.value.contains(target)
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
