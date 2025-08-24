<script setup lang="ts">
import { useUserStore } from '@/stores'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import CropperDialog from '@/components/dialog/CropperDialog.vue'
import ResetPassword from '@/views/user/ResetPassword.vue'
import { watch } from 'vue'

const router = useRouter()
const userCardRef = ref()
const userStore = useUserStore()
const route = useRoute()
const isMobile = ref(window.innerWidth <= 480)
const onResize = () => {
  isMobile.value = window.innerWidth <= 480
}
onMounted(() => {
  window.addEventListener('resize', onResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})

watch(
  () => route.query.tab,
  (t) => {
    activeTab.value = (t as Tab) === 'password' ? 'password' : 'user'
  }
)

type Tab = 'user' | 'password'
const activeTab = ref<Tab>(
  (route.query.tab as Tab) === 'password' ? 'password' : 'user'
)

const goTab = (t: Tab) => {
  if (activeTab.value === t) return
  activeTab.value = t
  router.replace({ path: route.path, query: { ...route.query, tab: t } })
}

const user = computed(() => {
  if (!userStore.user) throw new Error('user is null')
  return userStore.user
})
const submit = () => {
  userCardRef.value.submit()
}

const isEdit = ref(false)

const handleEdit = () => {
  if (user.value.admin) {
    isEdit.value = true
  } else {
    if (user.value.accessControlGroup.canUpdateProfile) {
      isEdit.value = true
    } else {
      ElMessage.error('Can not update profile')
    }
  }
}

const handleCancel = () => {
  userCardRef.value.cancelEdit()
  showCropper.value = false

  if (tempAvatar.value?.previewUrl) {
    URL.revokeObjectURL(tempAvatar.value.previewUrl)
    tempAvatar.value = undefined
  }

  if (userCardRef.value) {
    userCardRef.value.form.avatar = user.value.avatar || ''
    userCardRef.value.previewUrl = ''
    userCardRef.value.avatarFile = null
  }
}

const isDisabled = computed(() => {
  if (user.value.admin) {
    return false
  } else {
    return !userStore.user?.accessControlGroup.canUpdateProfile
  }
})

defineExpose({
  isEdit,
  isDisabled,
  user,
  submit,
  handleEdit,
  userCardRef
})
// avatar
const showCropper = ref(false)
const tempAvatar = ref<{ previewUrl: string }>()

function openCropperDialog(fileData?: { previewUrl: string }) {
  // cleanup
  if (tempAvatar.value?.previewUrl) {
    URL.revokeObjectURL(tempAvatar.value.previewUrl)
  }
  tempAvatar.value = fileData
  showCropper.value = true
}

// listen windows close
watch(showCropper, (newVal) => {
  if (!newVal && tempAvatar.value?.previewUrl) {
    URL.revokeObjectURL(tempAvatar.value.previewUrl)
    tempAvatar.value = undefined
  }
})

// New
const handleCroppedAvatar = async (base64: string) => {
  try {
    // identify Base64 valid
    if (!base64.startsWith('data:image/')) {
      throw new Error('Invalid image data')
    }

    // clean up
    if (userCardRef.value?.previewUrl) {
      URL.revokeObjectURL(userCardRef.value.previewUrl)
    }

    const blob = await fetch(base64).then((r) => r.blob())
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })

    if (userCardRef.value) {
      userCardRef.value.avatarFile = file
      userCardRef.value.previewUrl = base64
      userCardRef.value.form.avatar = base64
    }

    ElMessage.success('Avatar cropped! Ready to submit')
    showCropper.value = false

    if (isMobile.value) {
      submit()
    }
  } catch (error) {
    console.error('Crop error:', error)
    ElMessage.error('Failed to process image')
  }
}
</script>

