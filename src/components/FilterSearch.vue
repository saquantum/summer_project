<script setup>
import { Filter } from '@element-plus/icons-vue'
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
const assetTypeOptions = [
  { value: 'type_001', label: 'Water Tank' },
  { value: 'type_002', label: 'Soakaway' },
  { value: 'type_003', label: 'Green Roof' },
  { value: 'type_004', label: 'Permeable Pavement' },
  { value: 'type_005', label: 'Swale' },
  { value: 'type_006', label: 'Retention Pond' },
  { value: 'type_007', label: 'Rain Garden' }
]

const visible = ref(false)
const detail = ref(false)
const assetType = ref()
const assetId = ref()
let lastType

const popoverRef = ref(null)
const referenceRef = ref(null)

const handleClickOutside = (e) => {
  console.log(visible.value)
  const popoverEl = popoverRef.value?.popperRef?.contentRef
  const referenceEl = referenceRef.value.$el
  const dropdownEl = document.querySelector('.el-select-dropdown') // select 的弹窗
  if (
    !popoverEl.contains(e.target) &&
    !referenceEl.contains(e.target) &&
    (!dropdownEl || !dropdownEl.contains(e.target)) &&
    !inputRef.value.contains(e.target)
  ) {
    visible.value = false
    console.log('trigger')
  }
  console.log(visible.value)
}

const handleSelectVisibleChange = () => {}

const handleSelect = () => {}

const tableData = [
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' },
  { date: '1' }
]

const handleRowClick = (row) => {
  console.log(row)
  tags.value.push(row.date)
}
// custom input
const tags = ref([])
const input = ref('')
const inputRef = ref()

const handleKeydown = (e) => {
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

const removeTag = (index) => {
  tags.value.splice(index, 1)
}

const focusInput = () => {
  inputRef.value.focus()
  visible.value = true
  detail.value = false
}

const querySearch = (queryString, cb) => {
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

const material = ref()
const materialOption = [
  { label: 'Steel', value: 'Steel' },
  { label: 'Concrete', value: 'Concrete' },
  { label: 'Plastic', value: 'Plastic' },
  { label: 'Composite', value: 'Composite' }
]

const status = ref()
const statusOption = [
  { label: 'inactive', value: 'inactive' },
  { label: 'active', value: 'active' },
  { label: 'maintenance', value: 'maintenance' }
]

// slide bar
const capacityLitres = ref([0, 10000])

// installed at
const installedAt = ref()

const lastInspection = ref()

watch([assetType], () => {
  if (!assetType.value) {
    tags.value.splice(tags.value.indexOf(lastType))
    return
  }
  const obj = assetTypeOptions.find((item) => item.value === assetType.value)
  tags.value.splice(tags.value.indexOf(obj.label))
  tags.value.push(obj.label)
  lastType = obj.label
})

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
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
    <div v-if="detail === true">
      <div class="label">Type</div>
      <el-select
        :teleported="false"
        @visible-change="handleSelectVisibleChange"
        v-model="assetType"
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
      <div class="label">Material</div>
      <el-select
        :teleported="false"
        @visible-change="handleSelectVisibleChange"
        v-model="material"
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
      <div class="label">Status</div>
      <el-select
        :teleported="false"
        @visible-change="handleSelectVisibleChange"
        v-model="status"
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
      <div class="label">Id</div>
      <el-autocomplete
        v-model="assetId"
        :fetch-suggestions="querySearch"
        clearable
        placeholder="Please Input"
        @select="handleSelect"
        :teleported="false"
      />
      <div class="label">test prop</div>
      <ButtonInput></ButtonInput>
      <div class="label">Capacity litres</div>
      <el-slider v-model="capacityLitres" range :max="10000"></el-slider>
      <div class="label">Installed at</div>
      <el-date-picker
        v-model="installedAt"
        type="daterange"
        unlink-panels
        range-separator="To"
        start-placeholder="Start date"
        end-placeholder="End date"
        :shortcuts="shortcuts"
      />
      <div class="label">Last inspection</div>
      <el-date-picker
        v-model="lastInspection"
        type="daterange"
        unlink-panels
        range-separator="To"
        start-placeholder="Start date"
        end-placeholder="End date"
        :shortcuts="shortcuts"
      />
      <div style="margin-top: 20px">
        <el-button>Search</el-button>
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
