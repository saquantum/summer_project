<script setup lang="ts">
import type { AssetSearchBody, AssetSearchForm } from '@/types'
import { assetConverFormToFilter } from '@/utils/DataConversion'
import { Filter } from '@element-plus/icons-vue'
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'

const props = defineProps<{
  assetSearchBody: AssetSearchBody
  fetchTableData: () => void
}>()

const emit = defineEmits(['update:assetSearchBody'])

interface AssetTypeOption {
  value: string
  label: string
}

const assetTypeOptions: AssetTypeOption[] = [
  { value: 'type_001', label: 'Water Tank' },
  { value: 'type_002', label: 'Soakaway' },
  { value: 'type_003', label: 'Green Roof' },
  { value: 'type_004', label: 'Permeable Pavement' },
  { value: 'type_005', label: 'Swale' },
  { value: 'type_006', label: 'Retention Pond' },
  { value: 'type_007', label: 'Rain Garden' }
]

const visible = ref<boolean>(false)
const detail = ref<boolean>(false)
const assetId = ref<string | null>(null)
let lastType: string | null = null

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

interface TableRow {
  date: string
}

const tableData: TableRow[] = [
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' }
]

const handleRowClick = (row: TableRow) => {
  tags.value.push(row.date)
}

const tags = ref<string[]>([])
const input = ref<string>('')

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    console.log(input.value)
    fuzzySearch(input.value)
  }
  if (
    (e.key === 'Enter' || e.key === ',' || e.key === ' ') &&
    input.value.trim()
  ) {
    e.preventDefault()
    tags.value.push(input.value.trim())
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

const querySearch = (
  queryString: string,
  cb: (_results: { value: string }[]) => void
) => {
  const list = ['1', '2', '3', '4', '5']
  const result = list.map((item) => ({
    value: item
  }))
  cb(result)
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

const form = ref<AssetSearchForm>({
  id: '',
  name: '',
  typeId: '',
  ownerId: '',
  capacityLitres: [0, 10000],
  material: '',
  status: '',
  installedAt: null,
  lastInspection: null
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

const fuzzySearch = (input: string) => {
  console.log(input)
}
watch(
  [form],
  () => {
    console.log(111111111)
    if (!form.value.typeId) {
      if (lastType) {
        const index = tags.value.indexOf(lastType)
        if (index !== -1) tags.value.splice(index, 1)
      }
      return
    }
    const obj = assetTypeOptions.find(
      (item) => item.value === form.value.typeId
    )
    if (!obj) return
    if (lastType) {
      const index = tags.value.indexOf(lastType)
      if (index !== -1) tags.value.splice(index, 1)
    }
    tags.value.push(obj.label)
    lastType = obj.label
  },
  {
    deep: true
  }
)

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
      <el-form :model="form" label-width="auto">
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
              v-for="item in assetTypeOptions"
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

      <!-- do this later -->
      <div class="label">Id</div>
      <el-autocomplete
        v-model="assetId"
        :fetch-suggestions="querySearch"
        clearable
        placeholder="Please Input"
        :teleported="false"
      />
      <div class="label">test prop</div>
      <ButtonInput></ButtonInput>
      <div style="margin-top: 20px">
        <el-button @click="handleSearch">Search</el-button>
        <el-button>Clear filters</el-button>
      </div>
    </div>
    <div v-else>
      <span>Suggestion</span>
      <el-table
        :data="tableData"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="date" />
      </el-table>
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
</style>
