<script setup lang="ts">
import { useUserStore } from '@/stores'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CropperDialog from '@/components/dialog/CropperDialog.vue'

const router = useRouter()
const userCardRef = ref()
const userStore = useUserStore()

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
  if (fileData) {
    tempAvatar.value = fileData
  }
  showCropper.value = true
}

// New
const handleCroppedAvatar = async (base64: string) => {
  try {
    const blob = await fetch(base64).then((r) => r.blob())
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })

    if (userCardRef.value) {
      userCardRef.value.avatarFile = file
      userCardRef.value.previewUrl = base64
      userCardRef.value.form.avatar = base64
    }

    ElMessage.success('Avatar cropped! Ready to submit')
    showCropper.value = false
  } catch (error) {
    console.error('Crop error:', error)
    ElMessage.error('Failed to process image')
  }
}
</script>

<template>
  <!-- cut pop-up -->
  <CropperDialog
    :visible="showCropper"
    :initial-image="tempAvatar?.previewUrl"
    @close="showCropper = false"
    @crop-finish="handleCroppedAvatar"
  />
  <UserCard
    ref="userCardRef"
    v-model:isEdit="isEdit"
    @open-cropper="openCropperDialog"
  >
  </UserCard>

  <!-- operation -->
  <div class="button-group">
    <el-button
      v-if="!isEdit"
      class="styled-btn btn-edit"
      data-test="edit-btn"
      :disabled="isDisabled"
      @click="handleEdit"
    >
      Edit
    </el-button>
    <el-button v-else class="styled-btn btn-cancel" @click="handleCancel">
      Cancel
    </el-button>

    <el-button v-if="isEdit" class="styled-btn btn-submit" @click="submit">
      Submit
    </el-button>

    <el-button
      v-if="!isEdit"
      class="styled-btn btn-secondary"
      @click="router.push('/security/verify-mail')"
    >
      Change password
    </el-button>
  </div>
</template>

<style scoped>
.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 10px;
}
.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 12px;

  border: 1px solid #fdfdfd;
  color: #fff;

  background-image: linear-gradient(
    to top,
    rgba(163, 205, 168, 0.76) 0%,
    #a4d5a1 100%
  );
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.2),
    inset 0 -1px 1px rgba(0, 0, 0, 0.05),
    0 2px 4px rgba(0, 0, 0, 0.25);
}

.styled-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: linear-gradient(to bottom, #d4eacb, #f4f4f4);
  color: #6b9f65;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.25);
}
/* Cancel：*/
.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}
.btn-cancel:hover {
  transform: translateY(-1px);
  background-image: linear-gradient(
    to bottom,
    #a46b6b 0%,
    #dc5151 10%,
    #903737 100%
  );
  color: #ffffff;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}

/* Change password */
.btn-secondary {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #539853;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}
.btn-secondary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-image: linear-gradient(to top, #b8bbbf 20%, #d3d6dc 100%);
  color: #ffffff;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.25);
}
</style>
