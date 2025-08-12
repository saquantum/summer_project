<script setup lang="ts">
import AllUsers from '@/views/admin/AllUsers.vue'

const visible = defineModel<boolean>()
</script>

<template>
  <el-dialog v-model="visible">
    <AllUsers></AllUsers>

    <template #footer>
      <div>
        <el-button @click="visible = false">Cancel</el-button>
        <el-button type="primary" @click="visible = false"> Confirm </el-button>
      </div>
    </template>
  </el-dialog>
</template>
