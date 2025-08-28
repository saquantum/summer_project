<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

// Props:
const props = defineProps<{
  visible: boolean
  isMobile?: boolean
}>()

// Emits
const emit = defineEmits<{
  (_event: 'update:visible', _value: boolean): void
  (_event: 'close'): void
  (_event: 'crop-finish', _base64: string): void
  // (_event: 'submit-form'): void
}>()

// Reactive data
const fileInputRef = ref<HTMLInputElement>()
const imgRef = ref<HTMLImageElement>()
const imageSrc = ref('')
const croppedBase64 = ref('')
const isCompact = ref(false)
// const isMobile = ref(false)
let cropper: Cropper | null = null

// Default cropper options
const copperOptions: Cropper.Options = {
  aspectRatio: 1, // 1:1 ratio
  viewMode: 1,
  autoCrop: true,
  autoCropArea: 0.8,
  movable: true,
  zoomable: true,
  cropBoxMovable: false,
  cropBoxResizable: false,
  ready() {
    console.log('Cropper is ready')
  }
}

function handleClose() {
  emit('update:visible', false)
  emit('close')
  // clean cropper
  imageSrc.value = ''
  croppedBase64.value = ''
  cropper?.destroy()
  cropper = null

  // clean input
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

// File selection handler
function onFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) return

  // Destroy previous cropper instance
  if (cropper) {
    cropper.destroy()
    cropper = null
  }

  // Revoke previous URL
  if (imageSrc.value) {
    URL.revokeObjectURL(imageSrc.value)
  }

  // Create new URL
  imageSrc.value = URL.createObjectURL(file)
  croppedBase64.value = ''

  // Wait for DOM update then initialize cropper
  nextTick(() => {
    initCropper()
  })
}

// Initialize cropper
function initCropper() {
  if (!imgRef.value) return
  // get contain width
  const size = imgRef.value.parentElement?.clientWidth || 300
  cropper = new Cropper(imgRef.value, {
    ...copperOptions,
    minContainerWidth: size,
    minContainerHeight: size,
    minCanvasWidth: size,
    minCanvasHeight: size
  })
}

// Get cropped image
function getCroppedImage() {
  if (!cropper) return

  const canvas = cropper.getCroppedCanvas({
    width: 300,
    height: 300,
    fillColor: '#fff',
    imageSmoothingQuality: 'high'
  })

  if (canvas) {
    // Convert to base64
    croppedBase64.value = canvas.toDataURL('image/jpeg', 0.8)
    // emit('crop-finish', croppedBase64.value)

    // Another convert to blob for upload
    canvas.toBlob(
      (blob) => {
        if (blob) {
          console.log('Cropped blob:', blob)
          // Handle blob, e.g. upload to server
        }
      },
      'image/jpeg',
      0.8
    )
  }
}

// Reset cropper
function reset() {
  if (cropper) {
    cropper.reset()
    croppedBase64.value = ''
  }
}

// Confirm cropper
async function confirmCrop() {
  if (!croppedBase64.value) {
    getCroppedImage()
  }

  if (!croppedBase64.value) return

  await emit('crop-finish', croppedBase64.value)

  handleClose()
}

// Download image
function downloadImage() {
  if (!croppedBase64.value) return

  const link = document.createElement('a')
  link.download = 'cropped-image.jpg'
  link.href = croppedBase64.value
  link.click()
}

onMounted(() => {
  window.addEventListener('resize', () => cropper?.reset())
  isCompact.value = window.innerWidth < 768
})

// Cleanup resources
onUnmounted(() => {
  window.removeEventListener('resize', () => cropper?.reset())
  if (cropper) {
    cropper.destroy()
  }
  if (imageSrc.value) {
    URL.revokeObjectURL(imageSrc.value)
  }
})

// Small size
const checkCompactMode = () => {
  isCompact.value = window.innerWidth < 768
}

onMounted(() => {
  window.addEventListener('resize', checkCompactMode)
  checkCompactMode()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkCompactMode)
})
</script>

