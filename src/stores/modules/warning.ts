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
      allWarnings.value = res.data
    }
    const getAllLiveWarnings = async () => {
      const res = await adminGetAllLiveWarningsService()
      liveWarnings.value = res.data
    }
    const outdatedWarnings = computed(() =>
      allWarnings.value.filter(
        (item) => !liveWarnings.value.some((live) => live.id === item.id)
      )
    )
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
