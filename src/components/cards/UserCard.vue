<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
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

import { getUploadData, getUploadUrl } from '@/utils/upload'
import { useResponsiveAction } from '@/composables/useResponsiveAction'
import { useRouter } from 'vue-router'
import {
  EditPen,
  ArrowLeft,
  ArrowRight,
  Lock,
  Camera
} from '@element-plus/icons-vue'

const router = useRouter()
const isPhone = ref(false)
const goPassword = () => {
  router.push('/security/verify-mail')
}

const route = useRoute()
const props = defineProps<{ isEdit: boolean }>()

const emit = defineEmits(['update:isEdit', 'open-cropper'])
const userStore = useUserStore()
const originalForm = ref<UserInfoForm | null>(null)
const originalImageUrl = ref<string>('')
watch(
  () => props.isEdit,
  (val) => {
    if (val) {
      originalForm.value = JSON.parse(JSON.stringify(form.value))
      originalImageUrl.value = imageUrl.value
    }
  }
)

const user = computed(() => {
  if (!userStore.user) {
    throw new Error('User not logged in or expired')
  }
  return userStore.user
})

const currentUser = ref<User | null>(null)

const column = ref(2)

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
  'contactDetails.email': emailRules,
  'contactDetails.phone': phoneRules,
  'address.postcode': postcodeRules
}
const isTagMode = ref(false)
const prefOptions = [
  { key: 'email', label: 'Email' },
  { key: 'discord', label: 'Discord' },
  { key: 'phone', label: 'Phone' },
  { key: 'post', label: 'Post' },
  { key: 'telegram', label: 'Telegram' },
  { key: 'whatsapp', label: 'Whatsapp' }
]

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
    console.log(result)
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
      whatsapp: user.contactPreferences.whatsapp ?? false,
      telegram: user.contactPreferences.telegram ?? false
    }
  }
}

