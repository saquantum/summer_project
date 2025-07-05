<script setup>
import { Lock, Message } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  userGetEmailService,
  userEmailVerificationService,
  userResetPasswordService
} from '@/api/user'
import CodeUtil from '@/utils/codeUtil'
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
  password: [
    {
      required: true,
      message: 'Please input password',
      trigger: 'blur'
    }
    // {
    //   pattern: /^\S{6,15}$/,
    //   message: 'password must between 6 to 15 characters',
    //   trigger: 'blur'
    // }
  ],
  repassword: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    // {
    //   pattern: /^\S{6,15}$/,
    //   message: 'password must between 6 to 15 characters',
    //   trigger: 'blur'
    // },
    {
      validator: (rule, value, callback) => {
        if (value !== form.value.password) {
          callback(new Error("Those passwords didn't match. Try again."))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    {
      required: true,
      message: 'Please input code',
      trigger: 'blur'
    },
    {
      min: 6,
      max: 6,
      message: 'Code must be exactly 6 characters',
      trigger: 'blur'
    },
    {
      pattern: /^[A-Za-z0-9]{6}$/,
      message: 'Code must contain only letters and numbers',
      trigger: 'blur'
    }
  ]
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
    if (CodeUtil.isSuccess(res.code)) {
      resetFormVisible.value = true
    }
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
  <el-form
    :model="form"
    :rules="rules"
    ref="formRef"
    size="large"
    autocomplete="off"
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
      <el-button @click="handleSendEmail">Send email</el-button>
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
</template>

<style scoped>
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
