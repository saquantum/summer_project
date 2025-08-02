import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  adminGetAllLiveWarningsService,
  adminGetAllWarningsService
} from '@/api/admin'
import type { Warning } from '@/types'

export const useWarningStore = defineStore(
  'rain-warnings',
  () => {
    const allWarnings = ref<Warning[]>([])
    const liveWarnings = ref<Warning[]>([])

    const getAllWarnings = async () => {
      const res = await adminGetAllWarningsService()
      if (res.data && res.data.length > 0) allWarnings.value = res.data
    }
    const getAllLiveWarnings = async () => {
      const res = await adminGetAllLiveWarningsService()
      if (res.data && res.data.length > 0) liveWarnings.value = res.data
    }
    const outdatedWarnings = computed(() => {
      if (!allWarnings.value || allWarnings.value.length <= 0) return []
      return allWarnings.value.filter(
        (item) => !liveWarnings.value.some((live) => live.id === item.id)
      )
    })
    return {
      getAllWarnings,
      getAllLiveWarnings,
      outdatedWarnings,
      allWarnings,
      liveWarnings
    }
  },
  {
    persist: true
  }
)
