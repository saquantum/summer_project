import { userGetInfoService, userLoginService } from '@/api/user'
import { defineStore } from 'pinia'
import { ref } from 'vue'

import type { LoginForm, User, UserItem, UserSearchBody } from '@/types'
import { ElMessage } from 'element-plus'
import { adminSearchUsersService } from '@/api/admin'

export const useUserStore = defineStore(
  'rain-user',
  () => {
    const user = ref<User | null>(null)
    const proxyId = ref('')
    const users = ref<UserItem[]>([])

    const searchHistory = ref<string[]>([])
    const userSearchHistory = ref<string[]>([])

    const getUser = async (form: LoginForm) => {
      try {
        await userLoginService(form)
        const res = await userGetInfoService(form.username)
        user.value = res.data
        ElMessage.success('Success')
      } catch (e) {
        const status = (e as { response?: { status?: number } })?.response
          ?.status
        if (status === 401) {
          ElMessage.error('Username or password is incorrect.')
        } else {
          ElMessage.error('Unknown Error')
        }
        throw new Error(String(e))
      }
    }

    const getUsers = async (func: string, obj: UserSearchBody) => {
      try {
        const res = await adminSearchUsersService(func, obj)
        users.value = res.data
      } catch (e) {
        console.error(e)
      }
    }

    const getUserInfo = async () => {
      try {
        const { data } = await userGetInfoService(user.value.id)
        user.value = data
      } catch (e) {
        console.error(e)
      }
    }

    const updateUser = (obj: User) => {
      user.value = obj
    }

    const setProxyId = (id: string) => {
      proxyId.value = id
    }

    return {
      user,
      users,
      searchHistory,
      userSearchHistory,
      getUser,
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
