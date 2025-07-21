import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Mail } from '@/types'
import { useUserStore } from './user'
import { getMailService } from '@/api/mail'

export const useMailStore = defineStore(
  'rain-mail',
  () => {
    const mails = ref<Mail[]>([])

    const getMails = async () => {
      const userStore = useUserStore()
      if (userStore.user && !userStore.user.admin) {
        const res = await getMailService(userStore.user.id)
        mails.value = res.data
      } else if (userStore.user && userStore.user.admin) {
        // change api later
        const res = await getMailService(userStore.proxyId)
        mails.value = res.data
      }
    }

    const reset = () => {
      mails.value = []
    }
    return { mails, getMails, reset }
  },
  {
    persist: true
  }
)
