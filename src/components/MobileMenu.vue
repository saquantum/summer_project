<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const props = defineProps({
  visible: Boolean
})
const activeIndex = ref(route.path)
const emit = defineEmits(['update:visible'])
const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})
const isCollapsed = ref(false)
const handleMenuSelect = () => {
  isCollapsed.value = true
  visible.value = false
}
</script>

<template>
  <el-drawer
    v-model="visible"
    title="I am the title"
    direction="ltr"
    size="500"
  >
    <el-menu
      active-text-color="#ffd04b"
      :default-active="$route.path"
      text-color="black"
      :collapse="isCollapsed"
      router
      @select="handleMenuSelect"
    >
      <el-menu-item index="/admin/dashboard">
        <el-icon><House /></el-icon>
        <span>Dashboard</span>
      </el-menu-item>

      <el-sub-menu index="1">
        <template #title>
          <el-icon><User /></el-icon>
          <span>User</span>
        </template>
        <el-menu-item index="/admin/users">
          <span>All Users</span>
        </el-menu-item>
        <el-menu-item index="/admin/user/add">
          <span>Add User</span>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="2">
        <template #title>
          <el-icon><LocationInformation /></el-icon>
          <span>Asset</span>
        </template>
        <el-menu-item index="/admin/assets">
          <span>All Assets</span>
        </el-menu-item>
        <el-menu-item index="/admin/asset/add">
          <span>Add Asset</span>
        </el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="3">
        <template #title>
          <el-icon><Warning /></el-icon>
          <span>Warning</span>
        </template>
        <el-menu-item index="/admin/warnings">
          <span>All Warning</span>
        </el-menu-item>
        <el-menu-item
          v-if="activeIndex.startsWith('/warning')"
          :index="activeIndex"
        >
          <span>Current Warning</span>
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/admin/message/template">
        <el-icon><CopyDocument /></el-icon>
        <span>Message Template</span>
      </el-menu-item>

      <el-menu-item index="/admin/message">
        <el-icon><MessageBox /></el-icon>
        <span>Message</span>
      </el-menu-item>
    </el-menu>
  </el-drawer>
</template>
