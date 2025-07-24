// / <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly BASE_URL: string
  readonly MODE: string

  // Upload configuration
  readonly VITE_UPLOAD_UID: string
  readonly VITE_UPLOAD_TOKEN: string
  readonly VITE_UPLOAD_AVATAR_SIZE_LIMIT: string
  readonly VITE_UPLOAD_DOC_SIZE_LIMIT: string
  readonly VITE_UPLOAD_IMAGE_SIZE_LIMIT: string
  readonly VITE_UPLOAD_AVATAR_URL: string
  readonly VITE_UPLOAD_DOC_URL: string
  readonly VITE_UPLOAD_IMAGE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
