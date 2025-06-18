import { assetsGetInfoService } from '@/api/assets'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAssetsStore = defineStore(
  'rain-assets',
  () => {
    const assets = ref([])

    const getAssets = async () => {
      const res = await assetsGetInfoService(1)
      assets.value = res.data.data
    }
    return { assets, getAssets }
  },
  {
    persist: true
  }
)
