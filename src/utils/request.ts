import axios, { type AxiosRequestConfig } from 'axios'
const baseURL = '/api'

const instance = axios.create({
  baseURL,
  timeout: 10000
})

// URLs that don't need idempotency keys
const EXCLUDE_IDEMPOTENCY_PATHS = [
  '/login',
  '/logout',
  '/email/code',
  '/auth/refresh',
  '/email/verification',
  '/asset/type/search',
  '/warning/search',
  '/admin/user/all/search',
  '/admin/user/accumulate/search',
  '/admin/user/count',
  '/admin/asset/search',
  '/admin/asset/count',
  '/admin/warning/all/search',
  '/admin/template/search',
  '/admin/permission/search'
]

// Check if URL should be excluded from idempotency
function shouldExcludeIdempotency(url: string): boolean {
  return EXCLUDE_IDEMPOTENCY_PATHS.some((path) => url.includes(path))
}

// Generate idempotency key based on request content (UUID format)
function generateIdempotencyKey(config: AxiosRequestConfig): string {
  // Create a unique identifier based on URL, method, and data
  const identifier = JSON.stringify({
    url: config.url,
    method: config.method,
    data: config.data,
    params: config.params
  })

  // Generate hash from the identifier
  let hash = 0
  for (let i = 0; i < identifier.length; i++) {
    const char = identifier.charCodeAt(i)
    hash = (hash << 5) - hash + char
    hash = hash & hash // Convert to 32-bit integer
  }

  // Add time window for reasonable uniqueness
  const timeWindow = Math.floor(Date.now() / (5 * 60 * 1000)) // 5-minute window
  const combinedHash = Math.abs(hash + timeWindow)

  // Convert to UUID-like format
  const hex = combinedHash.toString(16).padStart(8, '0')
  const timeHex = timeWindow.toString(16).padStart(8, '0')

  // Create UUID v4-like format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
  return `${hex.slice(0, 8)}-${hex.slice(0, 4)}-4${hex.slice(1, 4)}-${timeHex.slice(0, 1)}${hex.slice(4, 7)}-${timeHex}${hex.slice(0, 4)}`
}

// Add a request interceptor
instance.interceptors.request.use(
  function (config) {
    if (config.method?.toLowerCase() === 'post') {
      config.headers = config.headers || {}

      // Check if this URL should be excluded from idempotency
      const shouldExclude = shouldExcludeIdempotency(config.url ?? '')

      // Only generate idempotency key if not already provided and not excluded
      if (!config.headers['Idempotency-Key'] && !shouldExclude) {
        config.headers['Idempotency-Key'] = generateIdempotencyKey(config)
      } else {
        config.headers['X-Idempotent-Post'] = true
      }
      console.log(config)
    }
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  }
)

// Add a response interceptor
instance.interceptors.response.use(
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    if (response.data) {
      return response.data
    }
    // failure
    return Promise.reject(response.data)
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error

    // 401 permission denied or token expired
    if (error.response?.status === 401) {
      import('@/stores').then(({ useGlobalLogout }) => {
        const { logout } = useGlobalLogout()
        logout()
      })
    }
    // default
    return Promise.reject(error)
  }
)

export default instance
export { baseURL }
