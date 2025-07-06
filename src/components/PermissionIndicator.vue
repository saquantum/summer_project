<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: Boolean,
    default: false // possible value：success, yellow, amber, red
  }
})

const statusClass = computed(() => {
  return `indicator--${props.status}`
})
</script>

<template>
  <el-tooltip effect="dark" content="Top Left prompts info" placement="bottom">
    <div class="indicator" :class="statusClass"></div>
  </el-tooltip>
</template>

<style scoped>
.indicator {
  background-color: rgb(48, 164, 108);
  border-radius: 9999px;
  width: 8px;
  height: 8px;
  box-sizing: border-box;
}

.indicator--true {
  background-color: rgb(48, 164, 108);
}

.indicator--false {
  background-color: rgb(229, 72, 77);
}
</style>
