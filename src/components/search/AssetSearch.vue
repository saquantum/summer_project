<script setup lang="ts">
import { useAssetStore, useUserStore } from '@/stores'
import type { AssetSearchBody, AssetSearchForm } from '@/types'
import { assetConverFormToFilter } from '@/utils/formUtils'
import { Filter } from '@element-plus/icons-vue'
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import type { CSSProperties } from 'vue'

const props = defineProps<{
  assetSearchBody: AssetSearchBody
  fetchTableData: () => void
}>()

const assetStore = useAssetStore()
const emit = defineEmits(['update:assetSearchBody'])

const visible = ref<boolean>(false)
const detail = ref<boolean>(false)

// References for container, dropdown, and input
const containerRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)

// Dropdown position state
const dropdownStyle = ref<CSSProperties>({
  position: 'absolute',
  top: '0px',
  left: '0px',
  width: '375px',
  zIndex: '2000',
  display: 'none'
})

// Calculate dropdown position
const updateDropdownPosition = async () => {
  await nextTick()
  if (!containerRef.value || !dropdownRef.value) return

  const containerRect = containerRef.value.getBoundingClientRect()
  const dropdownHeight = dropdownRef.value.offsetHeight
  const viewportHeight = window.innerHeight

  // Check if there is enough space to show dropdown below
  const spaceBelow = viewportHeight - containerRect.bottom
  const shouldShowAbove =
    spaceBelow < dropdownHeight && containerRect.top > dropdownHeight

  dropdownStyle.value = {
    position: 'fixed',
    top: shouldShowAbove
      ? `${containerRect.top - dropdownHeight}px`
      : `${containerRect.bottom}px`,
    left: `${containerRect.left}px`,
    width: `${Math.max(containerRect.width, 375)}px`,
    zIndex: '2000',
    display: visible.value ? 'block' : 'none'
  }
}

// Close dropdown when clicking outside
const handleClickOutside = (e: MouseEvent) => {
  if (
    containerRef.value &&
    dropdownRef.value &&
    !containerRef.value.contains(e.target as Node) &&
    !dropdownRef.value.contains(e.target as Node)
  ) {
    visible.value = false
  }
}

// Listen for window resize and scroll events
const handleResize = () => {
  if (visible.value) {
    updateDropdownPosition()
  }
}

const handleScroll = () => {
  if (visible.value) {
    updateDropdownPosition()
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

const focusInput = async () => {
  inputRef.value?.focus()
  visible.value = true
  detail.value = false
  await updateDropdownPosition()
}

const handleFilterClick = async () => {
  if (detail.value === true && visible.value === true) {
    visible.value = false
  } else {
    detail.value = true
    visible.value = true
    await updateDropdownPosition()
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
  console.log(form.value)
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

// Watch 'visible' changes and update dropdown style
watch(visible, async (newVal) => {
  if (newVal) {
    await updateDropdownPosition()
  } else {
    dropdownStyle.value.display = 'none'
  }
})

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
  window.addEventListener('resize', handleResize)
  window.addEventListener('scroll', handleScroll, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleScroll, true)
})

defineExpose({
  form
})
</script>

<template>
  <div class="search-container">
    <!-- Search input container -->
    <div ref="containerRef" class="tag-input-wrapper">
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
      <el-button @click="handleFilterClick" class="seamless-button">
        <el-icon><Filter /></el-icon>
      </el-button>
    </div>

    <!-- Custom dropdown - using Teleport to mount to body -->
    <Teleport to="body">
      <div ref="dropdownRef" class="custom-dropdown" :style="dropdownStyle">
        <!-- Detailed filter form -->
        <div v-if="detail" class="dropdown-content">
          <el-form :model="form" label-width="auto" label-position="left">
            <el-form-item label="Warning level">
              <el-select
                v-model="form.warningLevel"
                placeholder="Select warning level"
                clearable
                :teleported="false"
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
                v-model="form.typeId"
                placeholder="Select type"
                clearable
                class="select-style"
                :teleported="false"
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
                v-model="form.material"
                placeholder="Select material"
                clearable
                class="select-style"
                :teleported="false"
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
                v-model="form.status"
                placeholder="Select status"
                clearable
                class="select-style"
                :teleported="false"
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
              <DatePicker :teleported="true" v-model="form.installedAt" />
            </el-form-item>

            <el-form-item label="Last inspection">
              <DatePicker :teleported="true" v-model="form.lastInspection" />
            </el-form-item>
          </el-form>

          <div style="margin-top: 20px">
            <el-button @click="handleSearch">Search</el-button>
            <el-button @click="clearFilters">Clear filters</el-button>
          </div>
        </div>

        <!-- Search history -->
        <div v-else class="search-history dropdown-content">
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
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.search-container {
  position: relative;
}

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

/* Custom dropdown styles */
/* Desktop: dropdown auto-sizes to content, no scrollbars unless needed */
.custom-dropdown {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  max-height: none;
  min-width: 375px;
  width: auto;
  overflow: visible !important;
}

.dropdown-content {
  padding: 12px;
  max-height: none;
  overflow: visible;
}

.search-history {
  margin-top: 0;
  padding: 12px;
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

.label {
  margin-top: 5px;
  margin-bottom: 5px;
}

@media (max-width: 768px) {
  .custom-dropdown {
    left: 0 !important;
    top: 0 !important;
    width: 100vw !important;
    min-width: 100vw !important;
    max-width: 100vw !important;
    height: 100vh !important;
    max-height: 100vh !important;
    border-radius: 0 !important;
    border: none !important;
    box-shadow: none !important;
    z-index: 9999 !important;
    overflow-x: hidden !important;
  }
  .dropdown-content {
    max-height: calc(100vh - 24px);
    overflow-y: auto;
    overflow-x: hidden;
  }
}
</style>
