<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  title?: string
  content?: string
  confirmText?: string
  cancelText?: string
  countdownDuration?: number // seconds
}>()
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(props.modelValue)
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)
watch(visible, (val) => {
  emit('update:modelValue', val)
})

const confirmDisabled = ref(true)
const countdown = ref(props.countdownDuration ?? 5)
let timer: ReturnType<typeof setInterval> | null = null

watch(visible, (val) => {
  if (val) {
    countdown.value = props.countdownDuration ?? 5
    if (countdown.value === 0) {
      confirmDisabled.value = false
      return
    }

    confirmDisabled.value = true
    if (timer) clearInterval(timer)
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        confirmDisabled.value = false
        if (timer) {
          clearInterval(timer)
          timer = null
        }
      }
    }, 1000)
  } else {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    confirmDisabled.value = true
  }
})

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
const handleConfirm = () => {
  visible.value = false
  emit('confirm')
}
</script>

<template>
  <el-dialog v-model="visible" :title="title">
    <slot>
      <span>{{ content }}</span>
    </slot>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" data-test="cancel">
          {{ cancelText || 'Cancel' }}
        </el-button>
        <el-button
          data-test="confirm"
          type="primary"
          :disabled="confirmDisabled"
          @click="handleConfirm"
        >
          {{
            confirmDisabled
              ? `${confirmText || 'Confirm'} (${countdown})`
              : confirmText || 'Confirm'
          }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped></style>
