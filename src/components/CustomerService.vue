<script setup>
import { ref } from 'vue'
import { ChatDotSquare } from '@element-plus/icons-vue'

const popoverVisible = ref(false)
</script>

<template>
  <el-popover
    v-model:visible="popoverVisible"
    placement="top"
    width="260"
    trigger="click"
  >
    <template #reference>
      <div class="chat-float">
        <el-button type="primary" size="large" circle>
          <el-icon><ChatDotSquare /></el-icon>
        </el-button>
      </div>
    </template>

    <div class="support-content">
      <h4 style="margin: 0 0 10px 0">Support</h4>
      <p style="margin: 0">How can we help you?</p>
    </div>
  </el-popover>
</template>

<style scoped>
.chat-float {
  position: fixed;
  right: 50px;
  bottom: 40px;
  z-index: 9999;
}

.support-content {
  padding: 5px;
}
</style>
