<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { adminGetUserInfoService } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = ref()

const descriptionsItem = ref([])

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
  userStore.setProxyId(route.query.id)
  router.push({ path: '/myassets/manage', query: { id: route.query.id } })
  // router.push('/myassets/manage')
}

const setEdit = (val) => {
  userFormRef.value.setEdit(val)
}

onMounted(async () => {
  const id = route.query.id
  const res = await adminGetUserInfoService(id)
  console.log(res)
  user.value = res.data

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
  <UserForm ref="userFormRef"></UserForm>

  <div>
    <h3>Permission</h3>
    <el-checkbox
      v-for="(item, index) in checkboxOptions"
      :key="index"
      :label="item.label"
    />
  </div>

  <el-button @click="proxyUser"> Proxy as this user</el-button>
  <el-button @click="setEdit(true)">Edit</el-button>
  <el-button @click="setEdit(false)">Cancel</el-button>
  <el-button type="danger"> Delete </el-button>
</template>
