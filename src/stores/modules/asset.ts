import { assetsGetInfoService } from '@/api/assets'
import { adminGetAssetsService, adminGetUserAssetsService } from '@/api/admin'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AssetWithWarnings } from '@/types'

export const useAssetStore = defineStore(
  'rain-assets',
  () => {
    const userAssets = ref<AssetWithWarnings[]>([])
    const allAssets = ref<AssetWithWarnings[]>([])

    const getUserAssets = async (admin: boolean, id: string) => {
      try {
        const res = admin
          ? await adminGetUserAssetsService(id)
          : await assetsGetInfoService(id)
        if (res.data) {
          userAssets.value = res.data
        } else {
          userAssets.value = []
        }
      } catch (e) {
        console.error('fail to get user asset', e)
      }
    }

    const getAllAssets = async (
      offset: number,
      limit: number,
      orderList: string
    ) => {
      try {
        const res = await adminGetAssetsService(offset, limit, orderList)
        allAssets.value = res.data
      } catch (e) {
        console.error('fail to get all asset', e)
      }
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