<template>
  <el-dialog
    :model-value="props.visible"
    title="Crop Avatar"
    :width="props.isMobile ? '92vw' : '720px'"
    :fullscreen="false"
    :class="{ 'compact-mode': isCompact }"
    @close="handleClose"
    modal-class="confirm-dialog-overlay"
    class="confirm-dialog"
  >
    <!-- File Upload -->
    <input
      v-if="!isCompact"
      type="file"
      accept="image/*"
      @change="onFileChange"
      class="file-input"
      ref="fileInputRef"
    />

    <!-- cropper-main-container -->
    <div v-if="!isMobile" class="cropper-main-container">
      <div class="cropper-area">
        <!-- Cropper Area -->
        <div v-if="imageSrc" class="cropper-wrapper">
          <img
            ref="imgRef"
            :src="imageSrc"
            alt="Image to crop"
            class="cropper-image"
          />
        </div>
        <div v-else class="upload-hint">
          <div class="hint">please select image</div>
        </div>
      </div>

      <!-- Right: control-panel -->
      <div class="control-panel">
        <!-- Action Buttons -->
        <div class="button-group">
          <button @click="getCroppedImage" class="styled-btn">
            Get Cropped Result
          </button>
          <button
            @click="confirmCrop"
            :disabled="!croppedBase64"
            class="styled-btn"
          >
            Confirm
          </button>
          <button @click="reset" :disabled="!croppedBase64" class="styled-btn">
            Reset
          </button>
          <button
            @click="downloadImage"
            :disabled="!croppedBase64"
            class="styled-btn"
          >
            Download
          </button>
        </div>

        <!-- Preview Area -->
        <div v-if="croppedBase64" class="preview">
          <h4>Cropped Preview:</h4>
          <img
            :src="croppedBase64"
            alt="Cropped Preview"
            class="preview-image"
          />
        </div>
      </div>
    </div>
    <div v-else class="mobile-crop-container">
      <div class="mobile-cropper-wrapper">
        <template v-if="!imageSrc">
          <input
            type="file"
            accept="image/*"
            @change="onFileChange"
            ref="fileInputRef"
            style="width: 100%; padding: 20px"
          />
        </template>

        <template v-else>
          <img
            ref="imgRef"
            :src="imageSrc"
            alt="Image to crop"
            class="cropper-image"
            style="width: 100%; height: 100%; object-fit: contain"
          />
        </template>
      </div>

      <button class="styled-btn" @click="confirmCrop" :disabled="!imageSrc">
        Confirm
      </button>
    </div>
  </el-dialog>
</template>

<style scoped>
.cropper-main-container {
  display: flex;
  gap: 20px;
  min-height: 400px;
  align-items: flex-start;
}

/* left copper-area */
.cropper-area {
  flex: 1;
  min-width: 0;
}

.hint {
  font-size: 20px;
  font-weight: 600;
}

.file-input {
  margin-bottom: 20px;
  padding: 10px;
  border: 1px dashed #9bb7d4;
  border-radius: 12px;
  background: #f9fafc;
  font-size: 14px;
  color: #39435b;
  cursor: pointer;
  transition: 0.3s;
}

.file-input:hover {
  background: #eef2f7;
  border-color: #3b72e7;
}

.file-input::-webkit-file-upload-button {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 14px;
  border-radius: 12px;
  color: #ffffff;
  border: 1px solid #fff;
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  transition: 0.3s;
}

.file-input::-webkit-file-upload-button:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
}

.cropper-wrapper {
  width: 100%;
  height: 100%;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f8f8f8;
}

.cropper-image {
  max-width: 100%;
  max-height: 60vh;
  display: block;
}

.upload-hint {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
}

.control-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.button-group button:disabled {
  background: #cbcfde;
  cursor: not-allowed;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
}

.preview {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  background: white;
}

.preview h4 {
  margin: 0 0 10px 0;
  color: #495057;
}

.preview img {
  border: 1px solid #dee2e6;
  border-radius: 4px;
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
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

:deep(.el-dialog.confirm-dialog) {
  border-radius: 16px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

:deep(.el-dialog.confirm-dialog .el-dialog__header) {
  padding: 16px 20px 8px;
  border-bottom: 1px solid #eef2f7;
}
:deep(.el-dialog.confirm-dialog .el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.2px;
}
:deep(.el-dialog.confirm-dialog .el-dialog__body) {
  padding: 16px 20px;
  color: #334155;
  line-height: 1.7;
  background: #fff;
}
:deep(.el-dialog.confirm-dialog .el-dialog__footer) {
  padding: 14px 16px 16px;
  background: #fafbfd;
  border-top: 1px solid #eef2f7;
}

:deep(.el-overlay.confirm-dialog-overlay) {
  backdrop-filter: blur(2px);
  background-color: rgba(15, 23, 42, 0.35);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .mobile-crop-container {
    display: flex;
    flex-direction: column;
    min-height: 100dvh;
    width: 100%;
    box-sizing: border-box;
    padding: 16px 16px calc(88px + env(safe-area-inset-bottom));
    gap: 16px;
    -webkit-overflow-scrolling: touch;
  }

  .file-input {
    width: 100%;
    margin-bottom: 16px;
    padding: 12px;
    font-size: 16px;
    border: 1px dashed #9bb7d4;
    border-radius: 12px;
    background: #f9fafc;
  }

  .mobile-cropper-wrapper {
    flex: 1;
    width: 100%;
    max-width: 100%;
    aspect-ratio: 1 / 1;
    max-height: min(80dvh, 100vw);
    overflow: hidden;
    border: 1px dashed #e5e7eb;
    border-radius: 12px;
    background: #f9fafc;
  }

  .cropper-image {
    width: 100%;
    height: 100%;
    object-fit: contain;
    max-height: none;
    display: block;
  }

  .mobile-confirm-btn,
  .styled-btn.mobile-confirm-btn {
    position: sticky;
    bottom: env(safe-area-inset-bottom);
    width: 100%;
    padding: 14px 18px;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    display: inline-block;
  }

  .cropper-main-container {
    flex-direction: column;
  }
  .control-panel {
    width: 100%;
    margin-top: 16px;
    gap: 16px;
  }
  :deep(.el-dialog.confirm-dialog) {
    --el-dialog-width: 600px !important;
    margin: 20px auto !important;
    max-height: 90vh;
    border-radius: 12px;
  }

  :deep(.el-dialog__body) {
    max-height: 70vh;
    overflow-y: auto;
  }
}
</style>
