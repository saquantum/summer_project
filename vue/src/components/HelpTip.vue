<script setup lang="ts">
import { QuestionFilled } from '@element-plus/icons-vue'
import { ref } from 'vue'

interface Props {
  title?: string
  content?: string
  width?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Help',
  content: 'Click for more information',
  width: '400px'
})

const visible = ref(false)

const showTip = () => {
  visible.value = true
}
</script>

<template>
  <el-icon class="question-icon" @click="showTip" :title="'Click for help'">
    <QuestionFilled />
  </el-icon>

  <el-dialog
    v-model="visible"
    :title="props.title"
    :width="props.width"
    destroy-on-close
  >
    <div class="tip-content">
      <slot>
        <p>{{ props.content }}</p>
      </slot>
    </div>
    <template #footer>
      <el-button type="primary" @click="visible = false"> Got it </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.question-icon {
  color: #909399;
  cursor: pointer;
  font-size: 16px;
  margin-left: 4px;
  transition: color 0.3s ease;
}

.question-icon:hover {
  color: #409eff;
}

.tip-content {
  line-height: 1.6;
  color: #606266;
}

.tip-content p {
  margin: 0 0 12px 0;
}

.tip-content p:last-child {
  margin-bottom: 0;
}
</style>
