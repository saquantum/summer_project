import { assetsGetInfoService } from '@/api/assets'
import { adminGetAssetsService } from '@/api/admin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAssetStore = defineStore(
  'rain-assets',
  () => {
    const userAssets = ref([])
    const allAssets = ref([])

    const getUserAssets = async (id) => {
      const res = await assetsGetInfoService(id)
      userAssets.value = res.data
    }

    const getAllAssets = async () => {
      const res = await adminGetAssetsService()
      allAssets.value = res.data
    }

    const reset = () => {
      userAssets.value = []
      allAssets.value = []
    }
    return { userAssets, getUserAssets, reset, allAssets, getAllAssets }
  },
  {
    persist: true
  }
)
