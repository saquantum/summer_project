<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores'
// import { userUpdateService } from '@/api/user'
import { adminGetUserInfoService } from '@/api/admin'
import { useRoute } from 'vue-router'
const userStore = useUserStore()

const route = useRoute()
const user = ref({})
const descriptionsItem = ref([])
const form = ref({
  id: '',
  password: '',
  repassword: '',
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
      email: '',
      phone: '',
      whatsapp: '',
      discord: '',
      post: '',
      telegram: ''
    }
  }
})
const formRef = ref()

const isEdit = ref(false)
// Avatar upload
// const avatarUrl = ref(assetHolder.value.avatar || '')
// const avatarFile = ref(null)

// const handleAvatarChange = (e) => {
//   const file = e.target.files[0]
//   if (!file) return

//   const reader = new FileReader()
//   reader.onload = () => {
//     avatarUrl.value = reader.result
//   }
//   reader.readAsDataURL(file)
//   avatarFile.value = file
// }

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
  email: [
    { required: true, message: 'Email is required', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' }
  ],
  phone: [
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
    await form.value.validate()

    // await userUpdateService({
    //   ...form.value,
    //   avatar: avatarUrl.value
    // })

    ElMessage.success('Profile updated!')
  } catch {
    ElMessage.error('Please fix form errors')
  }
}

onMounted(async () => {
  // load data according to data type
  if (!userStore.user.admin) {
    user.value = userStore.user
    console.log(user.value)
  } else {
    const id = userStore.proxyId || route.query.id
    const res = await adminGetUserInfoService(id)
    console.log(res)
    user.value = res.data
  }

  const arr = user.value.assetHolder.name.split(' ')
  form.value = user.value
  form.value.firstName = arr[0]
  form.value.lastName = arr[1]

  descriptionsItem.value = [
    { label: 'User id', value: user.value.id },
    { label: 'First name', value: arr[0] },
    { label: 'Last name', value: arr[1] },
    { label: 'Email', value: user.value.assetHolder.email ?? '' },
    { label: 'Phone', value: user.value.assetHolder.phone ?? '' },
    {
      label: 'Street',
      value: user.value.assetHolder.address?.street ?? ''
    },
    {
      label: 'Postcode',
      value: user.value.assetHolder.address?.postcode ?? ''
    },
    { label: 'City', value: user.value.assetHolder.address?.city ?? '' },
    {
      label: 'Country',
      value: user.value.assetHolder.address?.country ?? ''
    }
  ]
})
</script>

<template>
  <el-descriptions title="User Info" :column="2" border v-if="!isEdit">
    <el-descriptions-item label="Avatar">
      <el-avatar :size="size" :src="circleUrl" />
    </el-descriptions-item>
    <el-descriptions-item
      v-for="(item, index) in descriptionsItem"
      :key="index"
      :label="item.label"
    >
      {{ item.value }}</el-descriptions-item
    >
    <el-descriptions-item
      label="Contact preferences"
      v-if="user && user.assetHolder && user.assetHolder.contact_preferences"
    >
      <el-checkbox
        v-model="user.assetHolder.contact_preferences.email"
        :disabled="!isEdit"
        >Email</el-checkbox
      >
      <el-checkbox
        v-model="user.assetHolder.contact_preferences.phone"
        :disabled="!isEdit"
        >Phone</el-checkbox
      >
      <el-checkbox
        v-model="user.assetHolder.contact_preferences.discord"
        :disabled="!isEdit"
        >Discord</el-checkbox
      >
      <el-checkbox
        v-model="user.assetHolder.contact_preferences.post"
        :disabled="!isEdit"
        >Post</el-checkbox
      >
      <el-checkbox
        v-model="user.assetHolder.contact_preferences.telegram"
        :disabled="!isEdit"
        >Telegram</el-checkbox
      >
    </el-descriptions-item>
  </el-descriptions>
  <el-form
    v-else
    ref="formRef"
    :model="form"
    label-position="top"
    label-width="auto"
    style="max-width: 600px"
    :rules="rules"
  >
    <!-- Avatar upload -->
    <el-form-item label="Avatar">
      <div style="display: flex; align-items: center; gap: 16px">
        <el-avatar :src="avatarUrl" size="large" />
        <input type="file" accept="image/*" @change="handleAvatarChange" />
      </div>
    </el-form-item>

    <!-- name -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="FIRST NAME" prop="lastName">
          <el-input v-model="form.firstName" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="LAST NAME" prop="email">
          <el-input v-model="form.lastName" />
        </el-form-item>
      </el-col>
    </el-row>

    <!-- email -->
    <el-form-item label="EMAIL ADDRESS" prop="email">
      <el-input v-model="form.assetHolder.email" />
    </el-form-item>

    <!-- phone -->
    <el-form-item label="PHONE" prop="phone">
      <el-input v-model="form.assetHolder.phone" />
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
            <el-input v-model="form.assetHolder.address.street" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="Post Code"
            label-width="100px"
            prop="address.postCode"
          >
            <el-input v-model="form.assetHolder.address.postcode" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="width: 100%">
        <el-col :span="12">
          <el-form-item label="City" label-width="100px" prop="address.city">
            <el-input v-model="form.assetHolder.address.city" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            label="Country"
            label-width="100px"
            prop="address.country"
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
  </el-form>

  <!-- operation -->
  <el-button @click="isEdit = true">Edit</el-button>
  <el-button @click="isEdit = false">Cancel</el-button>
  <el-button v-if="isEdit" type="primary" @click="submit">Submit</el-button>
</template>

<style scoped></style>
