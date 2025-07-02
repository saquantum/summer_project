import { assetsGetInfoService } from '@/api/assets'
import { adminGetAssetsService, adminGetUserAssetsService } from '@/api/admin'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAssetStore = defineStore(
  'rain-assets',
  () => {
    const userAssets = ref([])
    const allAssets = ref([])

    const getUserAssets = async (admin, id) => {
      let res
      if (admin) {
        res = await adminGetUserAssetsService(id)
      } else res = await assetsGetInfoService(id)
      console.log(res)
      userAssets.value = res.data
    }

    const getAllAssets = async (offset, limit, orderList) => {
      const res = await adminGetAssetsService(offset, limit, orderList)
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
