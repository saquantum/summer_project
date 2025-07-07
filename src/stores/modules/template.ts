import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminGetTemplateSerive } from '../../api/admin'
import type { Template } from '@/types'

export const userTemplateStore = defineStore(
  'rain-template',
  () => {
    const templates = ref<Template[]>([])

    const getTemplates = async () => {
      const { data } = await adminGetTemplateSerive(0, 50, '')
      templates.value = data
    }

    const reset = () => {
      templates.value = []
    }
    return { templates, getTemplates, reset }
  },
  {
    persist: true
  }
)
