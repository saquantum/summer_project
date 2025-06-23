import { userGetIdService, userGetInfoService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const user = ref({})
    const proxyId = ref('')
    const getUser = async () => {
      const { data } = await userGetIdService()
      if (!data.data.isAdmin) {
        const res = await userGetInfoService(data.data.id)
        user.value = res.data.data[0]
      }
      user.value.isAdmin = data.data.isAdmin
    }

    const setProxyId = (id) => {
      proxyId.value = id
    }

    const reset = () => {
      user.value = {}
    }
    return { user, getUser, reset, proxyId, setProxyId }
  },
  {
    persist: true
  }
)
