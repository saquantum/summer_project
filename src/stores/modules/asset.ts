import { assetsGetInfoService, getAssetByIdService } from '@/api/assets'

import {
  adminGetUserAssetsService,
  adminSearchAssetService,
  adminGetAssetByIdService
} from '@/api/admin'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AssetType, AssetSearchBody, AssetWithWarnings } from '@/types'
import { userGetAssetTypesService } from '@/api/user'
import { useUserStore } from './user'

export const useAssetStore = defineStore(
  'rain-assets',
  () => {
    const userAssets = ref<AssetWithWarnings[]>([])
    const allAssets = ref<AssetWithWarnings[]>([])

    const assetTypes = ref<AssetType[]>([])

    const typeOptions = computed(() =>
      assetTypes.value.map((item) => ({
        label: item.name,
        value: item.id
      }))
    )

    const getAssetTypes = async () => {
      const res = await userGetAssetTypesService()
      assetTypes.value = res.data
    }

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

    const getAllAssets = async (obj: AssetSearchBody) => {
      try {
        const res = await adminSearchAssetService(obj)
        console.log(obj, res)
        allAssets.value = res.data
      } catch (e) {
        console.error('fail to get all asset', e)
      }
    }

    const updateAssetById = async (id: string) => {
      const userStore = useUserStore()
      try {
        let res
        if (userStore.user?.admin) {
          res = await adminGetAssetByIdService(id)
        } else {
          res = await getAssetByIdService(userStore.user!.id, id)
        }
        if (res?.data) {
          const idx = userAssets.value.findIndex((a) => a.asset.id === id)
          if (idx !== -1) userAssets.value[idx] = res.data[0]

          const idx2 = allAssets.value.findIndex((a) => a.asset.id === id)
          if (idx2 !== -1) allAssets.value[idx2] = res.data[0]
        }
      } catch (e) {
        console.error('fail to update asset', e)
      }
    }

    const reset = () => {
      userAssets.value = []
      allAssets.value = []
      assetTypes.value = []
    }

    return {
      userAssets,
      getUserAssets,
      reset,
      allAssets,
      getAllAssets,
      getAssetTypes,
      assetTypes,
      typeOptions,
      updateAssetById
    }
  },
  {
    persist: true
  }
)
