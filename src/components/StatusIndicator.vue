<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    default: 'OFF' // possible value：SUCCESS, YELLOW, AMBER, RED
  }
})

const statusClass = computed(() => {
  return `indicator--${props.status}`
})
</script>

<template>
  <div class="indicator" :class="statusClass"></div>
</template>

<style scoped>
.indicator {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #ccc;
}

.indicator--SUCCESS {
  background-color: #4caf50;
  box-shadow: 0 0 4px rgba(76, 175, 80, 0.4);
  border-color: #c8e6c9;
}

.indicator--YELLOW {
  background-color: #ffc107;
  box-shadow: 0 0 4px rgba(255, 193, 7, 0.4);
  border-color: #fff3cd;
}

.indicator--AMBER {
  background-color: #ff9800;
  box-shadow: 0 0 4px rgba(255, 152, 0, 0.4);
  border-color: #ffe0b2;
}

.indicator--RED {
  background-color: #f44336;
  box-shadow: 0 0 4px rgba(244, 67, 54, 0.4);
  border-color: #ffcdd2;
}

.indicator--OFF {
  background-color: #bdbdbd;
  box-shadow: none;
  border-color: #e0e0e0;
}
</style>
