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
  router.push('/myassets/manage')
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
  <el-descriptions title="User Info" :column="2" border>
    <el-descriptions-item label="Avatar">
      <el-avatar :size="size" :src="circleUrl" />
    </el-descriptions-item>
    <el-descriptions-item
      v-for="(item, index) in descriptionsItem"
      :key="index"
      :label="item.label"
    >
      {{ item.value }}</el-descriptions-item
    >
  </el-descriptions>

  <div>
    <h3>Permission</h3>
    <el-checkbox
      v-for="(item, index) in checkboxOptions"
      :key="index"
      :label="item.label"
    />
  </div>

  <el-button @click="proxyUser"> Proxy as this user</el-button>
  <el-button type="danger"> Delete </el-button>
</template>
