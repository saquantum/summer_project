import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminGetWarningsService } from '@/api/admin'
export const useAdminStore = defineStore(
  'rain-warnings',
  () => {
    const reset = () => {
      allWarnings.value = []
    }

    const allWarnings = ref([])
    const getAllWarnings = async () => {
      const res = await adminGetWarningsService()
      allWarnings.value = res.data.data
    }
    return {
      getAllWarnings,
      allWarnings,
      reset
    }
  },
  {
    persist: true
  }
)
