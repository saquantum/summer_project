<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { ref, onMounted } from 'vue'
import type { FormRules } from 'element-plus'
import type { UserInfoForm } from '@/types'

import { adminInsertUserService } from '@/api/admin'
import {
  createRepasswordRules,
  emailRules,
  firstNameRules,
  lastNameRules,
  passwordRules,
  phoneRules,
  postcodeRules,
  trimForm,
  usernameRules
} from '@/utils/formUtils'

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
  id: usernameRules,
  password: passwordRules,
  repassword: createRepasswordRules(() => form.value.password || ''),
  firstName: firstNameRules,
  lastName: lastNameRules,
  'assetHolder.email': emailRules,
  'assetHolder.phone': phoneRules,
  'assetHolder.address.postcode': postcodeRules
})

const submit = async () => {
  trimForm(form.value)
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
