<script setup lang="ts">
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import { ref, watch } from 'vue'
import { userRegisterService } from '@/api/user'
import { useRouter } from 'vue-router'
import { useAssetStore, useUserStore } from '@/stores/index.ts'

import { ElMessage } from 'element-plus'
import type { LoginForm } from '@/types'
import {
  createRepasswordRules,
  emailRules,
  firstNameRules,
  lastNameRules,
  passwordRules,
  phoneRules,
  trimForm,
  usernameRules
} from '@/utils/formUtils'

const router = useRouter()
const userStore = useUserStore()
const assetStore = useAssetStore()

const loginFormRef = ref()
const registerFormRef = ref()

const isRegister = ref(false)

const loginForm = ref<LoginForm>({
  username: '',
  password: '',
  email: ''
})

const passwordInput = ref<HTMLInputElement | null>(null)

const shiftFocusToPassword = () => {
  passwordInput.value?.focus()
}

// register
const currentStep = ref(1)
const openSteps = ref(['1'])
const stepStatus = ref({
  1: false,
  2: false,
  3: false
})

const editingStep = ref<number | null>(null)

const registerForm = ref({
  id: '',
  firstName: '',
  lastName: '',
  name: '',
  email: '',
  phone: '',
  password: '',
  repassword: ''
})

const rules = {
  // customize rules here
  username: [
    { required: true, message: 'Please input username', trigger: 'blur' }
  ],
  id: usernameRules,
  password: passwordRules,
  repassword: createRepasswordRules(() => registerForm.value.password || ''),
  firstName: firstNameRules,
  lastName: lastNameRules,
  email: emailRules,
  phone: phoneRules
}

const login = async () => {
  // form validation
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  try {
    // get importance asset value after login
    await userStore.getUser(loginForm.value)
    await assetStore.getAssetTypes()
    router.push('/')
  } catch (e) {
    loginForm.value.password = ''
    console.error(e)
  }
}

// register function
const confirmEmail = async () => {
  try {
    await registerFormRef.value.validateField('email')
  } catch {
    return
  }
  stepStatus.value[1] = true
  currentStep.value = 2
  editingStep.value = null
  if (!openSteps.value.includes('2')) openSteps.value.push('2')
}

const goToStep3 = async () => {
  const valid = await registerFormRef.value.validateField(['id', 'phone'])
  if (valid) {
    stepStatus.value[2] = true
    currentStep.value = 3
    editingStep.value = null
    openSteps.value = ['1', '2', '3']
  }
}

const editStep = (step: number) => {
  currentStep.value = step
  editingStep.value = step
  openSteps.value = [String(step)]
}

const getStepTitleClass = (step: number) => {
  return {
    'step-header-disabled':
      editingStep.value !== null && editingStep.value !== step
  }
}

const resetRegister = () => {
  registerForm.value = {
    id: '',
    firstName: '',
    lastName: '',
    name: '',
    email: '',
    phone: '',
    password: '',
    repassword: ''
  }

  currentStep.value = 1
  openSteps.value = ['1']
  stepStatus.value = {
    1: false,
    2: false,
    3: false
  }
}

const register = async () => {
  // form validation
  trimForm(registerForm.value)
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }

  // register and reset value
  try {
    registerForm.value.name = `${registerForm.value.firstName} ${registerForm.value.lastName}`
    await userRegisterService(registerForm.value)
    ElMessage.success('success')
    isRegister.value = false
    resetRegister()
  } catch (e) {
    console.error(e)
  }
}

watch(isRegister, () => {
  loginForm.value = {
    username: '',
    password: '',
    email: ''
  }

  resetRegister()
})

defineExpose({ currentStep })
</script>

