import { userGetInfoService, userLoginService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const user = ref({})
    const proxyId = ref('')
    const getUser = async (form) => {
      const { data } = await userLoginService(form)

      const res = await userGetInfoService(data.id)

      user.value = res.data
    }

    const setProxyId = (id) => {
      proxyId.value = id
    }

    const reset = () => {
      user.value = {}
      proxyId.value = ''
    }
    return { user, getUser, reset, proxyId, setProxyId }
  },
  {
    persist: true
  }
)
