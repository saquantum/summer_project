<script setup lang="ts">
import { Filter } from '@element-plus/icons-vue'
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'

const props = defineProps<{
  input: string
  visible: boolean
}>()

const emit = defineEmits([
  'update:input',
  'update:visible',
  'search',
  'clearFilters'
])

const input = computed({
  get: () => props.input,
  set: (val: string) => emit('update:input', val)
})

const visible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val)
})

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

const tags = ref<string[]>([])

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && input.value.trim()) {
    e.preventDefault()
    tags.value.push(input.value.trim())
    fuzzySearch(input.value)
    input.value = ''
  } else if (e.key === 'Backspace' && !input.value && tags.value.length) {
    tags.value.pop()
  }
}

const fuzzySearch = (input: string) => {
  console.log(input)
}

const removeTag = (index: number) => {
  tags.value.splice(index, 1)
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

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

defineExpose({})
</script>

<template>
  <el-popover
    :offset="0"
    ref="popoverRef"
    :visible="visible"
    placement="bottom-start"
    :width="375"
    trigger="manual"
  >
    <template #reference>
      <div class="tag-input-wrapper">
        <span class="tag" v-for="(tag, index) in tags" :key="index">
          {{ tag }}
          <span class="close" @click.stop="removeTag(index)">×</span>
        </span>
        <input
          @click="focusInput"
          ref="inputRef"
          v-model="input"
          @keydown="handleKeydown"
          class="tag-input"
          placeholder="Search assets..."
        />
        <el-button
          ref="referenceRef"
          @click="handleFilterClick"
          class="seamless-button"
        >
          <el-icon><Filter /></el-icon>
        </el-button></div
    ></template>

    <slot></slot>

    <div style="margin-top: 20px">
      <el-button @click="emit('search')">Search</el-button>
      <el-button @click="emit('clearFilters')">Clear filters</el-button>
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

.label {
  margin-top: 5px;
  margin-bottom: 5px;
}

.search-history {
  margin-top: 10px;
  padding: 0;
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
</style>
