<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/index.ts'
import { userUpdateInfoService } from '@/api/user'
import {
  adminGetUserInfoService,
  adminUpdateUserInfoService
} from '@/api/admin'
import { ElMessage } from 'element-plus'
import type { User, UserInfoForm } from '@/types'
import { useRoute } from 'vue-router'
import {
  emailRules,
  firstNameRules,
  lastNameRules,
  phoneRules,
  postcodeRules,
  trimForm
} from '@/utils/formUtils'
import { Plus } from '@element-plus/icons-vue'
import { getUploadData, getUploadUrl } from '@/utils/upload'
import { useResponsiveAction } from '@/composables/useResponsiveAction'
const route = useRoute()
const props = defineProps<{ isEdit: boolean }>()

const emit = defineEmits(['update:isEdit', 'open-cropper'])
const userStore = useUserStore()

const user = computed(() => {
  if (!userStore.user) {
    throw new Error('User not logged in or expired')
  }
  return userStore.user
})

const currentUser = ref<User | null>(null)

const column = ref(2)

const descriptionsItem = computed(() => {
  if (!currentUser.value) return []
  const arr = currentUser.value.name.split(' ')
  return [
    { label: 'User id', value: currentUser.value.id },
    { label: 'First name', value: arr[0] },
    { label: 'Last name', value: arr[1] },
    { label: 'Email', value: currentUser.value.contactDetails.email ?? '' },
    { label: 'Phone', value: currentUser.value.contactDetails.phone ?? '' },
    {
      label: 'Street',
      value: currentUser.value.address?.street ?? ''
    },
    {
      label: 'Postcode',
      value: currentUser.value.address?.postcode ?? ''
    },
    {
      label: 'City',
      value: currentUser.value.address?.city ?? ''
    },
    {
      label: 'Country',
      value: currentUser.value.address?.country ?? ''
    }
  ]
})

const form = ref<UserInfoForm>({
  id: '',
  firstName: '',
  lastName: '',
  avatar: '',
  name: '',
  contactDetails: {
    email: '',
    phone: ''
  },
  address: {
    street: '',
    postcode: '',
    city: '',
    country: ''
  },
  contactPreferences: {
    email: false,
    phone: false,
    discord: false,
    post: false,
    whatsapp: false,
    telegram: false
  }
})
const formRef = ref()

const imageUrl = ref('')
const avatarFile = ref<File | null>(null)
const previewUrl = ref('')

const rules = {
  firstName: firstNameRules,
  lastName: lastNameRules,
  email: emailRules,
  phone: phoneRules,
  'address.postcode': postcodeRules
}

// Handle file selection and create local preview
// const handleAvatarChange = (uploadFile: UploadFile) => {
//   const file = uploadFile.raw
//   if (!file) return
//
//   const validation = validateAvatarFile(file)
//   if (!validation.valid) {
//     ElMessage.error(validation.message || 'File validation failed')
//     return
//   }
//
//   // Save file reference
//   avatarFile.value = file
//
//   // Create local preview URL
//   if (previewUrl.value) {
//     URL.revokeObjectURL(previewUrl.value)
//   }
//   previewUrl.value = URL.createObjectURL(file)
// }

// Upload avatar to server
const uploadAvatarToServer = async (): Promise<string | null> => {
  if (!avatarFile.value) return null

  const formData = new FormData()
  formData.append('file', avatarFile.value)

  // Add other required upload data
  const uploadData = getUploadData()
  Object.entries(uploadData).forEach(([key, value]) => {
    formData.append(key, value)
  })

  try {
    const response = await fetch(getUploadUrl('avatar'), {
      method: 'POST',
      body: formData
    })

    const result = await response.json()
    if (result.data?.url) {
      return result.data.url
    }
    throw new Error('Upload failed')
  } catch (error) {
    console.error('Avatar upload error:', error)
    throw error
  }
}

const userToForm = (user: User): UserInfoForm => {
  return {
    id: user.id,
    firstName: '',
    lastName: '',
    avatar: user.avatar ?? '',
    name: user.name ?? '',
    contactDetails: {
      email: user.contactDetails.email,
      phone: user.contactDetails.phone
    },
    address: {
      street: user.address?.street ?? '',
      postcode: user.address?.postcode ?? '',
      city: user.address?.city ?? '',
      country: user.address?.country ?? ''
    },
    contactPreferences: {
      email: user.contactPreferences.email ?? false,
      phone: user.contactPreferences.phone ?? false,
      discord: user.contactPreferences.discord ?? false,
      post: user.contactPreferences.post ?? false,
      whatsapp: user.contactPreferences.post ?? false,
      telegram: user.contactPreferences.post ?? false
    }
  }
}

