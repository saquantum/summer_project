import { userGetInfoService, userLoginService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

import type { LoginForm, User } from '@/types'
import CodeUtil from '@/utils/codeUtil'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const user = ref<User | null>(null)
    const proxyId = ref('')
    const searchHistory = ref<string[]>([])

    const getUser = async (form: LoginForm) => {
      try {
        const res = await userLoginService(form)
        if (CodeUtil.isSuccess(res.code)) {
          user.value = res.data
          if (!user.value?.admin) getUserInfo()
          ElMessage.success('Success')
        } else {
          ElMessage.error('Username or password is incorrect.')
          throw new Error('Username or password is incorrect.')
        }
      } catch (e) {
        throw new Error(String(e))
      }
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
      searchHistory,
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
