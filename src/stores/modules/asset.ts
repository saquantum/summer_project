import { assetsGetInfoService, getAssetByIdService } from '@/api/assets'

import {
  adminGetUserAssetsService,
  adminSearchAssetService,
  adminGetAssetByIdService
} from '@/api/admin'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  AssetType,
  AssetSearchBody,
  AssetWithWarnings,
  Warning
} from '@/types'
import { getAssetTypesService } from '@/api/assets'
import { useUserStore } from './user'

export const useAssetStore = defineStore(
  'rain-assets',
  () => {
    // watch filter condition
    const warningOrder: Record<string, number> = {
      RED: 3,
      AMBER: 2,
      YELLOW: 1,
      '': 0
    }
    const getMaxWarningLevel = (warnings: Warning[]): Warning | null => {
      if (!warnings.length) return null
      return warnings.reduce((max, cur) => {
        const maxLevel = max.warningLevel || ''
        const curLevel = cur.warningLevel || ''
        return warningOrder[curLevel] > warningOrder[maxLevel] ? cur : max
      }, warnings[0])
    }

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
      try {
        const res = await getAssetTypesService()
        assetTypes.value = res.data
      } catch (e) {
        console.error(e)
      }
    }

    const getUserAssets = async (admin: boolean, id: string) => {
      try {
        const res = admin
          ? await adminGetUserAssetsService(id)
          : await assetsGetInfoService(id)
        if (res.data) {
          userAssets.value = res.data
          userAssets.value.forEach(
            (item) => (item.maxWarning = getMaxWarningLevel(item.warnings))
          )
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
        allAssets.value = res.data
        if (!allAssets.value || allAssets.value.length <= 0) return
        allAssets.value.forEach(
          (item) => (item.maxWarning = getMaxWarningLevel(item.warnings))
        )
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

    return {
      userAssets,
      getUserAssets,

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
