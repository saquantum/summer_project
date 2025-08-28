<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { ref, onMounted } from 'vue'
import type { FormRules } from 'element-plus'
import type { UserInfoForm } from '@/types'
import PageTopTabs from '@/components/PageSurfaceTabs.vue'
import type { RouteLocationNormalized } from 'vue-router'

import { adminInsertUserService } from '@/api/admin'
import {
  createRepasswordRules,
  firstNameRules,
  lastNameRules,
  passwordRules,
  phoneRules,
  postcodeRules,
  trimForm,
  uniqueEmailRules,
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
  'contactDetails.email': uniqueEmailRules,
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
const tabsConfig = [
  {
    label: 'Users',
    to: { name: 'AdminAllUsers' },
    match: (r: RouteLocationNormalized) =>
      r.name === 'AdminAllUsers' || r.name === 'AdminUserDetail'
  },
  { label: 'Add User', to: { name: 'AdminAddUser' } },
  {
    label: 'Access Control',
    to: { name: 'AdminAccessControl' },
    match: (r: RouteLocationNormalized) =>
      r.path?.startsWith('/admin/access-control')
  }
]
</script>

<template>
  <div class="page-surface">
    <PageTopTabs :tabs="tabsConfig" />
    <el-card class="card-elevated form-card">
      <el-form
        ref="formRef"
        :model="form"
        label-width="auto"
        label-position="top"
        :rules="rules"
        class="edit-form"
      >
        <!-- Account -->
        <div class="section-title">Account</div>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="Username" prop="id">
              <el-input v-model="form.id" data-test="id" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Password" prop="password">
              <el-input
                type="password"
                v-model="form.password"
                data-test="password"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Type password again" prop="repassword">
              <el-input
                type="password"
                v-model="form.repassword"
                data-test="repassword"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- name -->
        <div class="section-title">Name</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="First name" prop="firstName">
              <el-input v-model="form.firstName" data-test="firstName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Last name" prop="lastName">
              <el-input v-model="form.lastName" data-test="lastName" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- Contact -->
        <div class="section-title">Contact</div>
        <!-- email -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Email" prop="contactDetails.email">
              <el-input v-model="form.contactDetails.email" data-test="email" />
            </el-form-item>
          </el-col>
          <!-- phone -->
          <el-col :span="12">
            <el-form-item label="Phone" prop="contactDetails.phone">
              <el-input v-model="form.contactDetails.phone" data-test="phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- address -->
        <div class="section-title">Address</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="Street" prop="address.street">
              <el-input v-model="form.address.street" data-test="street" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Post Code" prop="address.postcode">
              <el-input v-model="form.address.postcode" data-test="postcode" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="City" prop="address.city">
              <el-input v-model="form.address.city" data-test="city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Country" prop="address.country">
              <el-input v-model="form.address.country" data-test="city" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- Preferences -->
        <div class="section-title">Contact preferences</div>
        <div class="pref-grid">
          <el-checkbox
            label="Email"
            v-model="form.contactPreferences.email"
            data-test="preference-email"
          />
          <el-checkbox
            label="Phone"
            v-model="form.contactPreferences.phone"
            data-test="preference-phone"
          />
          <el-checkbox
            label="WhatsApp"
            v-model="form.contactPreferences.whatsapp"
            data-test="preference-whatsapp"
          />
          <el-checkbox
            label="Discord"
            v-model="form.contactPreferences.discord"
            data-test="preference-discord"
          />
          <el-checkbox
            label="Post"
            v-model="form.contactPreferences.post"
            data-test="preference-post"
          />
          <el-checkbox
            label="Telegram"
            v-model="form.contactPreferences.telegram"
            data-test="preference-telegram"
          />
        </div>

        <!--submit botton -->
        <div class="form-actions">
          <el-button
            type="primary"
            @click="submit"
            data-test="submit"
            class="styled-btn btn-submit"
            >Submit</el-button
          >
          <el-button
            @click="reset"
            data-test="reset"
            class="styled-btn btn-cancel"
            >Reset</el-button
          >
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /*width: 1100px;*/
  width: min(80vw, 1600px);
  max-width: calc(100% - 2rem);
  box-sizing: border-box;
}
.card-elevated {
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  border: 1px solid #ebeef5;
  background: #fff;
  overflow: hidden;
}

.section-title {
  margin: 6px 0 10px;
  font-size: 16px;
  font-weight: 700;
  color: #2f3b37;
  border-bottom: 2px solid rgba(133, 165, 220, 0.27);
  padding-bottom: 6px;
}

.edit-form :deep(.el-input__wrapper) {
  background-color: #f7f8fa;
  border: 1px solid #e3e6ea;
  border-radius: 10px;
  box-shadow: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s,
    background-color 0.2s;
  padding: 0 12px;
  min-height: 40px;
}
.edit-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
}
.edit-form :deep(.el-input__wrapper.is-focus) {
  border-color: #4c7dd1;
  box-shadow: 0 0 0 3px rgba(123, 166, 125, 0.15);
  background-color: #fff;
}
.edit-form :deep(.el-input__inner) {
  color: #111827;
  caret-color: #141cb3;
  font-size: 15px;
}
.edit-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 12px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}
.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  border: 1px solid #fff;
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
}

.btn-cancel:hover {
  transform: translateY(-1px);
  background-image: linear-gradient(
    to bottom,
    #782d2d 0%,
    #852e2e 10%,
    #903737 100%
  );
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 25px;
}

.pref-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  justify-items: start;
  padding-bottom: 20px;
}
.pref-grid:deep(.el-checkbox.is-checked .el-checkbox__inner) {
  background-color: rgba(54, 94, 145, 0.91);
  border-color: rgba(209, 174, 21, 0.93);
}
.pref-grid :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #ffffff;
}

.pref-grid :deep(.el-checkbox__label) {
  color: #283242;
  font-size: 15px;
}

@media (max-width: 768px) {
  .page-surface {
    width: min(1100px, calc(100vw - 24px));
    margin: 40px auto;
    padding: 16px;
  }
}
@media (max-width: 480px) {
  .page-surface {
    background: transparent;
    border: 0;
    box-shadow: none;
    padding: 0;
    margin-top: 24px;
  }
}
</style>
