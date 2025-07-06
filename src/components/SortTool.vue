<script setup lang="ts">
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'
interface SortItem {
  prop: string
  order: 'ascending' | 'descending'
}

interface ColumnItem {
  prop: string
  label: string
}

const props = defineProps<{
  multiSort: SortItem[]
  columns: ColumnItem[]
  fetchTableData: () => void
}>()

const emit = defineEmits(['update:multiSort'])

const getColumnLabel = (prop: string): string | undefined => {
  const obj = props.columns.find((item) => item.prop === prop)
  return obj?.label
}

const removeSortColumn = (prop: string): void => {
  const index = props.multiSort.findIndex((item) => item.prop === prop)
  if (index !== -1) {
    const newSort = props.multiSort.filter((item) => item.prop !== prop)
    emit('update:multiSort', newSort)
    props.fetchTableData()
  }
}

const clearAllSort = (): void => {
  emit('update:multiSort', [])
  props.fetchTableData()
}
</script>

<template>
  <div v-if="props.multiSort.length > 0" class="sort-status">
    <span>Current Sort: </span>
    <el-tag
      v-for="(sort, index) in props.multiSort"
      :key="sort.prop"
      :type="index === 0 ? 'primary' : 'info'"
      size="small"
      closable
      @close="removeSortColumn(sort.prop)"
    >
      {{ getColumnLabel(sort.prop) }}
      <el-icon>
        <component :is="sort.order === 'ascending' ? ArrowUp : ArrowDown" />
      </el-icon>
    </el-tag>
    <el-button size="small" text @click="clearAllSort">Clear Sort</el-button>
  </div>
</template>

<style scoped>
.sort-status {
  display: flex;
  align-items: center;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  height: 100%;
}

.sort-status .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}
</style>
