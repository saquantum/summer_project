import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminGetTemplateSerive } from '../../api/admin'

export const userTemplateStore = defineStore(
  'rain-template',
  () => {
    const templates = ref([])

    const getTemplates = async () => {
      const { data } = await adminGetTemplateSerive()
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
