<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'
import { userUpdateService } from '@/api/user'

const userStore = useUserStore()

const { assetHolder } = userStore.user
console.log(assetHolder)
const arr = userStore.user.assetHolder.name.split(' ')
const form = ref({
  firstName: arr[0],
  lastName: arr[1],
  email: assetHolder.email,
  phone: assetHolder.phone,
  address: {
    street: assetHolder.address.street,
    postCode: assetHolder.address.postcode,
    city: assetHolder.address.city,
    country: assetHolder.address.country
  }
})
//Avatar upload
const avatarUrl = ref(assetHolder.avatar || '') // 默认
const avatarFile = ref(null)

const handleAvatarChange = (e) => {
  const file = e.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = () => {
    avatarUrl.value = reader.result
  }
  reader.readAsDataURL(file)
  avatarFile.value = file
}


const rules = {
  firstName: [
    { required: true, message: 'First name is required', trigger: 'blur' },
    { min: 2, max: 30, message: 'First name must be 2–30 characters', trigger: 'blur' }
  ],
  lastName: [
    { required: true, message: 'Last name is required', trigger: 'blur' },
    { min: 2, max: 30, message: 'Last name must be 2–30 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Email is required', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
  ],
  'address.street': [
    { required: true, message: 'Street is required', trigger: 'blur' }
  ],
  'address.postCode': [
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
  'address.city': [
    { required: true, message: 'City is required', trigger: 'blur' }
  ],
  'address.country': [
    { required: true, message: 'Country is required', trigger: 'blur' }
  ]
}


const submit = async () => {
  try {
    await formRef.value.validate()

    await userUpdateService({
      ...form.value,
      avatar: avatarUrl.value // 要求后端支接收base64
    })

    ElMessage.success('Profile updated!')
  } catch (e) {
    ElMessage.error('Please fix form errors')
  }
}
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Personal details</span>
      </div>
    </template>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      label-width="auto"
      style="max-width: 600px"
    >
      <!-- Avatar upload -->
      <el-form-item label="Avatar">
        <div style="display: flex; align-items: center; gap: 16px;">
          <el-avatar :src="avatarUrl" size="large" />
          <input type="file" accept="image/*" @change="handleAvatarChange" />
        </div>
      </el-form-item>

      <!-- name -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="FIRST NAME" prop="firstName">
            <el-input v-model="form.firstName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="LAST NAME" prop="lastName">
            <el-input v-model="form.lastName" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- email -->
      <el-form-item label="EMAIL ADDRESS" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>

      <!-- address -->
      <el-form-item label="ADDRESS" prop="address">
        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item label="Street" label-width="100px" prop="address.street">
              <el-input v-model="form.address.street" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Post Code" label-width="100px" prop="address.postCode">
              <el-input v-model="form.address.postCode" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item label="City" label-width="100px" prop="address.city">
              <el-input v-model="form.address.city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Country" label-width="100px" prop="address.country">
              <el-input v-model="form.address.country" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>


      <!--submit botton -->
      <el-form-item>
        <el-button type="primary" @click="submit">Submit</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
