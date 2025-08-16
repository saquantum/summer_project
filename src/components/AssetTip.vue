<script setup lang="ts">
import { QuestionFilled } from '@element-plus/icons-vue'

const visible = defineModel('visible')
</script>

<template>
  <el-icon><QuestionFilled /></el-icon>

  <el-dialog v-model="visible" title="How to draw polygon" width="500">
    <span
      >A multipolygon is consist of different polygons, and a polygon is consist
      of different rings.</span
    >
    <template #footer>
      <el-button type="primary" @click="visible = false"> Close </el-button>
    </template>
  </el-dialog>
</template>
