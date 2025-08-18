<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useUserStore } from '@/stores/index.ts'
import { useGlobalLogout } from '@/utils/logout'
import {
  userGetEmailService,
  userEmailVerificationService,
  userResetPasswordService
} from '@/api/user'

import {
  codeRules,
  createRepasswordRules,
  passwordRules
} from '@/utils/formUtils'
import CodeUtil from '@/utils/codeUtil'
const userStore = useUserStore()

const { logout } = useGlobalLogout()

const formRef = ref()
const form = ref({
  code: '',
  password: '',
  repassword: '',
  email: userStore.user?.contactDetails?.email || ''
})

const rules = {
  password: passwordRules,
  repassword: createRepasswordRules(() => form.value.password || ''),
  code: codeRules
}

// count down for send button
const sendDisabled = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const resetFormVisible = ref(false)

// reset password
const handleSendEmail = async () => {
  try {
    await userGetEmailService(form.value.email)
    sendDisabled.value = true
    countdown.value = 30
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        sendDisabled.value = false
        clearInterval(timer!)
        timer = null
      }
    }, 1000)
  } catch (e) {
    console.error(e)
  }
}

const handleVerify = async () => {
  try {
    await formRef.value.validateField('code')
  } catch {
    return
  }

  try {
    const res = await userEmailVerificationService(form.value)
    if (CodeUtil.isBusinessError(res.code)) {
      throw new Error('Invalid code')
    }
    resetFormVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleConfirm = async () => {
  try {
    await userResetPasswordService(form.value)
    logout()
  } catch (e) {
    console.error(e)
  }
}
</script>

<template>
  <div class="sheet-body">
    <template v-if="!resetFormVisible">
      <div class="otp-cards">
        <div class="otp-card intro-card" shadow="never">
          <div class="intro">
            For your account safety, we need to verify your email
          </div>
          <div class="intro sub">
            OTP code will be sent to: {{ form.email }}
          </div>
          <div class="card-actions-left">
            <el-button
              class="pill-btn ghost"
              @click="handleSendEmail"
              :disabled="sendDisabled"
            >
              {{ sendDisabled ? `Send (${countdown})` : 'Send code' }}
            </el-button>
          </div>
        </div>

        <el-card class="otp-card form-card" shadow="never">
          <div class="field-label-2">Code</div>

          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            size="large"
            class="otp-form"
          >
            <div class="otp-inline">
              <el-form-item prop="code" class="otp-item">
                <el-input
                  v-model="form.code"
                  placeholder="Enter the OTP code"
                />
              </el-form-item>
              <el-button class="pill-btn primary" @click="handleVerify"
              >Verify</el-button
              >
            </div>
          </el-form>
        </el-card>
      </div>
    </template>
    <template v-else>
      <div class="field-label-2">New password</div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            type="password"
          />
        </el-form-item>

        <div class="field-label-2">Confirm new password</div>
        <el-form-item prop="repassword">
          <el-input
            v-model="form.repassword"
            :prefix-icon="Lock"
            type="password"
          />
        </el-form-item>

        <div class="sheet-actions">
          <el-button class="pill-btn primary" @click="handleConfirm"
          >Save</el-button
          >
        </div>
      </el-form>
    </template>
  </div>
</template>

<style scoped>
.sheet-body {
  padding-top: 4px;
}

.field-label-2 {
  margin: 10px 6px 6px;
  font-weight: 700;
  color: #045492;
}
.sheet-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}
.pill-btn {
  border-radius: 999px;
  padding: 8px 20px;
  border: 0;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
  font-weight: 700;
  font-size: 15px;
}
.pill-btn.primary {
  color: #fff;
  border: 1px solid #fff;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.8) 120%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}
.pill-btn.ghost {
  color: #39435b;
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
}

.sheet-body {
  padding-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 0 14px;
  height: 44px;
  border: 1px solid #dde5e8;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease,
    background-color 0.15s ease;
}
:deep(.el-input__wrapper:hover) {
  border-color: #cfd8df;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(155, 183, 212, 0.25);
  background: #fff;
}

.otp-cards {
  display: grid;
  grid-template-columns: 4fr 5fr;
  gap: 16px;
}

.otp-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.intro-card {
  background: transparent;
  border: none;
}

.form-card {
  height: 150px;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.intro {
  font-size: 20px;
  margin-bottom: 10px;
  line-height: 1.6;
  color: #333;
}
.intro.sub {
  font-size: 16px;
  opacity: 0.8;
}

.card-actions-left {
  justify-content: flex-start;
  padding-left: 10px;
  padding-top: 10px;
}

.field-label-2 {
  margin: 2px 0 8px;
  font-weight: 700;
  color: #045492;
}
.otp-form :deep(.el-form-item) {
  margin: 0;
}
.otp-form :deep(.el-form-item__error) {
  position: static;
  margin-top: 6px;
}

:deep(.el-input__wrapper) {
  height: 44px;
  border-radius: 12px;
  border: 1px solid #dde5e8;
}

.otp-inline {
  display: grid;
  grid-template-columns: 1fr 140px;
  column-gap: 12px;
  align-items: center;
  margin-bottom: 24px;
}

.otp-inline :deep(.el-form-item) {
  margin: 0;
  position: relative;
}

.otp-inline :deep(.el-form-item__error) {
  position: absolute;
  left: 0;
  bottom: -20px;
  line-height: 1.2;
}

:deep(.el-input__wrapper) {
  height: 44px;
}
.pill-btn {
  height: 44px;
}

@media (max-width: 768px) {
  .otp-cards {
    grid-template-columns: 1fr;
  }
  .card-actions-left,
  .card-actions-right {
    margin-top: 12px;
  }
}

@media (max-width: 480px) {
  .sheet {
    padding: 8px 12px 20px;
  }
  .sheet-title {
    font-size: 18px;
  }

  .sheet {
    padding: 12px 16px 24px;
    background: #fff;
  }
  .sheet-header {
    display: grid;
    grid-template-columns: 40px 1fr auto;
    align-items: center;
    gap: 8px;
    padding: 10px 8px;
    border-bottom: 1px solid #eee;
  }
  .sheet-title {
    text-align: center;
    font-weight: 800;
    font-size: 20px;
  }
  .sheet-body {
    padding: 18px 8px 0;
  }
  .intro {
    font-size: 14px;
    margin: 8px 6px;
    color: #333;
  }
  .intro.sub {
    opacity: 0.7;
  }
  .field-label-2 {
    margin: 14px 6px 8px;
    font-weight: 700;
    color: #045492;
  }

  .sheet-header .back-btn,
  .sheet-header .cancel-btn {
    color: #045492;
    font-weight: bold;
  }
  .sheet-header .back-btn :deep(svg) {
    width: 20px;
    height: 20px;
  }

  .sheet-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 16px;
  }
  .pill-btn {
    border-radius: 999px;
    padding: 8px 20px;
    border: 0;
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
    font-weight: 700;
    font-size: 15px;
  }
  .pill-btn.primary {
    color: #fff;
    border: 1px solid #fff;
    text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
    background-image: linear-gradient(
      180deg,
      #e4dfd8 0%,
      #9bb7d4 60%,
      rgba(58, 78, 107, 0.8) 120%
    );
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
  }
  .pill-btn.ghost {
    color: #39435b;
    text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
    border: 1px solid #fff;
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
    background: linear-gradient(
      180deg,
      #f0e6d2 0%,
      rgba(125, 140, 163, 0.44) 100%
    );
  }

  :deep(.el-input__wrapper) {
    border-radius: 14px;
    background: #fff;
    border: 1px solid #dde5e8;
    box-shadow: none;
  }
}
</style>
