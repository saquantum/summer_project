<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
import { ref, nextTick } from 'vue'
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import {
  userGetEmailService,
  userEmailVerificationService,
  userResetPasswordService
} from '@/api/user'
import CodeUtil from '@/utils/codeUtil'
import { useRouter } from 'vue-router'
import type { FormItemRule } from 'element-plus'
const userStore = useUserStore()
const assetStore = useAssetStore()
const router = useRouter()

const form = ref({
  code: '',
  password: '',
  repassword: '',
  email: userStore.user?.assetHolder?.email || ''
})

const rules = {
  // customize rules here
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
    {
      pattern: /^\S{6,15}$/,
      message: 'password must between 6 to 15 characters',
      trigger: 'blur'
    },
    {
      validator: (
        rule: FormItemRule,
        value: string,
        callback: (error?: Error) => void
      ) => {
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
      message: 'Please input captcha',
      trigger: 'blur'
    },
    {
      min: 6,
      max: 6,
      message: 'Captcha must be exactly 6 characters',
      trigger: 'blur'
    },
    {
      pattern: /^[A-Za-z0-9]{6}$/,
      message: 'Captcha must contain only letters and numbers',
      trigger: 'blur'
    }
  ]
}

const resetFormVisible = ref(false)

const handleSendEmail = async () => {
  const res = await userGetEmailService(form.value.email)
  console.log(res)
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
    nextTick(() => {
      userStore.reset()
      assetStore.reset()
    })
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
      <el-button @click="handleVerify">Next</el-button>
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
