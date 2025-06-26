<script setup>
import { ref } from 'vue'

const formModel = ref({
  email: '',
  CAPTCHA: ''
})

const form = ref()
const loading = ref(false)
const submitted = ref(false)

// Generate random CAPTCHA
const generateCaptcha = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
  let result = ''
  for (let i = 0; i < 6; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

const captchaCode = ref(generateCaptcha())

// Form validation rules
const rules = {
  email: [
    { required: true, message: 'Please input your email', trigger: 'blur' },
    {
      type: 'email',
      message: 'Please input a valid email address',
      trigger: ['blur', 'change']
    }
  ],
  CAPTCHA: [
    { required: true, message: 'Please input CAPTCHA', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value.toUpperCase() !== captchaCode.value) {
          callback(new Error('CAPTCHA is incorrect'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const refreshCaptcha = () => {
  captchaCode.value = generateCaptcha()
  formModel.value.CAPTCHA = ''
}

const handleSubmit = async () => {
  if (!form.value) return

  try {
    const valid = await form.value.validate()
    if (valid) {
      loading.value = true

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 2000))

      submitted.value = true
      loading.value = false
    }
  } catch (error) {
    console.log('Validation failed:', error)
    loading.value = false
  }
}

const resetForm = () => {
  submitted.value = false
  formModel.value = { email: '', CAPTCHA: '' }
  refreshCaptcha()
  form.value?.resetFields()
}
</script>

<template>
  <div class="recovery-container">
    <div class="recovery-card">
      <!-- Header -->
      <div class="header">
        <div class="icon-wrapper">
          <Mail class="header-icon" />
        </div>
        <h2>Account Recovery</h2>
        <p class="subtitle">Recover your username or reset your password</p>
      </div>

      <!-- Success State -->
      <div v-if="submitted" class="success-state">
        <div class="success-icon">✓</div>
        <h3>Recovery Email Sent!</h3>
        <p>
          We've sent recovery instructions to
          <strong>{{ formModel.email }}</strong>
        </p>
        <p class="note">
          Please check your email and follow the confirmation link to complete
          the process.
        </p>
        <el-button type="primary" @click="resetForm" class="back-btn">
          Send Another Recovery Email
        </el-button>
      </div>

      <!-- Form State -->
      <div v-else>
        <div class="instructions">
          <p>
            Enter your email address below and we'll send you instructions to
            recover your account.
          </p>
          <p class="warning">
            ⚠️ You will need to reply to a confirmation email
          </p>
        </div>

        <el-form
          :model="formModel"
          :rules="rules"
          ref="form"
          size="large"
          autocomplete="off"
          class="recovery-form"
          @submit.prevent="handleSubmit"
        >
          <el-form-item prop="email">
            <el-input
              v-model="formModel.email"
              :prefix-icon="User"
              placeholder="Enter your email address"
              type="email"
              autocomplete="email"
            />
          </el-form-item>

          <el-form-item prop="CAPTCHA">
            <div class="captcha-container">
              <el-input
                v-model="formModel.CAPTCHA"
                :prefix-icon="Lock"
                placeholder="Enter CAPTCHA code"
                maxlength="6"
                style="flex: 1; margin-right: 12px"
              />
              <div class="captcha-display">
                <span class="captcha-code">{{ captchaCode }}</span>
                <el-button
                  type="text"
                  @click="refreshCaptcha"
                  class="refresh-btn"
                  title="Refresh CAPTCHA"
                >
                  <RefreshCw class="refresh-icon" />
                </el-button>
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleSubmit"
              :loading="loading"
              class="submit-btn"
              native-type="submit"
            >
              {{ loading ? 'Sending...' : 'Send Recovery Email' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="footer-links">
          <a href="#" class="link">Back to Login</a>
          <span class="separator">•</span>
          <a href="#" class="link">Create Account</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.recovery-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.recovery-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 480px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.icon-wrapper {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.header-icon {
  width: 28px;
  height: 28px;
  color: white;
}

.header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.instructions {
  margin-bottom: 24px;
}

.instructions p {
  color: #555;
  line-height: 1.5;
  margin: 0 0 12px;
}

.warning {
  background: #fff3cd;
  border: 1px solid #ffeeba;
  color: #856404;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  margin-top: 16px !important;
}

.recovery-form {
  margin-bottom: 24px;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-display {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 12px;
  min-width: 120px;
}

.captcha-code {
  font-family: 'Courier New', monospace;
  font-weight: bold;
  font-size: 18px;
  letter-spacing: 2px;
  color: #333;
  text-decoration: line-through;
  text-decoration-color: #999;
  flex: 1;
}

.refresh-btn {
  padding: 4px;
  margin-left: 8px;
}

.refresh-icon {
  width: 16px;
  height: 16px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
}

.footer-links {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.link:hover {
  color: #764ba2;
}

.separator {
  margin: 0 12px;
  color: #ccc;
}

/* Success State */
.success-state {
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  background: #4caf50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  font-size: 36px;
  color: white;
  font-weight: bold;
}

.success-state h3 {
  margin: 0 0 16px;
  font-size: 24px;
  color: #1a1a1a;
}

.success-state p {
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px;
}

.note {
  background: #e8f5e8;
  border: 1px solid #c8e6c9;
  color: #2e7d32;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  margin: 16px 0 24px !important;
}

.back-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  padding: 12px 24px;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 640px) {
  .recovery-card {
    padding: 24px;
    margin: 16px;
  }

  .captcha-container {
    flex-direction: column;
    gap: 8px;
  }

  .captcha-display {
    justify-content: center;
    width: 100%;
  }
}

/* Element Plus overrides */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

:deep(.el-input__wrapper:hover) {
  border-color: #667eea;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

:deep(.el-form-item__error) {
  font-size: 12px;
  margin-top: 4px;
}
</style>
