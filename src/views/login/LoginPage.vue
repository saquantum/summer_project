<script setup>
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import {
  userCheckEmailService,
  userCheckUIDService,
  userRegisterService
} from '@/api/user'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import CodeUtil from '@/utils/codeUtil'
const loginFormRef = ref()
const registerFormRef = ref()

const isRegister = ref(false)
const isRecover = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  repassword: '',
  captcha: '',
  email: ''
})

const passwordInput = ref(null)
const shiftFocusToPassword = () => {
  passwordInput.value?.focus()
}

const registerForm = ref({
  id: '',
  firstName: '',
  lastName: '',
  assetHolder: {
    name: 'default name',
    email: '',
    phone: '',
    address: {
      street: '',
      postcode: '',
      city: '',
      country: ''
    },
    contact_preferences: {
      email: true,
      phone: false,
      whatsapp: false,
      discord: false,
      post: false,
      telegram: false
    }
  }
})

const rules = {
  // customize rules here
  username: [
    { required: true, message: 'Please input username', trigger: 'blur' },
    {
      min: 5,
      max: 10,
      message: 'username must between 5 to 10 characters',
      trigger: 'blur'
    }
  ],
  id: [
    { required: true, message: 'Please input username', trigger: 'blur' },
    {
      min: 5,
      max: 10,
      message: 'username must between 5 to 10 characters',
      trigger: 'blur'
    },
    {
      validator: async (rule, value, callback) => {
        const res = await userCheckUIDService(value)
        // success means find a username called ${value}
        if (CodeUtil.isSuccess(res.code)) {
          callback(
            new Error(`Username ${value} is already exists, try a new one`)
          )
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
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
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error("Those passwords didn't match. Try again."))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  captcha: [
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
  ],
  'assetHolder.email': [
    { required: true, message: 'Please input email', trigger: 'blur' },
    {
      type: 'email',
      message: 'Please input a valid email address',
      trigger: ['blur', 'change']
    },
    {
      validator: async (rule, value, callback) => {
        const res = await userCheckEmailService(value)
        if (CodeUtil.isSuccess(res.code)) {
          callback(new Error('This email has already been used'))
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  'assetHolder.phone': [
    { required: true, message: 'Phone is required', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const phoneRegex = /^[0-9+\-()\s]{7,20}$/
        if (!phoneRegex.test(value)) {
          callback(new Error('Invalid phone number'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    },
    {
      validator: async (rule, value, callback) => {
        console.log('???')
        const res = await userCheckEmailService(value)
        console.log(res)
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const register = async () => {
  await registerFormRef.value.validate()
  const res = await userRegisterService(registerForm.value)
  console.log(res)
  ElMessage.success('success')
  isRegister.value = false
}

const router = useRouter()
const userStore = useUserStore()
const login = async () => {
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  try {
    await userStore.getUser(loginForm.value)
    ElMessage.success('success')
    router.push('/')
  } catch {
    loginForm.value.password = ''
    ElMessage.error('Username or password is incorrect')
  }
}

watch(isRegister, () => {
  loginForm.value = {
    username: '',
    password: '',
    repassword: ''
  }
})
</script>

<template>
  <div class="login-page">
    <div class="card-wrapper">
      <el-card class="glass-effect" style="width: 90%; max-width: 400px">
        <!-- Sign in form -->
        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          size="large"
          autocomplete="off"
          class="form-style"
          v-if="!isRegister && !isRecover"
        >
          <el-form-item>
            <h1>Sign in</h1>
          </el-form-item>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              :prefix-icon="User"
              placeholder="Please input username"
              @keydown.shift.enter="shiftFocusToPassword"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              ref="passwordInput"
              v-model="loginForm.password"
              name="password"
              :prefix-icon="Lock"
              type="password"
              placeholder="Please input password"
              @keydown.enter="login"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <div class="flex">
              <el-checkbox>Remember me</el-checkbox>
              <el-link
                type="primary"
                :underline="false"
                @click="isRecover = true"
                >Forget password?</el-link
              >
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              @click="login"
              class="button"
              type="primary"
              auto-insert-space
              >Sign in</el-button
            >
          </el-form-item>
          <el-form-item class="flex">
            <el-link type="info" :underline="false" @click="isRegister = true">
              Register →
            </el-link>
          </el-form-item>
        </el-form>

        <!-- Register form -->
        <el-form
          class="form-style"
          :model="registerForm"
          :rules="rules"
          ref="registerFormRef"
          size="large"
          autocomplete="off"
          v-else-if="isRegister && !isRecover"
        >
          <el-form-item>
            <h1>Register</h1>
          </el-form-item>
          <el-form-item prop="assetHolder.email">
            <el-input
              v-model="registerForm.assetHolder.email"
              :prefix-icon="Message"
              placeholder="Please input your email"
              style="width: 100%"
              type="email"
              autocomplete="email"
            ></el-input>
          </el-form-item>

          <el-form-item prop="assetHolder.phone">
            <el-input
              v-model="registerForm.assetHolder.phone"
              :prefix-icon="Phone"
              placeholder="Please input your phone number"
              style="width: 100%"
              type="phone"
              autocomplete="phone"
            ></el-input>
          </el-form-item>

          <el-form-item prop="id">
            <el-input
              v-model="registerForm.id"
              :prefix-icon="User"
              placeholder="Please input username"
              style="width: 100%"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="Please input password"
            ></el-input>
          </el-form-item>
          <el-form-item prop="repassword">
            <el-input
              v-model="registerForm.repassword"
              :prefix-icon="Lock"
              type="password"
              placeholder="Please input password again"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              @click="register"
              class="button"
              type="primary"
              auto-insert-space
            >
              Register
            </el-button>
          </el-form-item>
          <el-form-item class="flex">
            <el-link type="info" :underline="false" @click="isRegister = false">
              ← Back
            </el-link>
          </el-form-item>
        </el-form>
        <!-- recover form -->
        <el-form
          v-if="isRecover"
          :model="loginForm"
          :rules="rules"
          ref="form"
          size="large"
          autocomplete="off"
          class="form-style"
        >
          <el-form-item>
            <h1>Recover</h1>
          </el-form-item>
          <el-form-item prop="email">
            <el-input
              v-model="loginForm.email"
              :prefix-icon="User"
              placeholder="Enter your email address"
              type="email"
              autocomplete="email"
            />
          </el-form-item>

          <el-form-item prop="captcha">
            <el-input
              v-model="loginForm.captcha"
              :prefix-icon="Lock"
              placeholder="Enter captcha code"
              maxlength="6"
            />
          </el-form-item>

          <el-form-item>
            <el-button>Send email</el-button>
          </el-form-item>

          <el-link
            type="info"
            :underline="false"
            @click="((isRegister = false), (isRecover = false))"
          >
            ← Back to login
          </el-link>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  background: url('@/assets/login_bg.png') no-repeat center / cover;
}

.card-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.glass-effect {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
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
