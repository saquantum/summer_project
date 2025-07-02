<script setup>
const props = defineProps({
  multiSort: Array,
  columns: Array,
  fetchTableData: Function
})

console.log(props.multiSort)

const emit = defineEmits(['update:multiSort'])

const getColumnLabel = (prop) => {
  console.log(props.multiSort)
  const obj = props.columns.find((item) => item.prop === prop)
  return obj.label
}

const removeSortColumn = (prop) => {
  const index = props.multiSort.findIndex((item) => item.prop === prop)
  if (index !== -1) {
    const newSort = props.multiSort.filter((item) => item.prop !== prop)
    emit('update:multiSort', newSort)
    props.fetchTableData()
  }
}

const clearAllSort = () => {
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
      <el-icon
        ><component :is="sort.order === 'ascending' ? ArrowUp : ArrowDown"
      /></el-icon>
    </el-tag>
    <el-button size="small" text @click="clearAllSort">Clear Sort</el-button>
  </div>
</template>

<style scoped>
.sort-status {
  margin-bottom: 16px;
  padding: 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.sort-status .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}
</style>
