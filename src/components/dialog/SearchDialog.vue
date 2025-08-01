<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['update:visible'])
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})
</script>

<template>
  <div>
    <el-dialog v-model="visible" title="Tips" width="500">
      <TestSearch></TestSearch>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">Cancel</el-button>
          <el-button type="primary" @click="visible = false">
            Confirm
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
