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
  contactDetails: {
    email: '',
    phone: ''
  },
  name: '',
  address: {
    street: '',
    postcode: '',
    city: '',
    country: ''
  },
  contactPreferences: {
    email: true,
    phone: false,
    whatsapp: false,
    discord: false,
    post: false,
    telegram: false
  },
  permissionConfig: {
    userId: '',
    canCreateAsset: false,
    canSetPolygonOnCreate: false,
    canUpdateAssetFields: false,
    canUpdateAssetPolygon: false,
    canDeleteAsset: false,
    canUpdateProfile: false
  }
})

const formRef = ref()

const rules = ref<FormRules<typeof form>>({
  id: usernameRules,
  password: passwordRules,
  repassword: createRepasswordRules(() => form.value.password || ''),
  firstName: firstNameRules,
  lastName: lastNameRules,
  'contactDetails.email': emailRules,
  'contactDetails.phone': phoneRules,
  'address.postcode': postcodeRules
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
    form.value.name = `${form.value.firstName} ${form.value.lastName}`

    await adminInsertUserService([form.value])

    ElMessage.success('Successfully add an user')
  } catch (e) {
    console.error(e)
    ElMessage.error('Server Error')
  }
}

const reset = () => {
  form.value = {
    id: '',
    password: '',
    repassword: '',
    firstName: '',
    lastName: '',
    contactDetails: {
      email: '',
      phone: ''
    },
    name: '',
    address: {
      street: '',
      postcode: '',
      city: '',
      country: ''
    },
    contactPreferences: {
      email: true,
      phone: false,
      whatsapp: false,
      discord: false,
      post: false,
      telegram: false
    },
    permissionConfig: {
      userId: '',
      canCreateAsset: false,
      canSetPolygonOnCreate: false,
      canUpdateAssetFields: false,
      canUpdateAssetPolygon: false,
      canDeleteAsset: false,
      canUpdateProfile: false
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
      <el-form-item label="Email" prop="contactDetails.email">
        <el-input v-model="form.contactDetails.email" />
      </el-form-item>

      <!-- phone -->
      <el-form-item label="Phone" prop="contactDetails.phone">
        <el-input v-model="form.contactDetails.phone" />
      </el-form-item>

      <!-- address -->
      <el-form-item label="Address">
        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item
              label="Street"
              label-width="100px"
              prop="address.street"
            >
              <el-input v-model="form.address.street" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="Post Code"
              label-width="100px"
              prop="address.postcode"
            >
              <el-input v-model="form.address.postcode" />
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
            <el-form-item
              label="Country"
              label-width="100px"
              prop="address.country"
            >
              <el-input v-model="form.address.country" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item label="Contact preferences">
        <el-checkbox label="Email" v-model="form.contactPreferences.email" />
        <el-checkbox label="Phone" v-model="form.contactPreferences.phone" />
        <el-checkbox
          label="WhatsApp"
          v-model="form.contactPreferences.whatsapp"
        />
        <el-checkbox
          label="Discord"
          v-model="form.contactPreferences.discord"
        />
        <el-checkbox label="Post" v-model="form.contactPreferences.post" />
        <el-checkbox
          label="Telegram"
          v-model="form.contactPreferences.telegram"
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
