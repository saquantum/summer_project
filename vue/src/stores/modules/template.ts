import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminGetTemplateSerive } from '../../api/admin'
import type { Template } from '@/types'

export const useTemplateStore = defineStore(
  'rain-template',
  () => {
    const templates = ref<Template[]>([])

    const getTemplates = async (
      offset: number,
      limit: number,
      orderList: string
    ) => {
      try {
        const { data } = await adminGetTemplateSerive(offset, limit, orderList)
        templates.value = data
      } catch (e) {
        console.error(e)
      }
    }

    return { templates, getTemplates }
  },
  {
    persist: true
  }
)
