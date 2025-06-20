<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores'
const userStore = useUserStore()

const { assetHolder } = userStore.user
console.log(assetHolder)
const arr = userStore.user.assetHolder.name.split(' ')
const form = ref({
  firstName: arr[0],
  lastName: arr[1],
  email: assetHolder.email,
  address: {
    street: '',
    postCode: '',
    city: '',
    country: ''
  }
})
</script>

<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Personal details</span>
      </div>
    </template>

    <el-form
      :model="form"
      label-position="top"
      label-width="auto"
      style="max-width: 600px"
    >
      <!-- name -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="FIRST NAME">
            <el-input v-model="form.firstName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="LAST NAME">
            <el-input v-model="form.lastName" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- email -->
      <el-form-item label="EMAIL ADDRESS">
        <el-input v-model="form.email" />
      </el-form-item>

      <!-- address -->
      <el-form-item label="ADDRESS">
        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item label="Street" label-width="100px">
              <el-input v-model="form.address.street" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Post Code" label-width="100px">
              <el-input v-model="form.address.postCode" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="width: 100%">
          <el-col :span="12">
            <el-form-item label="City" label-width="100px">
              <el-input v-model="form.address.city" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Country" label-width="100px">
              <el-input v-model="form.address.country" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
