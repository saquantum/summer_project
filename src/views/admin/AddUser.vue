<script setup>
import { userRegisterService } from '@/api/user'
import { ElMessage } from 'element-plus'
import { ref, onMounted } from 'vue'

const form = ref({
  id: '',
  firstName: '',
  lastName: '',
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
})

const formRef = ref()

const rules = {
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
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
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
    }
  ],
  'assetHolder.address.street': [
    { required: true, message: 'Street is required', trigger: 'blur' }
  ],
  'assetHolder.address.postcode': [
    { required: true, message: 'Post code is required', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const ukPostcodeRegex = /^[A-Z]{1,2}\d{1,2}[A-Z]?\s?\d[A-Z]{2}$/i
        if (!ukPostcodeRegex.test(value)) {
          callback(new Error('Invalid UK postcode (e.g. SW1A 1AA)'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  'assetHolder.address.city': [
    { required: true, message: 'City is required', trigger: 'blur' }
  ],
  'assetHolder.address.country': [
    { required: true, message: 'Country is required', trigger: 'blur' }
  ]
}

const submit = async () => {
  try {
    await formRef.value.validate()
  } catch (e) {
    console.log(e)
    return
  }
  try {
    form.value.assetHolder.name = `${form.value.firstName} ${form.value.lastName}`
    await userRegisterService(form.value)
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

      <el-form-item label="Contact preference">
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
