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
const userStore = useUserStore()

const { logout } = useGlobalLogout()

const form = ref({
  code: '',
  password: '',
  repassword: '',
  email: userStore.user?.assetHolder?.email || ''
})

const rules = {
  password: passwordRules,
  repassword: createRepasswordRules(() => form.value.password || ''),
  code: codeRules
}

const resetFormVisible = ref(false)

const handleSendEmail = async () => {
  try {
    await userGetEmailService(form.value.email)
  } catch (e) {
    console.error(e)
  }
}

const handleVerify = async () => {
  try {
    const res = await userEmailVerificationService(form.value)
    console.log(res)
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
    {{ userStore.user?.assetHolder?.email }}
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
      <el-button @click="handleSendEmail">Send OTP code</el-button>
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
