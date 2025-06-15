import { userGetInfoService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const token = ref('')
    const setToken = (newToken) => {
      token.value = newToken
    }

    const user = ref({})
    const getUser = async () => {
      const res = await userGetInfoService()
      user.value = res.data.data
    }
    return { token, setToken, user, getUser }
  },
  {
    persist: true
  }
)
