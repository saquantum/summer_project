/**
 * Upload Configuration
 * Centralized configuration for file upload functionality
 */

export interface UploadConfig {
  // Authentication credentials
  auth: {
    uid: string
    token: string
  }

  // File size limits (in MB)
  fileSizeLimit: {
    avatar: number
    document: number
    image: number
  }

  // Allowed file types
  allowedTypes: {
    avatar: string[]
    document: string[]
    image: string[]
  }

  // Upload URLs
  endpoints: {
    avatar: string
    document: string
    image: string
  }
}

// Default configuration
export const uploadConfig: UploadConfig = {
  auth: {
    uid: import.meta.env.VITE_UPLOAD_UID || '',
    token: import.meta.env.VITE_UPLOAD_TOKEN || ''
  },

  fileSizeLimit: {
    avatar: Number(import.meta.env.VITE_UPLOAD_AVATAR_SIZE_LIMIT) || 5, // MB
    document: Number(import.meta.env.VITE_UPLOAD_DOC_SIZE_LIMIT) || 10,
    image: Number(import.meta.env.VITE_UPLOAD_IMAGE_SIZE_LIMIT) || 8
  },

  allowedTypes: {
    avatar: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
    document: [
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ],
    image: [
      'image/jpeg',
      'image/png',
      'image/gif',
      'image/webp',
      'image/svg+xml'
    ]
  },

  endpoints: {
    avatar: import.meta.env.VITE_UPLOAD_AVATAR_URL || '/api/upload/avatar',
    document: import.meta.env.VITE_UPLOAD_DOC_URL || '/api/upload/document',
    image: import.meta.env.VITE_UPLOAD_IMAGE_URL || '/api/upload/image'
  }
}

// Helper functions
export const getUploadData = () => ({
  uid: uploadConfig.auth.uid,
  token: uploadConfig.auth.token
})

export const getUploadUrl = (
  type: 'avatar' | 'document' | 'image' = 'avatar'
) => {
  return uploadConfig.endpoints[type]
}

export const validateAvatarFile = (
  file: File
): { valid: boolean; message?: string } => {
  // Check file size
  const sizeInMB = file.size / 1024 / 1024
  if (sizeInMB > uploadConfig.fileSizeLimit.avatar) {
    return {
      valid: false,
      message: `Avatar file size cannot exceed ${uploadConfig.fileSizeLimit.avatar}MB!`
    }
  }

  // Check file type
  if (!uploadConfig.allowedTypes.avatar.includes(file.type)) {
    return {
      valid: false,
      message: 'Please upload a valid image file (JPEG, PNG, GIF, WebP)'
    }
  }

  return { valid: true }
}

export const validateImageFile = (
  file: File
): { valid: boolean; message?: string } => {
  const sizeInMB = file.size / 1024 / 1024
  if (sizeInMB > uploadConfig.fileSizeLimit.image) {
    return {
      valid: false,
      message: `Image file size cannot exceed ${uploadConfig.fileSizeLimit.image}MB!`
    }
  }

  if (!uploadConfig.allowedTypes.image.includes(file.type)) {
    return {
      valid: false,
      message: 'Please upload a valid image file'
    }
  }

  return { valid: true }
}

export const validateDocumentFile = (
  file: File
): { valid: boolean; message?: string } => {
  const sizeInMB = file.size / 1024 / 1024
  if (sizeInMB > uploadConfig.fileSizeLimit.document) {
    return {
      valid: false,
      message: `Document file size cannot exceed ${uploadConfig.fileSizeLimit.document}MB!`
    }
  }

  if (!uploadConfig.allowedTypes.document.includes(file.type)) {
    return {
      valid: false,
      message: 'Please upload a valid document file (PDF, DOC, DOCX)'
    }
  }

  return { valid: true }
}