<template>
  <!-- cut pop-up -->
  <div class="mobile-actions" v-show="!isDisabled">
    <button
      class="tab-btn"
      :class="{ active: activeTab === 'password' }"
      type="button"
      @click="goTab('password')"
    >
      Change password
    </button>

    <template v-if="!isEdit">
      <el-button
        class="styled-btn btn-edit"
        @click="handleEdit"
        data-test="edit-btn"
        >Edit</el-button
      >
    </template>
    <template v-else>
      <el-button class="styled-btn btn-cancel" @click="handleCancel"
        >Cancel</el-button
      >
      <el-button class="styled-btn btn-submit" @click="submit"
        >Submit</el-button
      >
    </template>
  </div>

  <CropperDialog
    :visible="showCropper"
    :initial-image="tempAvatar?.previewUrl"
    :isMobile="isMobile"
    @close="showCropper = false"
    @crop-finish="handleCroppedAvatar"
    @submit-form="submit"
  />

  <div class="page-surface">
    <div class="top-tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'user' }"
        type="button"
        @click="goTab('user')"
      >
        User Info
      </button>

      <button
        class="tab-btn"
        type="button"
        :class="{ active: activeTab === 'password' }"
        @click="goTab('password')"
      >
        Change password
      </button>
    </div>

    <template v-if="activeTab === 'user'">
      <UserCard
        ref="userCardRef"
        v-model:isEdit="isEdit"
        @open-cropper="openCropperDialog"
      >
        <template #header-actions>
          <div class="header-actions-desktop">
            <el-button
              class="styled-btn btn-edit"
              v-if="!isEdit"
              :disabled="isDisabled"
              data-test="edit-btn"
              @click="handleEdit"
            >
              Edit
            </el-button>

            <div class="button-group" v-if="isEdit">
              <el-button class="styled-btn btn-cancel" @click="handleCancel">
                Cancel
              </el-button>
              <el-button class="styled-btn btn-submit" @click="submit">
                Submit
              </el-button>
            </div>
          </div>
        </template>

        <template #avatar>
          <el-button
            type="primary"
            @click="openCropperDialog"
            :disabled="!isEdit"
          >
            Change Avatar
          </el-button>
        </template>
      </UserCard>
    </template>

    <template v-else>
      <ResetPassword embedded @close="goTab('user')" />
    </template>
  </div>
</template>

<style scoped>
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
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
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

.btn-edit {
  width: 80px;
}

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
.top-tabs {
  position: absolute;
  display: inline-flex;
  gap: 8px;
  left: 0;
  top: -35px;
  border-radius: 10px 10px 0 0;
  overflow: visible;
  box-shadow: none;
  box-sizing: border-box;
  z-index: 3;
}
.top-tabs.attached {
  position: absolute;
  margin-bottom: 0;
}

.top-tabs .tab-btn {
  appearance: none;
  border: none;
  padding: 8px 16px;
  border-radius: 8px 8px 0 0;
  background: #dcdbd7;
  color: rgb(5, 55, 99);
  font-weight: 600;
  font-size: 18px;
  line-height: 1;
}

.top-tabs .tab-btn.active {
  background: #06365f;
  color: #fff;
}

.top-tabs .tab-btn.disabled,
.top-tabs .tab-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.mobile-actions {
  display: none;
}

@media (max-width: 768px) {
  .page-surface {
    width: min(1100px, calc(100vw - 24px));
    margin: 60px auto;
    padding: 20px;
    position: relative;
  }

  .top-tabs {
    position: absolute;
    left: 0;
    top: -29px;
    z-index: 3;
  }

  .top-tabs .tab-btn {
    font-size: 16px;
    padding: 6px 12px;
  }
}

@media (max-width: 480px) {
  .top-tabs {
    display: none;
  }
  .header-actions-desktop {
    display: none !important;
  }

  .page-surface {
    background: transparent;
    border: 0;
    box-shadow: none;
    border-radius: 0;
    padding: 0;
    margin: 40px 0 0;
    width: 100%;
  }
}
</style>
