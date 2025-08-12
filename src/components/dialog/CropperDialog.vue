<script setup lang="ts">
import { ref, nextTick, onUnmounted } from 'vue'
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

function handleClose() {
  emit('update:visible', false)
  emit('close')
}

// Reactive data
const imgRef = ref<HTMLImageElement>()
const imageSrc = ref('')
const croppedBase64 = ref('')
let cropper: Cropper | null = null

// Default cropper options
const defaultOptions: Cropper.Options = {
  aspectRatio: 1, // 1:1 ratio
  viewMode: 1,
  autoCrop: true,
  responsive: true,
  cropBoxResizable: true,
  cropBoxMovable: true,
  zoomable: true,
  ready() {
    console.log('Cropper is ready')
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

  cropper = new Cropper(imgRef.value, defaultOptions)
}

// Get cropped image
function getCroppedImage() {
  if (!cropper) return

  const canvas = cropper.getCroppedCanvas({
    width: 300,
    height: 300,
    imageSmoothingEnabled: true,
    imageSmoothingQuality: 'high'
  })

  if (canvas) {
    // Convert to base64
    // croppedBase64.value = canvas.toDataURL('image/jpeg', 0.8)
    const base64 = canvas.toDataURL('image/jpeg', 0.8)
    croppedBase64.value = base64
    emit('crop-finish', base64)
    handleClose()

    // You can also convert to blob for upload
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

// Download image
function downloadImage() {
  if (!croppedBase64.value) return

  const link = document.createElement('a')
  link.download = 'cropped-image.jpg'
  link.href = croppedBase64.value
  link.click()
}

// Cleanup resources
onUnmounted(() => {
  if (cropper) {
    cropper.destroy()
  }
  if (imageSrc.value) {
    URL.revokeObjectURL(imageSrc.value)
  }
})
</script>

<template>
  <el-dialog
    :model-value="props.visible"
    title="Crop Avatar"
    width="600px"
    @close="handleClose"
  >
    <!-- File Upload -->
    <input
      type="file"
      accept="image/*"
      @change="onFileChange"
      class="file-input"
    />

    <!-- Cropper Area -->
    <div v-if="imageSrc" class="cropper-wrapper">
      <img
        ref="imgRef"
        :src="imageSrc"
        alt="Image to crop"
        style="max-width: 100%; max-height: 50vh"
      />

      <!-- Action Buttons -->
      <div class="button-group">
        <button @click="getCroppedImage">Get Cropped Result</button>
        <button @click="reset">Reset</button>
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
          style="max-width: 200px"
        />
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.cropper-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.file-input {
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.cropper-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  border: 1px solid #eee;
  padding: 10px;
}

.button-group {
  display: flex;
  gap: 8px;
  justify-content: center;
}

button {
  padding: 8px 16px;
  border: 1px solid #007bff;
  border-radius: 4px;
  background: #007bff;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover:not(:disabled) {
  background: #0056b3;
}

button:disabled {
  background: #6c757d;
  border-color: #6c757d;
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
</style>
