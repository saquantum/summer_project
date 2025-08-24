import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { Mail } from '@/types'
import { useUserStore } from './user'
import { getMailService } from '@/api/mail'

export const useMailStore = defineStore(
  'rain-mail',
  () => {
    const mails = ref<Mail[]>([])

    const unreadMails = computed(() => {
      if (!mails.value || mails.value.length <= 0) return []
      return mails.value.filter((mail) => !mail.hasRead)
    })
    const getMails = async () => {
      try {
        const userStore = useUserStore()
        if (userStore.user && !userStore.user.admin) {
          const res = await getMailService(userStore.user.id)
          console.log(res)
          if (res.data) {
            mails.value = res.data ?? []
          }
        } else if (userStore.user && userStore.user.admin) {
          // change api later
          const res = await getMailService(userStore.proxyId)
          if (res.data) {
            mails.value = res.data ?? []
          }
        }
      } catch (e) {
        console.error(e)
      }
    }

    return { mails, unreadMails, getMails }
  },
  {
    persist: true
  }
)
