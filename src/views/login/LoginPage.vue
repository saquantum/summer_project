<script setup>
import { User, Lock } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import { userRegisterService } from '@/api/user'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
const form = ref()
const isRegister = ref(true)
const formModel = ref({
  username: '',
  password: '',
  repassword: ''
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
        if (value !== formModel.value.password) {
          callback(new Error("Those passwords didn't match. Try again."))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const register = async () => {
  await form.value.validate()
  await userRegisterService(formModel.value)
  ElMessage.success('success')
  isRegister.value = false
}

const router = useRouter()
const userStore = useUserStore()
const login = async () => {
  try {
    await form.value.validate()
  } catch {
    return
  }

  try {
    await userStore.getUser(formModel.value)
    ElMessage.success('success')
    router.push('/')
  } catch {
    formModel.value.password = ''
    ElMessage.error('Username or password is incorrect')
  }
}

watch(isRegister, () => {
  formModel.value = {
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
          :model="formModel"
          :rules="rules"
          ref="form"
          size="large"
          autocomplete="off"
          class="form-style"
          v-if="isRegister"
        >
          <el-form-item>
            <h1>Sign in</h1>
          </el-form-item>
          <el-form-item prop="username">
            <el-input
              v-model="formModel.username"
              :prefix-icon="User"
              placeholder="Please input username"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="formModel.password"
              name="password"
              :prefix-icon="Lock"
              type="password"
              placeholder="Please input password"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <div class="flex">
              <el-checkbox>Remember me</el-checkbox>
              <el-link type="primary" :underline="false"
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
          :model="formModel"
          :rules="rules"
          ref="form"
          size="large"
          autocomplete="off"
          v-else
        >
          <el-form-item>
            <h1>Register</h1>
          </el-form-item>
          <el-form-item prop="username">
            <el-input
              v-model="formModel.username"
              :prefix-icon="User"
              placeholder="Please input username"
              style="width: 100%"
            ></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="formModel.password"
              :prefix-icon="Lock"
              type="password"
              placeholder="Please input password"
            ></el-input>
          </el-form-item>
          <el-form-item prop="repassword">
            <el-input
              v-model="formModel.repassword"
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
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  background: url('@/assets/login_bg.png') no-repeat center / cover;
  .form {
    display: flex;
    flex-direction: column;
    justify-content: center;
    user-select: none;
    .title {
      margin: 0 auto;
    }
    .button {
      width: 100%;
    }
  }
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
