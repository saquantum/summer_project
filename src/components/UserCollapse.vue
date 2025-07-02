<script setup>
import { ref } from 'vue'
const props = defineProps({
  users: Object
})
const activeNames = ref(0)

const handleChange = (val) => {
  console.log(val)
}
</script>

<template>
  <el-collapse v-if="props.users" v-model="activeNames" @change="handleChange">
    <el-collapse-item
      v-for="(item, index) in props.users"
      :key="index"
      :name="index"
    >
      <div class="content">
        <div>Asset Holder Id</div>
        <div>{{ item.assetHolderId }}</div>
        <div>
          <div>Permission</div>
          <div style="display: flex; gap: 3px">
            <PermissionIndicator :status="true"></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
            <PermissionIndicator></PermissionIndicator>
          </div>
        </div>

        <div>
          <div>Action</div>
          <div>
            <el-button text type="primary" size="small">Edit</el-button>
            <el-button text type="danger" size="small">Delete</el-button>
          </div>
        </div>
      </div>

      <template #title>
        <div class="title-wrapper">
          <div>{{ item.username }}</div>
          <div>{{ item.count }}</div>
        </div>
      </template>
    </el-collapse-item>
  </el-collapse>
</template>

<style scoped>
.content {
  padding-left: 10px;
}
.title-wrapper {
  padding-left: 10px;
  display: flex;
  justify-content: space-between;
}
</style>
