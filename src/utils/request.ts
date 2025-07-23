import axios from 'axios'
import { v4 as uuidv4 } from 'uuid'

const baseURL = '/api'

const instance = axios.create({
  baseURL,
  timeout: 10000
})

// Add a request interceptor
instance.interceptors.request.use(
  function (config) {
    if (config.method?.toLowerCase() === 'post') {
      config.headers = config.headers || {}
      config.headers['Idempotency-Key'] = uuidv4()
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
