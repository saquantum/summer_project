<script setup>
import { ref } from 'vue'
const props = defineProps({
  asset: Object
})
const displayData = ref()

displayData.value = [
  { label: 'ID', value: props.asset.id },

  { label: 'Type', value: props.asset.type },
  { label: 'Capacity litres', value: props.asset.capacityLitres },
  {
    label: 'Material',
    value: props.asset.material
  },
  {
    label: 'status',
    value: props.asset.status
  },
  { label: 'Installed at', value: props.asset.installedAt },
  { label: 'Last inspection', value: props.asset.lastInspection }
]
console.log(displayData)
</script>

<template>
  <el-card class="asset-card" shadow="hover" v-if="props.asset">
    <template #header>
      <div class="card-header">
        <h3
          class="asset-title"
          :class="{
            'warning-low': !props.warningLevel,
            'warning-medium': props.warningLevel === 'YELLOW',
            'warning-high': props.warningLevel === 'AMBER',
            'warning-severe': props.warningLevel === 'RED'
          }"
        >
          {{ asset.name || 'Asset Name' }}
        </h3>
        <StatusIndicator :status="status" />
      </div>
    </template>

    <div>
      <el-descriptions :column="2">
        <el-descriptions-item
          v-for="(item, index) in displayData"
          :key="index"
          :label="item.label"
          class-name="custom-"
          label-class-name="custom-label"
        >
          <span>{{ item.value }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-button>Edit</el-button>
      <el-button type="danger">Delete</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.asset-card {
  width: 350px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.asset-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: grid;
  grid-template-columns: 1fr auto;
  column-gap: 8px;
}

.asset-title {
  /* hide overflow text*/
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  line-height: 1.2;
  height: 1.2em;
}
</style>
