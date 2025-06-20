import { assetsGetInfoService } from '@/api/assets'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAssetsStore = defineStore(
  'rain-assets',
  () => {
    const assets = ref([])

    const getAssets = async (id) => {
      const res = await assetsGetInfoService(id)
      assets.value = res.data.data
    }

    const reset = () => {
      assets.value = []
    }
    return { assets, getAssets, reset }
  },
  {
    persist: true
  }
)
