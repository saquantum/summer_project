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
    confirmDisabled.value = false
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
  <el-dialog
    v-model="visible"
    :title="title"
    style="width: 90%; margin: 80px auto; height: 150px; max-width: 600px"
    align-center
    class="confirm-dialog"
  >
    <slot>
      <span>{{ content }}</span>
    </slot>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          @click="handleCancel"
          data-test="cancel"
          class="styled-btn btn-cancel"
        >
          {{ cancelText || 'Cancel' }}
        </el-button>
        <el-button
          data-test="confirm"
          type="primary"
          :disabled="confirmDisabled"
          @click="handleConfirm"
          class="styled-btn"
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

<style scoped>
.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 12px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.btn-cancel:hover {
  transform: translateY(-1px);
  background-image: linear-gradient(
    to bottom,
    #782d2d 0%,
    #852e2e 10%,
    #903737 100%
  );
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}
</style>
