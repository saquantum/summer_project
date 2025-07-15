import { userGetInfoService, userLoginService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

import type { LoginForm, User, UserItem } from '@/types'
import { ElMessage } from 'element-plus'
import { adminGetUsersService } from '@/api/admin'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const user = ref<User | null>(null)
    const proxyId = ref('')
    const users = ref<UserItem[]>([])

    const searchHistory = ref<string[]>([])

    const getUser = async (form: LoginForm) => {
      try {
        const res = await userLoginService(form)
        user.value = res.data
        if (!user.value?.admin) getUserInfo()
        ElMessage.success('Success')
      } catch (e) {
        ElMessage.error('Username or password is incorrect.')
        throw new Error(String(e))
      }
    }

    const getUsers = async (
      func: string,
      offset: number,
      limit: number,
      sortStr: string
    ) => {
      const res = await adminGetUsersService(func, offset, limit, sortStr)
      console.log(res)
      users.value = res.data
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
      searchHistory.value = []
    }
    return {
      user,
      users,
      searchHistory,
      getUser,
      reset,
      proxyId,
      setProxyId,
      updateUser,
      getUserInfo,
      getUsers
    }
  },
  {
    persist: true
  }
)