<template>
  <el-card class="glass-effect">
    <!-- Sign in form -->
    <el-form
      :model="loginForm"
      :rules="rules"
      ref="loginFormRef"
      size="large"
      autocomplete="off"
      class="form-style"
      v-if="!isRegister"
    >
      <!-- Sign in-->
      <el-form-item class="form-heading">
        <div class="form-title-group">
          <h1 class="form-title">SIGN IN</h1>
          <p class="form-subtitle">Please enter your details</p>
        </div>
      </el-form-item>

      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          :prefix-icon="User"
          placeholder="Username"
          @keydown.enter="shiftFocusToPassword"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          ref="passwordInput"
          v-model="loginForm.password"
          :prefix-icon="Lock"
          type="password"
          placeholder="Password"
          @keydown.enter="login"
        />
      </el-form-item>

      <el-form-item>
        <el-button class="button" type="primary" @click="login">
          Sign in
        </el-button>
      </el-form-item>

      <el-form-item>
        <div class="flex">
          <el-checkbox>Remember me</el-checkbox>
          <el-link
            type="primary"
            underline="never"
            @click="router.push('/recover')"
            >Forget password?
          </el-link>
        </div>
      </el-form-item>

      <el-form-item class="fixed-bottom-tip">
        <span>Don't have an account? &nbsp;&nbsp;</span>
        <el-link type="primary" @click="isRegister = true">Register</el-link>
      </el-form-item>
    </el-form>

    <!-- register -->
    <el-form
      v-else
      :model="registerForm"
      :rules="rules"
      ref="registerFormRef"
      size="large"
      class="form-style"
    >
      <el-form-item class="form-heading">
        <div class="form-title-group">
          <h1 class="form-title">SIGN UP</h1>
        </div>
      </el-form-item>

      <el-collapse v-model="openSteps">
        <!-- Step 1 -->
        <el-collapse-item
          name="1"
          :disabled="editingStep !== null && editingStep !== 1"
        >
          <template #title>
            <div class="collapse-title">
              <span>Step 1:&nbsp;Your Email</span>
            </div>
          </template>

          <template v-if="editingStep !== 1 && stepStatus[1]">
            <div class="details-line">
              <span class="email">{{ registerForm.email }}</span>
              <el-link type="primary" @click.stop="editStep(1)" class="change">
                Change
              </el-link>
            </div>
          </template>
          <template v-else>
            <el-form-item prop="email">
              <el-input
                data-test="register-email-input"
                v-model="registerForm.email"
                :prefix-icon="Message"
                placeholder="Please input your email"
                type="email"
                autocomplete="email"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                @click="confirmEmail"
                data-test="register-button1"
                class="step-button"
              >
                Continue
              </el-button>
            </el-form-item>
          </template>
        </el-collapse-item>

        <!-- Step 2 -->
        <el-collapse-item
          name="2"
          :disabled="
            !stepStatus[1] || (editingStep !== null && editingStep !== 2)
          "
        >
          <template #title>
            <div class="collapse-title">
              <span>Step 2:&nbsp;Your Details</span>
            </div>
          </template>

          <template v-if="editingStep !== 2 && stepStatus[2]">
            <div class="details-line">
              <div class="details">
                <div>{{ registerForm.id }}</div>
                <div>
                  {{ registerForm.firstName }} {{ registerForm.lastName }}
                </div>
                <div>{{ registerForm.phone }}</div>
              </div>
              <el-link
                v-if="stepStatus[2] && editingStep !== 2"
                type="primary"
                @click.stop="editStep(2)"
                class="change"
              >
                Change
              </el-link>
            </div>
          </template>
          <template v-else>
            <el-form-item prop="id">
              <el-input
                v-model="registerForm.id"
                :prefix-icon="User"
                placeholder="Please input username"
              />
            </el-form-item>

            <div style="display: flex; gap: 10px; width: 100%">
              <el-form-item prop="firstName" style="flex: 1">
                <el-input
                  v-model="registerForm.firstName"
                  placeholder="First name"
                />
              </el-form-item>
              <el-form-item prop="lastName" style="flex: 1">
                <el-input
                  v-model="registerForm.lastName"
                  placeholder="Last name"
                />
              </el-form-item>
            </div>

            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                :prefix-icon="Phone"
                placeholder="Please input your phone number"
                type="phone"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="goToStep3" class="step-button">
                SAVE AND CONTINUE
              </el-button>
            </el-form-item>
          </template>
        </el-collapse-item>

        <!-- Step 3 -->
        <el-collapse-item
          name="3"
          :disabled="
            !stepStatus[2] || (editingStep !== null && editingStep !== 3)
          "
          :class="getStepTitleClass(3)"
        >
          <template #title>
            <div class="collapse-title">
              <span>Step 3:&nbsp;Account Info</span>
            </div>
          </template>

          <template v-if="editingStep !== 3 && stepStatus[3]">
            <div class="details-line">
              <span class="details">Password is set</span>
            </div>
          </template>
          <template v-else>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                :prefix-icon="Lock"
                type="password"
                placeholder="Please input password"
              />
            </el-form-item>
            <el-form-item prop="repassword">
              <el-input
                v-model="registerForm.repassword"
                :prefix-icon="Lock"
                type="password"
                placeholder="Please input password again"
              />
            </el-form-item>
            <el-form-item>
              <el-button @click="register" class="step-button">
                REGISTER
              </el-button>
            </el-form-item>
          </template>
        </el-collapse-item>
      </el-collapse>

      <el-form-item class="fixed-bottom-tip">
        <span>Already have an account?&nbsp;&nbsp;</span>
        <el-link type="primary" underline="never" @click="isRegister = false">
          Sign in
        </el-link>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
:deep(.el-collapse-item__header),
:deep(.el-collapse-it__wrap),
:deep(.el-collapse-item__content) {
  /*background-color: transparent;*/
  padding: 0 0;
  box-sizing: border-box;
  background-color: rgba(115, 162, 230, 0);
}

.glass-effect {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  width: 90%;
  max-width: 400px;
}

.el-collapse-item {
  margin: 0;
  border-bottom: none;
  background-color: rgba(115, 162, 230, 0);
}

:deep(.el-collapse-item__wrap) {
  background-color: rgba(80, 60, 120, 0);

  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.collapse-title {
  display: flex;
  width: 100%;
  font-size: 16px;
  font-weight: bold;
  justify-content: space-between;
  align-items: center;
}

.details-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.details {
  font-size: 14px;
  color: #333;
}

.change {
  font-size: 14px;
  color: #409eff;
  cursor: pointer;
}

.form-title-group {
  width: 100%;
  padding-top: 10px;
}

.form-title {
  font-size: 35px;
  font-weight: bold;
  color: #222;
  margin: 0;
}

.form-subtitle {
  font-size: 16px;
  color: #888;
  margin-top: 0;
  margin-bottom: 0;
}

:deep(.el-collapse-item__header) {
  border: none;
}

:deep(.el-collapse) {
  border: none;
}

.form-style {
  width: 85%;
  margin: 0 auto;
}

.flex {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button {
  width: 100%;
  text-transform: uppercase;
  background-color: #75b4f4;
  border-radius: 4px;

  font-weight: bold;
  font-size: 16px;
  border: none;
  color: #fff;
}

.button:hover {
  background-color: #5294d8;
}

.step-button {
  width: 100%;
  text-transform: uppercase;
  border: 2px solid #75b4f4;
  color: #ffffff;
  background-color: rgba(117, 180, 244, 0.51);
  font-weight: bold;
  border-radius: 4px;
}

.step-button:hover {
  background-color: #75b4f4;
}
</style>
