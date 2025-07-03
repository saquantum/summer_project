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
      user.value = data
      if (!user.value.admin) getUserInfo()
    }

    const getUserInfo = async () => {
      const { data } = await userGetInfoService(user.value.id)
      user.value = data
    }

    const updateUser = (obj) => {
      user.value = obj
    }

    const setProxyId = (id) => {
      proxyId.value = id
    }

    const reset = () => {
      user.value = {}
      proxyId.value = ''
    }
    return {
      user,
      getUser,
      reset,
      proxyId,
      setProxyId,
      updateUser,
      getUserInfo
    }
  },
  {
    persist: true
  }
)