const submit = async () => {
  trimForm(form.value)
  try {
    // If there's a new uploaded avatar, upload it to server first
    if (avatarFile.value) {
      try {
        const avatarUrl = await uploadAvatarToServer()
        if (avatarUrl) {
          form.value.avatar = avatarUrl
          imageUrl.value = avatarUrl
        }
      } catch {
        ElMessage.error('Failed to upload avatar')
        return
      }
    } else {
      // If no new avatar, use current imageUrl
      form.value.avatar = imageUrl.value
    }

    await formRef.value.validate()
  } catch {
    return
  }

  try {
    const submitData = {
      ...form.value,
      name: `${form.value.firstName} ${form.value.lastName}`
    }

    if (user.value.admin) {
      await adminUpdateUserInfoService([submitData])
    } else {
      if (currentUser.value?.id) {
        await userUpdateInfoService(currentUser.value.id, submitData)
      }
    }

    ElMessage.success('Profile updated!')
    await userStore.getUserInfo()
    await loadUserData()

    // Clean up local preview
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
      previewUrl.value = ''
    }
    avatarFile.value = null

    emit('update:isEdit', false)
  } catch (error) {
    console.error('Update failed:', error)
    ElMessage.error('Failed to update profile')
  }
}

const loadUserData = async () => {
  try {
    if (!user.value.admin) {
      currentUser.value = userStore.user
    } else {
      const id = (userStore.proxyId || route.query.id) as string

      const res = await adminGetUserInfoService(id)

      currentUser.value = res.data
    }

    imageUrl.value = currentUser.value?.avatar ?? ''

    if (!currentUser.value) throw new Error('User does not exist')
    const arr = currentUser.value.name.split(' ')
    form.value = userToForm(currentUser.value)
    form.value.firstName = arr[0]
    form.value.lastName = arr[1]
  } catch (error) {
    console.error('Failed to load user data:', error)
    ElMessage.error('Failed to load user data')
  }
}

// responsive design
useResponsiveAction((width) => {
  if (width < 992) {
    column.value = 1
  } else {
    column.value = 2
  }
})

onMounted(async () => {
  await loadUserData()
})

// Clean up resources
onUnmounted(() => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
})

// Clean up when canceling edit
const cancelEdit = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  avatarFile.value = null
  emit('update:isEdit', false)
}

defineExpose({
  submit,
  form,
  avatarFile,
  previewUrl,
  cancelEdit
})
</script>

<template>
  <el-descriptions title="User Info" :column="column" border v-if="!isEdit">
    <el-descriptions-item label="Avatar">
      <el-avatar :src="currentUser?.avatar" />
    </el-descriptions-item>
    <el-descriptions-item
      v-for="(item, index) in descriptionsItem"
      :key="index"
      :label="item.label"
    >
      {{ item.value }}</el-descriptions-item
    >
    <el-descriptions-item label="Contact preferences" v-if="currentUser">
      <el-checkbox
        v-model="currentUser.contactPreferences.email"
        :disabled="!props.isEdit"
        >Email</el-checkbox
      >
      <el-checkbox
        v-model="currentUser.contactPreferences.phone"
        :disabled="!props.isEdit"
        >Phone</el-checkbox
      >
      <el-checkbox
        v-model="currentUser.contactPreferences.discord"
        :disabled="!props.isEdit"
        >Discord</el-checkbox
      >
      <el-checkbox
        v-model="currentUser.contactPreferences.post"
        :disabled="!props.isEdit"
        >Post</el-checkbox
      >
      <el-checkbox
        v-model="currentUser.contactPreferences.telegram"
        :disabled="!props.isEdit"
        >Telegram</el-checkbox
      >
      <el-checkbox
        v-model="currentUser.contactPreferences.whatsapp"
        :disabled="!props.isEdit"
        >Whatsapp</el-checkbox
      >
    </el-descriptions-item>
  </el-descriptions>
  <el-form
    v-else
    ref="formRef"
    :model="form"
    label-position="top"
    label-width="auto"
    class="form-class"
    :rules="rules"
  >
    <!-- Avatar upload -->
    <el-form-item label="Avatar">
      <div style="display: flex; align-items: center; gap: 16px">
        <el-avatar
          v-if="previewUrl || imageUrl"
          :src="previewUrl || imageUrl"
          @click="emit('open-cropper')"
          style="cursor: pointer; border: 1px dashed #d9d9d9"
        ></el-avatar>
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        <div v-if="avatarFile" style="color: #409eff; font-size: 12px">
          {{ avatarFile.name }} (Preview)
        </div>
      </div>
    </el-form-item>

    <!-- name -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="FIRST NAME" prop="firstName">
          <el-input v-model="form.firstName" data-test="firstName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="LAST NAME" prop="lastName">
          <el-input v-model="form.lastName" />
        </el-form-item>
      </el-col>
    </el-row>

    <!-- email -->
    <el-form-item label="EMAIL ADDRESS" prop="contactDetails.email">
      <el-input v-model="form.contactDetails.email" />
    </el-form-item>

    <!-- phone -->
    <el-form-item label="PHONE" prop="contactDetails.phone">
      <el-input v-model="form.contactDetails.phone" />
    </el-form-item>

    <!-- address -->
    <el-form-item label="ADDRESS">
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
            prop="address.postCode"
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
      <el-checkbox label="Discord" v-model="form.contactPreferences.discord" />
      <el-checkbox label="Post" v-model="form.contactPreferences.post" />
      <el-checkbox
        label="Telegram"
        v-model="form.contactPreferences.telegram"
      />
      <el-checkbox
        label="
        Whatsapp"
        v-model="form.contactPreferences.whatsapp"
      />
    </el-form-item>
  </el-form>
</template>

<style scoped>
.form-class {
  max-width: 600px;
  margin: 0 auto;
  overflow: hidden;
  padding: 10px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}
</style>
