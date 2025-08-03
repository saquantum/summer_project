<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useGlobalLogout, useUserStore } from '@/stores/index.ts'
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
  <div>For your account safety, we need to verify your email</div>
  <div>
    OTP code will send to your email
    {{ userStore.user?.contactDetails?.email }}
  </div>
  <el-form
    :model="form"
    :rules="rules"
    ref="formRef"
    size="large"
    v-if="!resetFormVisible"
  >
    <el-form-item prop="code">
      <el-input v-model="form.code" placeholder="Please input OTP code">
      </el-input>
    </el-form-item>

    <el-form-item>
      <el-button @click="handleSendEmail" :disabled="sendDisabled">
        {{ sendDisabled ? `Send (${countdown})` : 'Send' }}
      </el-button>
      <el-button @click="handleVerify">Verify</el-button>
    </el-form-item>
  </el-form>

  <el-form
    :model="form"
    :rules="rules"
    ref="formRef"
    size="large"
    class="form-style"
    v-else
  >
    <el-form-item>
      <h1>Reset your password</h1>
    </el-form-item>

    <el-form-item prop="password">
      <el-input
        v-model="form.password"
        :prefix-icon="Lock"
        type="password"
        placeholder="Please input password"
      ></el-input>
    </el-form-item>
    <el-form-item prop="repassword">
      <el-input
        v-model="form.repassword"
        :prefix-icon="Lock"
        type="password"
        placeholder="Please input password again"
      ></el-input>
    </el-form-item>

    <el-form-item>
      <el-button @click="handleConfirm">Confirm</el-button>
    </el-form-item>
  </el-form>
</template>
