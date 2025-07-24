<script setup lang="ts">
import { Lock, Message } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
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
const router = useRouter()
const form = ref({
  username: '',
  password: '',
  repassword: '',
  code: '',
  email: ''
})

const formRef = ref()

const codeVisible = ref(false)
const resetFormVisible = ref(false)
const rules = {
  password: passwordRules,
  repassword: createRepasswordRules(() => form.value.password || ''),
  code: codeRules
}

// reset password

const handleSendEmail = async () => {
  const res = await userGetEmailService(form.value.email)
  console.log(res)
  codeVisible.value = true
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
    const res = await userResetPasswordService(form.value)
    console.log(res)
    router.push('/login')
  } catch (e) {
    console.error(e)
  }
}
</script>

<template>
  <el-card class="glass-effect">
    <el-form
      :model="form"
      :rules="rules"
      ref="formRef"
      size="large"
      class="form-style"
      v-if="!resetFormVisible"
    >
      <el-form-item>
        <h1>Recover</h1>
      </el-form-item>
      <el-form-item prop="email">
        <el-input
          v-model="form.email"
          :prefix-icon="Message"
          placeholder="Enter your email address"
          type="email"
          autocomplete="email"
        />
      </el-form-item>

      <el-form-item prop="code" v-if="codeVisible">
        <el-input
          v-model="form.code"
          :prefix-icon="Lock"
          placeholder="Enter code code"
          maxlength="6"
        />
      </el-form-item>

      <el-form-item>
        <el-button @click="handleSendEmail">Send</el-button>
        <el-button @click="handleVerify" v-if="codeVisible">Verify</el-button>
      </el-form-item>

      <el-link type="info" :underline="false" @click="router.push('/login')">
        ← Back to login
      </el-link>
    </el-form>

    <el-form
      :model="form"
      :rules="rules"
      ref="formRef"
      size="large"
      autocomplete="off"
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

      <el-link type="info" underline="never" @click="router.push('/login')">
        ← Back to login
      </el-link>
    </el-form>
  </el-card>
</template>

<style scoped>
.glass-effect {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  width: 90%;
  max-width: 400px;
}

.form-style {
  width: 80%;
  margin: 0 auto;
}

.flex {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