const submit = async () => {
  trimForm(form.value)
  try {
    if (avatarFile.value) {
      try {
        const avatarUrl = await uploadAvatarToServer()
        console.log(avatarUrl)
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

useResponsiveAction((width) => {
  column.value = width < 768 ? 1 : 2
  isTagMode.value = width <= 768
  isPhone.value = width <= 480
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
  if (originalForm.value) {
    Object.assign(form.value, JSON.parse(JSON.stringify(originalForm.value)))
  }

  imageUrl.value = originalImageUrl.value || currentUser.value?.avatar || ''

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  avatarFile.value = null

  formRef.value?.clearValidate?.()

  emit('update:isEdit', false)
}

defineExpose({
  submit,
  form,
  avatarFile,
  previewUrl,
  cancelEdit
})

type FieldKey =
  | 'name'
  | 'contactDetails.email'
  | 'contactDetails.phone'
  | 'address.country'
  | 'address.street'
  | 'address.city'
  | 'address.postcode'

const sheetOpen = ref(false)
const sheetTitle = ref('')
const sheetLabel = ref('')
const sheetKey = ref<FieldKey>('name')
const sheetValue = ref('')

type AnyRecord = Record<string, unknown>

const isRecord = (v: unknown): v is AnyRecord =>
  typeof v === 'object' && v !== null

const getByPath = (obj: unknown, path: string): unknown => {
  return path.split('.').reduce<unknown>((acc, key) => {
    return isRecord(acc) ? acc[key] : undefined
  }, obj)
}

const setByPath = (obj: unknown, path: string, val: unknown): void => {
  if (!isRecord(obj)) return

  const keys = path.split('.')
  const lastKey = keys.pop() as string

  const target = keys.reduce<AnyRecord>((acc, key) => {
    const next = isRecord(acc[key]) ? (acc[key] as AnyRecord) : {}
    acc[key] = next
    return next
  }, obj)

  target[lastKey] = val
}

const fields = [
  { key: 'name' as FieldKey, label: 'Name', sheetTitle: 'Your name' },
  {
    key: 'contactDetails.email' as FieldKey,
    label: 'Email',
    sheetTitle: 'Email address'
  },
  {
    key: 'contactDetails.phone' as FieldKey,
    label: 'Phone number',
    sheetTitle: 'Phone number'
  },
  {
    key: 'address.country' as FieldKey,
    label: 'Country',
    sheetTitle: 'Country'
  },
  { key: 'address.street' as FieldKey, label: 'Street', sheetTitle: 'Street' },
  { key: 'address.city' as FieldKey, label: 'City', sheetTitle: 'City' },
  {
    key: 'address.postcode' as FieldKey,
    label: 'Postcode',
    sheetTitle: 'Postal code'
  }
]

const displayValue = (k: FieldKey) => {
  if (k === 'name') {
    const full =
      `${form.value.firstName || ''} ${form.value.lastName || ''}`.trim()
    return full || '(optional)'
  }
  const v = getByPath(form.value, k)
  return v ? String(v) : '(optional)'
}

const toStringSafe = (v: unknown): string =>
  v == null ? '' : typeof v === 'string' ? v : String(v)
const openField = (item: {
  key: FieldKey
  label: string
  sheetTitle: string
}) => {
  sheetKey.value = item.key
  sheetLabel.value = item.label
  sheetTitle.value = item.sheetTitle
  sheetValue.value =
    item.key === 'name'
      ? displayValue('name').replace('(optional)', '').trim()
      : toStringSafe(getByPath(form.value, item.key))

  sheetOpen.value = true
}
const saveField = async () => {
  if (sheetKey.value === 'name') {
    const raw = (sheetValue.value || '').trim()
    const [first, ...rest] = raw.split(/\s+/)
    form.value.firstName = first || ''
    form.value.lastName = rest.join(' ')
    form.value.name = raw
  } else {
    setByPath(form.value, sheetKey.value, sheetValue.value)
  }
  sheetOpen.value = false
}
</script>

<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-position="top"
    label-width="auto"
    class="edit-form"
  >
    <div class="grid-scroller">
      <el-row :gutter="20" class="profile-grid">
        <el-col :xs="24" :sm="8" :md="8" class="col-left">
          <div class="left-stack">
            <el-card class="card profile-card">
              <div
                class="avatar-wrap"
                @click="props.isEdit && emit('open-cropper')"
              >
                <el-avatar
                  :src="previewUrl || imageUrl || currentUser?.avatar"
                />
                <span class="avatar-edit-dot" v-if="props.isEdit"
                  ><el-icon><Camera /></el-icon
                ></span>
              </div>
              <div class="profile-name">{{ currentUser?.name }}</div>
              <div class="uid">uid {{ currentUser?.id }}</div>
            </el-card>

            <el-card class="card pref-card" v-if="currentUser">
              <div class="card-title-sub">Contact Preferences</div>
              <div class="pref-list pref-grid" v-if="!isTagMode">
                <el-checkbox
                  v-model="form.contactPreferences.email"
                  :disabled="!props.isEdit"
                  >Email</el-checkbox
                >
                <el-checkbox
                  v-model="form.contactPreferences.discord"
                  :disabled="!props.isEdit"
                  >Discord</el-checkbox
                >
                <el-checkbox
                  v-model="form.contactPreferences.phone"
                  :disabled="!props.isEdit"
                  >Phone</el-checkbox
                >
                <el-checkbox
                  v-model="form.contactPreferences.post"
                  :disabled="!props.isEdit"
                  >Post</el-checkbox
                >
                <el-checkbox
                  v-model="form.contactPreferences.telegram"
                  :disabled="!props.isEdit"
                  >Telegram</el-checkbox
                >
                <el-checkbox
                  v-model="form.contactPreferences.whatsapp"
                  :disabled="!props.isEdit"
                  >Whatsapp</el-checkbox
                >
              </div>

              <div
                class="pref-list tag-grid"
                v-else
                :class="{ 'is-edit': props.isEdit }"
              >
                <el-check-tag
                  v-for="opt in prefOptions"
                  :key="opt.key"
                  v-show="
                    props.isEdit ||
                    form.contactPreferences[
                      opt.key as keyof UserInfoForm['contactPreferences']
                    ]
                  "
                  :checked="
                    form.contactPreferences[
                      opt.key as keyof UserInfoForm['contactPreferences']
                    ]
                  "
                  @change="
                    (val: boolean) => {
                      if (!props.isEdit) return
                      ;(form.contactPreferences as any)[opt.key] = val
                    }
                  "
                  :class="{ 'is-disabled': !props.isEdit }"
                >
                  {{ opt.label }}
                </el-check-tag>
              </div>
            </el-card>
          </div>
        </el-col>

        <el-col :xs="24" :sm="16" :md="16" class="col-right">
          <el-card class="card info-card edit-form">
            <div class="info-card__header">
              <div class="card-title">Information</div>
              <div class="actions">
                <slot name="header-actions"></slot>
              </div>
            </div>
            <el-divider
              style="
                --el-border-color: rgba(133, 165, 220, 0.27);
                border-top-width: 2px;
              "
            />
            <div>
              <el-row :gutter="16">
                <el-col :xs="24" :sm="12" :md="12">
                  <div class="field-label">First Name</div>
                  <el-input
                    v-model="form.firstName"
                    :disabled="!props.isEdit"
                    data-test="firstName"
                  />
                </el-col>
                <el-col :xs="24" :sm="12" :md="12">
                  <div class="field-label">Last Name</div>
                  <el-input v-model="form.lastName" :disabled="!props.isEdit" />
                </el-col>
              </el-row>
            </div>
            <el-row :gutter="16" class="mt-12">
              <el-col :xs="24" :sm="12" :md="12">
                <div class="field-label">Email Address</div>
                <el-input
                  v-model="form.contactDetails.email"
                  :disabled="!props.isEdit"
                />
              </el-col>
              <el-col :xs="24" :sm="12" :md="12">
                <div class="field-label">Phone Number</div>
                <el-input
                  v-model="form.contactDetails.phone"
                  :disabled="!props.isEdit"
                />
              </el-col>
            </el-row>

            <el-row :gutter="16" class="mt-12">
              <el-col :xs="24" :sm="8" :md="8">
                <div class="field-label">Country</div>
                <el-input
                  v-model="form.address.country"
                  :disabled="!props.isEdit"
                />
              </el-col>
              <el-col :xs="24" :sm="8" :md="8">
                <div class="field-label">City</div>
                <el-input
                  v-model="form.address.city"
                  :disabled="!props.isEdit"
                />
              </el-col>
              <el-col :xs="24" :sm="8" :md="8">
                <div class="field-label">Postcode</div>
                <el-input
                  v-model="form.address.postcode"
                  :disabled="!props.isEdit"
                />
              </el-col>
            </el-row>
            <el-row :gutter="16" class="mt-12">
              <el-col :xs="24">
                <div class="field-label">Street</div>
                <el-input
                  v-model="form.address.street"
                  :disabled="!props.isEdit"
                />
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </el-form>

  <div v-if="isPhone && !sheetOpen" class="mobile-list">
    <div class="group">
      <div class="mobile-hero">
        <div
          class="avatar-wrap"
          @click="props.isEdit && emit('open-cropper')"
          role="button"
          tabindex="0"
        >
          <el-avatar
            :src="previewUrl || imageUrl || currentUser?.avatar || undefined"
            :size="88"
          />
          <button
            v-if="isPhone"
            class="cam-btn"
            type="button"
            aria-label="Change avatar"
            @click.stop="emit('open-cropper')"
          >
            <el-icon><Camera /></el-icon>
          </button>
          <span class="avatar-edit-dot" v-if="props.isEdit"></span>
        </div>
      </div>

      <div class="mobile-prefs">
        <div class="contact-bar">
          <div class="contact-title">Contact Preferences</div>
          <button
            class="mp-edit"
            type="button"
            aria-label="Edit preferences"
            @click="emit('update:isEdit', !props.isEdit)"
          >
            <el-icon><EditPen /></el-icon>
          </button>
        </div>

        <div class="mp-tags" :class="{ 'is-edit': props.isEdit }">
          <el-check-tag
            v-for="opt in prefOptions"
            :key="opt.key"
            v-show="
              props.isEdit ||
              form.contactPreferences[
                opt.key as keyof UserInfoForm['contactPreferences']
              ]
            "
            :checked="
              form.contactPreferences[
                opt.key as keyof UserInfoForm['contactPreferences']
              ]
            "
            @change="
              (val: boolean) => {
                if (!props.isEdit) return
                ;(form.contactPreferences as any)[opt.key] = val
              }
            "
          >
            {{ opt.label }}
          </el-check-tag>
        </div>
      </div>

      <div class="group-title">Personal info</div>
      <button
        v-for="f in fields"
        :key="f.key"
        type="button"
        class="cell"
        @click="openField(f)"
      >
        <span class="label">{{ f.label }}</span>
        <span class="value">{{ displayValue(f.key) }}</span>
        <el-icon class="chev"><ArrowRight /></el-icon>
      </button>
    </div>
  </div>

  <el-drawer
    v-model="sheetOpen"
    direction="btt"
    :with-header="false"
    size="92%"
    class="field-drawer"
  >
    <div class="sheet">
      <div class="sheet-header">
        <el-button text @click="sheetOpen = false"
          ><el-icon><ArrowLeft /></el-icon
        ></el-button>
        <div class="sheet-title">{{ sheetTitle }}</div>
        <el-button text @click="sheetOpen = false">Cancel</el-button>
      </div>

      <div class="sheet-body">
        <div class="field-label-2">{{ sheetLabel }}</div>
        <el-input v-model="sheetValue" clearable />
        <div class="sheet-actions">
          <el-button round type="primary" class="save-btn" @click="saveField"
            >Save</el-button
          >
        </div>
      </div>
    </div>
  </el-drawer>

  <div v-if="isPhone" class="group">
    <div class="change-title">Change Password</div>
    <button
      v-if="isPhone"
      type="button"
      class="list-item sheet-style"
      @click="goPassword"
    >
      <div class="left">
        <el-icon><Lock /></el-icon>
        <span>Password</span>
      </div>
      <div class="right">
        <el-icon><ArrowRight /></el-icon>
      </div>
    </button>
  </div>
</template>

<style scoped>
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

.col-left,
.col-right {
  display: flex;
}

.left-stack {
  display: flex;
  flex-direction: column;
  gap: 0;
  width: 100%;
}

.card {
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
}

.profile-card {
  text-align: center;
  padding-top: 5px;
}
.avatar-wrap {
  position: relative;
  display: inline-block;
}
.avatar-edit-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  right: 6px;
  bottom: 6px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #fff;
  color: rgba(2, 2, 2, 0.53);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}
.avatar-edit-dot .el-icon {
  font-size: 20px;
}
.profile-name {
  font-size: 28px;
  font-weight: 700;
  margin-top: 16px;
}

.card-title {
  flex: 1;
  font-size: 25px;
  font-weight: 800;
  text-align: left;
}
.card-title-sub {
  font-size: 20px;
  font-weight: 700;
  text-align: left;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(133, 165, 220, 0.27);
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.info-card__header .actions {
  display: flex;
  align-items: center;
}
.uid {
  color: #666;
  font-weight: 600;
}
.field-label {
  margin: 6px 0 6px;
  font-weight: 600;
  font-size: 18px;
  line-height: 1.4;
}
.mt-12 {
  margin-top: 30px;
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
}

.edit-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
}

.edit-form :deep(.el-input__wrapper.is-focus) {
  border-color: #4c7dd1;
  box-shadow: 0 0 0 3px rgba(123, 166, 125, 0.15);
  background-color: #fff;
}

.edit-form:deep(.el-input__inner) {
  color: #111827;
  caret-color: #141cb3;
  font-size: 15px;
}

.edit-form:deep(.el-checkbox.is-checked .el-checkbox__inner) {
  background-color: rgba(54, 94, 145, 0.91);
  border-color: rgba(209, 174, 21, 0.93);
}
.edit-form :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: #ffffff;
}

.edit-form:deep(.el-checkbox__label) {
  color: #283242;
  font-size: 18px;
}

.edit-card {
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
}

.edit-form {
  width: 100%;
  margin: 0 auto;
}
.edit-form .row {
  margin-bottom: 4px;
}

.avatar-field {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.avatar-hint {
  font-size: 12px;
  color: #6b7280;
}

.pref-line :deep(.el-checkbox) {
  margin-right: 16px;
}

.pref-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px 10px;
  align-items: center;
}

.pref-grid :deep(.el-checkbox) {
  margin-right: 0;
  display: flex;
  align-items: center;
}

.pref-card {
  margin-top: 22px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-card {
  height: 500px;
}
.profile-grid {
  align-items: stretch;
  height: 500px;
}

.avatar-wrap .el-avatar {
  width: 160px;
  height: 160px;
}

.list-item {
  display: none;
}

@media (max-width: 1450px) and (min-width: 768px) {
  .grid-scroller {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    padding-bottom: 8px;
  }

  .profile-grid {
    display: flex;
    gap: 20px;
    width: max-content;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }

  .profile-grid::before,
  .profile-grid::after {
    display: none;
  }
  .profile-grid > .el-col {
    padding-left: 10px;
    padding-right: 10px;
  }

  .col-left,
  .col-right {
    flex: 0 0 auto;
  }

  .col-left {
    min-width: 360px;
  }
  .col-right {
    min-width: 820px;
  }
  .info-card {
    min-width: 820px;
  }

  .el-card__body {
    overflow: visible;
  }
}

@media (max-width: 768px) {
  .profile-grid {
    display: block;
    height: auto !important;
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  .col-left,
  .col-right {
    display: block;
    width: 100%;
    min-width: 0;
  }

  .info-card,
  .pref-card {
    height: auto !important;
    margin-top: 20px;
  }
  .card,
  .info-card,
  .pref-card {
    overflow: visible;
  }

  .edit-form {
    overflow: visible;
    padding: 0 10px;
  }

  .edit-form :deep(.el-input__wrapper) {
    min-height: 40px;
    border-radius: 10px;
  }
  .tag-grid {
    display: flex;
    flex-wrap: wrap;
    margin-top: 10px;
    gap: 5px 5px;
  }

  .tag-grid :deep(.el-check-tag) {
    border-radius: 12px;
    padding: 5px 15px;
    font-size: 10px;
    font-weight: 500;
    border: 1px solid #315a89;
    color: #050a61;
    background-color: rgba(153, 179, 207, 0.29);
    transition: all 0.2s ease;
    cursor: pointer;
  }

  .tag-grid :deep(.el-check-tag.is-checked) {
    background-color: rgb(150, 176, 204);
    border-color: #3b4c69;
    color: #fff;
  }

  .tag-grid :deep(.el-check-tag.is-disabled) {
    cursor: default;
    opacity: 0.6;
  }

  .tag-grid :deep(.el-check-tag) {
    position: relative;
  }

  .tag-grid :deep(.el-check-tag.is-checked)::after {
    content: none;
  }

  .tag-grid.is-edit :deep(.el-check-tag.is-checked)::after {
    content: 'x';
    font-weight: 1000;
    position: absolute;
    top: 4px;
    right: 5px;
    font-size: 12px;
    color: #b30d0d;
    pointer-events: none;
  }
}

@media (max-width: 480px) {
  .mobile-header {
    display: grid;
    grid-template-columns: 40px 1fr auto;
    align-items: center;
    gap: 8px;
    padding: 6px 4px 4px;
  }
  .nav-back {
    color: #d9d9d9;
  }
  .mobile-title {
    font-size: 18px;
    font-weight: 700;
  }
  .save-pill {
    border: 0;
    border-radius: 999px;
    padding: 6px 14px;
    background: #2a2a2a;
    color: #f5f5f5;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
  }

  .mobile-hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 18px 8px 10px;
    position: relative;
  }
  .mobile-hero .el-avatar {
    width: 88px;
    height: 88px;
    border: 4px solid #303030;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
  }
  .hero-name {
    margin-top: 10px;
    font-size: 18px;
    font-weight: 700;
  }
  .hero-email {
    margin-top: 2px;
    font-size: 12px;
    opacity: 0.7;
  }

  .section-header {
    font-size: 11px;
    letter-spacing: 0.12em;
    color: rgba(255, 255, 255, 0.45);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px 6px 8px;
  }
  .section-header .chev {
    opacity: 0.4;
  }

  .list-item {
    width: 100%;
    background: transparent;
    border: 0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 14px 6px;
    color: #e8e8e8;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
  }
  .list-item:first-of-type {
    border-top: 0;
  }
  .list-item .left {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
  }
  .list-item .right {
    font-size: 12px;
    opacity: 0.6;
  }
  .list-item .el-icon {
    font-size: 18px;
    opacity: 0.85;
  }
  .list-item.danger {
    color: #ff8989;
  }

  .profile-grid,
  .pref-card,
  .info-card,
  .card-title-sub,
  .info-card :deep(.el-divider) {
    display: none !important;
  }

  .group {
    margin-top: 6px;
  }
  .group-title,
  .change-title {
    font-weight: 700;
    color: #2f3b37;
    margin: 30px 6px 10px 6px;
  }

  .cell {
    width: 100%;
    text-align: left;
    display: grid;
    grid-template-columns: 1fr auto 14px;
    align-items: center;
    gap: 10px;
    padding: 14px 16px;
    margin: 12px 0;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.06);
    border-radius: 18px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.04);
  }
  .cell .label {
    font-size: 16px;
    color: #111;
  }
  .cell .value {
    justify-self: end;
    font-size: 16px;
    color: #6a7a74;
  }
  .cell .chev {
    color: #9aa5a0;
  }

  .field-drawer :deep(.el-drawer) {
    border-radius: 22px 22px 0 0;
    overflow: hidden;
  }
  .field-drawer :deep(.el-drawer__body) {
    padding: 0;
    background: #fff;
  }

  .sheet {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  .sheet-header {
    display: grid;
    grid-template-columns: 64px 1fr 64px;
    align-items: center;
    padding: 10px 8px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    position: sticky;
    top: 0;
    z-index: 1;
    background: #fff;
  }
  .sheet-title {
    text-align: center;
    font-weight: 800;
    font-size: 18px;
  }

  .sheet-body {
    padding: 14px 14px 24px;
    overflow: auto;
  }
  .field-label-2 {
    margin: 6px 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #054b9a;
  }

  .sheet-body :deep(.el-input__wrapper) {
    background: #fff;
    border: 1px solid #dfe5e7;
    border-radius: 14px;
    min-height: 44px;
    box-shadow: none;
  }
  .sheet-actions {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .save-btn {
    padding: 8px 20px;
    font-weight: 600;
    font-size: 16px;
    border-radius: 12px;

    border: 1px solid #fdfdfd;
    color: #fff;

    text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
    background-image: linear-gradient(
      180deg,
      #e4dfd8 0%,
      #9bb7d4 60%,
      rgba(58, 78, 107, 0.8) 120%
    );
    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
  }

  .list-item.sheet-style {
    background: #fff;
    border: 1px solid #dfe5e7;
    border-radius: 18px;
    padding: 14px 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.04);
    color: #111;
  }

  .list-item.sheet-style .left {
    display: inline-flex;
    align-items: center;
    gap: 10px;
  }

  .list-item.sheet-style .right {
    font-size: 12px;
    color: #6a7a74;
  }

  .list-item.sheet-style .el-icon {
    font-size: 18px;
    opacity: 0.85;
  }

  .list-item.sheet-style:hover {
    border-color: #cfd7db;
    background: #fafbfc;
  }
  .list-item.sheet-style:active {
    transform: translateY(1px);
  }

  .mobile-hero :deep(.el-avatar) {
    width: 88px;
    height: 88px;
    border: 2px solid #e6dada;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
  }
  .avatar-wrap {
    position: relative;
    display: inline-block;
    width: 88px;
    height: 88px;
  }
  .avatar-wrap :deep(.el-avatar) {
    width: 100%;
    height: 100%;
    border: 2px solid #e6dada;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.25);
  }

  .avatar-wrap .cam-btn {
    position: absolute;
    right: 6px;
    bottom: 6px;
    width: 28px;
    height: 28px;
    padding: 0;
    border: 0;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: #020202;
    color: #fff;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
    z-index: 2;
  }

  .avatar-wrap .cam-btn .el-icon {
    font-size: 18px;
    line-height: 1;
  }

  .cam-btn .el-icon {
    font-size: 18px;
    display: inline-flex;
  }
  .contact-bar {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .contact-title {
    font-weight: 700;
    color: #2f3b37;
    margin: 30px 6px 10px 6px;
  }

  .mp-header .el-icon {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .mp-edit {
    width: 32px;
    height: 32px;
    border: 0;
    border-radius: 999px;
    background: #111;
    color: #fff;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.18);
    padding: 0;
    margin-left: auto;
    margin-right: 10px;
  }
  .mp-edit .el-icon {
    font-size: 16px;
  }

  .mp-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-top: 14px;
  }
  .mp-tags :deep(.el-check-tag) {
    border-radius: 12px;
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 500;
    border: 1px solid #365e91;
    color: #08345e;
    background-color: #e0dfdc;
    transition: all 0.2s ease;
    cursor: pointer;
  }
  .mp-tags :deep(.el-check-tag.is-checked) {
    background-color: #365e91;
    border-color: #08345e;
    color: #fff;
  }
}
</style>
