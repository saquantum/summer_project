<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/index.ts'
import { adminGetUserInfoService } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = ref()
interface DescriptionItem {
  label: string
  value: string
}
const descriptionsItem = ref<DescriptionItem[]>([])

const userFormRef = ref()
const checkboxOptions = ref([
  { label: 'Add new asset', value: false },
  { label: 'Add polygon', value: false },
  { label: 'Update polygon', value: false },
  { label: 'Update basic information', value: false },
  { label: 'Delete asset', value: false },
  { label: 'Update profile', value: false }
])

const proxyUser = () => {
  // goto user interface
  const id = route.query.id
  if (typeof id === 'string') {
    userStore.setProxyId(id)
    router.push({ path: '/myassets/manage', query: { id: id } })
    // router.push('/myassets/manage')
  }
}

const isEdit = ref(false)
const submit = () => {
  userFormRef.value.submit()
}
onMounted(async () => {
  const id = route.query.id
  if (typeof id === 'string') {
    const res = await adminGetUserInfoService(id)
    console.log(res)
    user.value = res.data
  }

  const arr = user.value.assetHolder.name.split(' ')
  descriptionsItem.value = [
    { label: 'User id', value: user.value.id },
    { label: 'First name', value: arr[0] },
    { label: 'Last name', value: arr[1] },
    { label: 'Email', value: user.value.assetHolder.email ?? '' },
    { label: 'Phone', value: user.value.assetHolder.phone ?? '' },
    {
      label: 'Street',
      value: user.value.assetHolder.address?.street ?? ''
    },
    {
      label: 'Postcode',
      value: user.value.assetHolder.address?.postcode ?? ''
    },
    { label: 'City', value: user.value.assetHolder.address?.city ?? '' },
    {
      label: 'Country',
      value: user.value.assetHolder.address?.country ?? ''
    }
  ]
  console.log(descriptionsItem)
})
</script>

<template>
  <UserForm ref="userFormRef" v-model:isEdit="isEdit"></UserForm>
  <div>
    <h3>Permission</h3>
    <el-checkbox
      v-for="(item, index) in checkboxOptions"
      :key="index"
      :label="item.label"
    />
  </div>

  <el-button @click="proxyUser" v-if="!isEdit"> Proxy as this user</el-button>
  <el-button @click="isEdit = true" v-if="!isEdit">Edit</el-button>
  <el-button @click="isEdit = false" v-else>Cancel</el-button>
  <el-button v-if="isEdit" @click="submit">Submit</el-button>
  <el-button type="danger" v-if="isEdit"> Delete </el-button>
</template>
