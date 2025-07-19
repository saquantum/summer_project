<script setup lang="ts">
import { useAssetStore, useUserStore } from '@/stores'
import type { AssetSearchBody, AssetSearchForm } from '@/types'
import { assetConverFormToFilter } from '@/utils/dataConversion'
import { Filter } from '@element-plus/icons-vue'
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps<{
  assetSearchBody: AssetSearchBody
  fetchTableData: () => void
}>()

const assetStore = useAssetStore()
const emit = defineEmits(['update:assetSearchBody'])

const visible = ref<boolean>(false)
const detail = ref<boolean>(false)
// const assetId = ref<string | null>(null)
// const lastType: string | null = null

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
  tags.value.push(id)
  fuzzySearch(id)
}

const tags = ref<string[]>([])
const input = ref<string>('')

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

const materialOption = [
  { label: 'Steel', value: 'Steel' },
  { label: 'Concrete', value: 'Concrete' },
  { label: 'Plastic', value: 'Plastic' },
  { label: 'Composite', value: 'Composite' }
]

const statusOption = [
  { label: 'inactive', value: 'inactive' },
  { label: 'active', value: 'active' },
  { label: 'maintenance', value: 'maintenance' }
]

// select value
const warningLevelOptions = [
  {
    value: 'NO',
    label: 'No Warning'
  },
  {
    value: 'YELLOW',
    label: 'Yellow Warning'
  },
  {
    value: 'AMBER',
    label: 'Amber Warning'
  },
  {
    value: 'RED',
    label: 'Red Warning'
  }
]

const form = ref<AssetSearchForm>({
  id: '',
  name: '',
  typeId: '',
  ownerId: '',
  capacityLitres: [0, 10000],
  material: '',
  status: '',
  installedAt: null,
  lastInspection: null,
  warningLevel: ''
})

const handleSearch = () => {
  const newObj: AssetSearchBody = {
    ...props.assetSearchBody,
    filters: assetConverFormToFilter(form.value)
  }
  emit('update:assetSearchBody', newObj)
  props.fetchTableData()
  visible.value = false
}

const clearFilters = () => {
  form.value = {
    id: '',
    name: '',
    typeId: '',
    ownerId: '',
    capacityLitres: [0, 10000],
    material: '',
    status: '',
    installedAt: null,
    lastInspection: null,
    warningLevel: ''
  }
  handleSearch()
}

const fuzzySearch = (input: string) => {
  const fuzzyForm = {
    id: input,
    name: '',
    typeId: '',
    ownerId: '',
    capacityLitres: [0, 10000],
    material: '',
    status: '',
    installedAt: null,
    lastInspection: null,
    warningLevel: ''
  }
  const obj: AssetSearchBody = {
    ...props.assetSearchBody,
    filters: assetConverFormToFilter(fuzzyForm)
  }
  emit('update:assetSearchBody', obj)
  const index = userStore.searchHistory.indexOf(input)
  if (index === -1) {
    userStore.searchHistory.unshift(input)
  } else {
    userStore.searchHistory.splice(index, 1)
    userStore.searchHistory.unshift(input)
  }
  props.fetchTableData()
  visible.value = false
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
    <div v-if="detail">
      <el-form :model="form" label-width="auto" label-position="left">
        <el-form-item label="Warning level">
          <el-select
            :teleported="false"
            v-model="form.warningLevel"
            placeholder="Select warning level"
            clearable
          >
            <el-option
              v-for="item in warningLevelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Id">
          <el-input v-model="form.id"></el-input>
        </el-form-item>
        <el-form-item label="Name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="Type">
          <el-select
            :teleported="false"
            v-model="form.typeId"
            placeholder="Select type"
            clearable
            class="select-style"
          >
            <el-option
              v-for="item in assetStore.typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Material">
          <el-select
            :teleported="false"
            v-model="form.material"
            placeholder="Select material"
            clearable
            class="select-style"
          >
            <el-option
              v-for="item in materialOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Status">
          <el-select
            :teleported="false"
            v-model="form.status"
            placeholder="Select status"
            clearable
            class="select-style"
          >
            <el-option
              v-for="item in statusOption"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="Capacity litres">
          <el-slider
            v-model="form.capacityLitres"
            range
            :max="10000"
          ></el-slider>
        </el-form-item>

        <el-form-item label="Installed at">
          <el-date-picker
            v-model="form.installedAt"
            type="daterange"
            unlink-panels
            range-separator="To"
            start-placeholder="Start date"
            end-placeholder="End date"
          />
        </el-form-item>

        <el-form-item label="Last inspection">
          <el-date-picker
            v-model="form.lastInspection"
            type="daterange"
            unlink-panels
            range-separator="To"
            start-placeholder="Start date"
            end-placeholder="End date"
          />
        </el-form-item>
      </el-form>

      <div style="margin-top: 20px">
        <el-button @click="handleSearch">Search</el-button>
        <el-button @click="clearFilters">Clear filters</el-button>
      </div>
    </div>
    <div v-else class="search-history">
      <span>Search history</span>
      <ul>
        <li
          v-for="(item, index) in userStore.searchHistory.slice(0, 5)"
          :key="index"
          @click="handleRowClick(item)"
        >
          {{ item }}
        </li>
      </ul>
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
