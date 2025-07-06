import { userGetInfoService, userLoginService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

import type { LoginForm, User } from '@/types'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const user = ref<User | null>(null)
    const proxyId = ref('')
    const getUser = async (form: LoginForm) => {
      const { data } = await userLoginService(form)
      user.value = data
      if (!user.value?.admin) getUserInfo()
    }

    const getUserInfo = async () => {
      if (!user.value) return
      const { data } = await userGetInfoService(user.value.id)
      user.value = data
    }

    const updateUser = (obj: User) => {
      user.value = obj
    }

    const setProxyId = (id: string) => {
      proxyId.value = id
    }

    const reset = () => {
      user.value = null
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
