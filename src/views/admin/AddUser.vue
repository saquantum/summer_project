<script setup lang="ts">
import { userCheckUIDService, userCheckEmailService } from '@/api/user'
import CodeUtil from '@/utils/codeUtil'
import { ElMessage } from 'element-plus'
import { ref, onMounted } from 'vue'
import type { FormRules } from 'element-plus'
import type { UserInfoForm } from '@/types'
import type { InternalRuleItem } from 'async-validator'
import { adminInsertUserService } from '@/api/admin'

const form = ref<UserInfoForm>({
  id: '',
  password: '',
  repassword: '',
  firstName: '',
  lastName: '',
  assetHolder: {
    id: '',
    name: '',
    email: '',
    phone: '',
    addressId: '',
    address: {
      assetHolderId: '',
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
      telegram: false,
      assetHolderId: ''
    }
  }
})

const formRef = ref()

const rules = ref<FormRules<typeof form>>({
  id: [
    { required: true, message: 'Please input username', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]+$/,
      message:
        'Username can only contain letters, numbers, and special characters (no spaces allowed).',
      trigger: 'blur'
    },
    {
      asyncValidator: async (
        rule: InternalRuleItem,
        value: string,
        callback: (error?: string | Error) => void
      ) => {
        try {
          const res = await userCheckUIDService(value)
          if (CodeUtil.isSuccess(res.code)) {
            callback(new Error(`Username '${value}' already exists`))
          } else {
            callback()
          }
        } catch {
          callback('Server error')
        }
      },
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: true,
      message: 'Please input password',
      trigger: 'blur'
    },
    {
      pattern: /^\S{5,15}$/,
      message: 'password must between 5 to 15 characters',
      trigger: 'blur'
    }
  ],
  repassword: [
    { required: true, message: 'Please input password', trigger: 'blur' },
    {
      pattern: /^\S{5,15}$/,
      message: 'password must between 5 to 15 characters',
      trigger: 'blur'
    },
    {
      validator: (
        rule: InternalRuleItem,
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
  firstName: [
    { required: true, message: 'First name is required', trigger: 'blur' },
    {
      min: 2,
      max: 30,
      message: 'First name must be 2–30 characters',
      trigger: 'blur'
    }
  ],
  lastName: [
    { required: true, message: 'Last name is required', trigger: 'blur' },
    {
      min: 2,
      max: 30,
      message: 'Last name must be 2–30 characters',
      trigger: 'blur'
    }
  ],
  'assetHolder.email': [
    { required: true, message: 'Email is required', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' },
    {
      asyncValidator: async (
        rule: InternalRuleItem,
        value: string,
        callback: (error?: Error) => void
      ) => {
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
      validator: (
        rule: InternalRuleItem,
        value: string,
        callback: (error?: Error) => void
      ) => {
        const phoneRegex = /^[0-9+\-()\s]{7,20}$/
        if (!phoneRegex.test(value)) {
          callback(new Error('Invalid phone number'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],

  'assetHolder.address.postcode': [
    {
      pattern: /^[a-zA-Z0-9\-\s]{3,16}$/,
      message: 'Post code must be 3-16 letters, numbers, spaces or hyphens.',
      trigger: 'blur'
    }
  ]
})

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch (fields) {
    if (fields) {
      const firstErrorField = Object.keys(fields)[0]
      formRef.value.scrollToField(firstErrorField)
      return
    }
  }
  try {
    form.value.assetHolder.name = `${form.value.firstName} ${form.value.lastName}`
    console.log(form.value)
    const res = await adminInsertUserService([form.value])
    console.log(res)
    ElMessage.success('Successfully add an user')
  } catch {
    ElMessage.error('Server Error')
  }
}

const reset = () => {
  form.value = {
    id: '',
    firstName: '',
    lastName: '',
    password: '',
    repassword: '',
    assetHolder: {
      name: '',
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
  }
}

onMounted(async () => {})
</script>

<template>
  <div class="container">
    <el-form
      ref="formRef"
      :model="form"
      label-width="auto"
      label-position="left"
      style="max-width: 600px"
      :rules="rules"
    >
      <el-form-item label="Username" prop="id">
        <el-input v-model="form.id" />
      </el-form-item>

      <el-form-item label="Password" prop="password">
        <el-input type="password" v-model="form.password" />
      </el-form-item>

      <el-form-item label="Type password again" prop="repassword">
        <el-input type="password" v-model="form.repassword" />
      </el-form-item>
      <!-- name -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="First name" prop="firstName">
            <el-input v-model="form.firstName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Last name" prop="lastName">
            <el-input v-model="form.lastName" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- email -->
      <el-form-item label="Email" prop="assetHolder.email">
        <el-input v-model="form.assetHolder.email" />
      </el-form-item>

      <!-- phone -->
      <el-form-item label="Phone" prop="assetHolder.phone">
        <el-input v-model="form.assetHolder.phone" />
      </el-form-item>

      <!-- address -->
      <el-form-item label="Address">
        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item
              label="Street"
              label-width="100px"
              prop="assetHolder.address.street"
            >
              <el-input v-model="form.assetHolder.address.street" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="Post Code"
              label-width="100px"
              prop="assetHolder.address.postcode"
            >
              <el-input v-model="form.assetHolder.address.postcode" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item
              label="City"
              label-width="100px"
              prop="assetHolder.address.city"
            >
              <el-input v-model="form.assetHolder.address.city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="Country"
              label-width="100px"
              prop="assetHolder.address.country"
            >
              <el-input v-model="form.assetHolder.address.country" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item label="Contact preferences">
        <el-checkbox
          label="Email"
          v-model="form.assetHolder.contact_preferences.email"
        />
        <el-checkbox
          label="Phone"
          v-model="form.assetHolder.contact_preferences.phone"
        />
        <el-checkbox
          label="Discord"
          v-model="form.assetHolder.contact_preferences.discord"
        />
        <el-checkbox
          label="Post"
          v-model="form.assetHolder.contact_preferences.post"
        />
        <el-checkbox
          label="telegram"
          v-model="form.assetHolder.contact_preferences.telegram"
        />
      </el-form-item>

      <!--submit botton -->
      <el-form-item>
        <div style="display: flex; justify-content: center; width: 100%">
          <el-button type="primary" @click="submit">Submit</el-button>
          <el-button @click="reset">Reset</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
