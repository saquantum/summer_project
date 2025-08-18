<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

// Props:
const props = defineProps<{
  visible: boolean
}>()

// Emits
const emit = defineEmits<{
  (_event: 'update:visible', _value: boolean): void
  (_event: 'close'): void
  (_event: 'crop-finish', _base64: string): void
}>()

// Reactive data
const fileInputRef = ref<HTMLInputElement>()
const imgRef = ref<HTMLImageElement>()
const imageSrc = ref('')
const croppedBase64 = ref('')
const isCompact = ref(false)
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
function confirmCrop() {
  if (croppedBase64.value) {
    emit('crop-finish', croppedBase64.value)
  }
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
    width="800px"
    :class="{ 'compact-mode': isCompact }"
    @close="handleClose"
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
    <div class="cropper-main-container">
      <!-- 左：裁剪区 -->
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
          <p>please select image</p>
        </div>
      </div>

      <!-- Right：control-panel -->
      <div class="control-panel">
        <!-- Action Buttons -->
        <div class="button-group">
          <button @click="getCroppedImage">Get Cropped Result</button>
          <button @click="confirmCrop" :disabled="!croppedBase64">
            Confirm
          </button>
          <button @click="reset" :disabled="!croppedBase64">Reset</button>
          <button @click="downloadImage" :disabled="!croppedBase64">
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

.file-input {
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
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

.button-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.button-group button {
  padding: 10px 16px;
  border: none;
  border-radius: 4px;
  background: #007bff;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}
.button-group button:hover {
  background: #0069d9;
}

.button-group button:disabled {
  background: #cccccc;
  cursor: not-allowed;
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

@media (max-width: 768px) {
  .cropper-main-container {
    flex-direction: column;
  }

  .control-panel {
    width: 100%;
    margin-top: 20px;
  }

  .file-input {
    margin-bottom: 15px;
  }

  .cropper-image {
    max-height: 50vh;
  }
}

.compact-mode {
  width: 90vw !important;
}

.compact-mode .cropper-main-container {
  min-height: 300px;
}
</style>
