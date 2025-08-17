<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores'
import {
  User,
  MessageBox,
  Back,
  House,
  LocationInformation,
  Warning,
  CopyDocument
} from '@element-plus/icons-vue'

const userStore = useUserStore()

const props = defineProps<{
  visible: boolean
  activeIndex: string
  showUserSideBar: boolean
}>()

const emit = defineEmits(['update:visible'])

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// Helper functions for better route matching
const isAssetDetailRoute = (route: string): boolean => {
  // Match routes like /assets/123, /admin/assets/456 but exclude /assets and /assets/add
  const assetRoutePattern = /^\/(?:admin\/)?assets\/(?!add$)[^/]+(?:\/.*)?$/
  return assetRoutePattern.test(route)
}

const isWarningDetailRoute = (route: string): boolean => {
  // Match routes like /warnings/123, /admin/warnings/456 but exclude base /warnings
  const warningRoutePattern = /^\/(?:admin\/)?warnings\/[^/]+(?:\/.*)?$/
  return warningRoutePattern.test(route)
}

const isCurrentWarningRoute = (route: string): boolean => {
  // More precise matching for warning routes
  return route.startsWith('/warnings/') || route.startsWith('/admin/warnings/')
}

const handleMenuSelect = () => {
  visible.value = false
}

const handleBackToAdmin = () => {
  userStore.proxyId = ''
}
</script>

<template>
  <!-- user interface -->
  <el-menu
    data-test="user-menu"
    :default-active="props.activeIndex"
    active-text-color="#0000"
    background-color="#f2f2f3"
    text-color="#000"
    router
    v-if="props.showUserSideBar"
    @select="handleMenuSelect"
  >
    <el-menu-item index="/user/profile" data-test="my-profile-side">
      <el-icon><User /></el-icon>
      <span>My Profile</span>
    </el-menu-item>

    <el-sub-menu index="1">
      <template #title>
        <el-icon><LocationInformation /></el-icon>
        <span>Asset</span>
      </template>
      <el-menu-item index="/assets">
        <span>My assets</span>
      </el-menu-item>

      <el-menu-item index="/assets/add">
        <span>Add asset</span>
      </el-menu-item>

      <!-- Fixed asset detail route matching -->
      <el-menu-item
        v-if="isAssetDetailRoute(props.activeIndex)"
        :index="props.activeIndex"
      >
        <span>Asset detail</span>
      </el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="3">
      <template #title>
        <el-icon><Warning /></el-icon>
        <span>Warning</span>
      </template>
      <el-menu-item index="/warnings">
        <span>All Warning</span>
      </el-menu-item>

      <!-- Fixed warning detail route matching -->
      <el-menu-item
        v-if="isWarningDetailRoute(props.activeIndex)"
        :index="props.activeIndex"
      >
        <span>Current Warning</span>
      </el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/message" data-test="message-side">
      <el-icon><MessageBox /></el-icon>
      <span>Message</span>
    </el-menu-item>

    <el-menu-item
      v-if="userStore.user?.admin"
      index="/"
      @click="handleBackToAdmin"
    >
      <el-icon><Back /></el-icon>
      <span>Back to admin</span>
    </el-menu-item>
  </el-menu>

  <!-- admin interface -->
  <el-menu
    data-test="admin-menu"
    active-text-color="#ffd04b"
    background-color="#f2f2f3"
    :default-active="props.activeIndex"
    text-color="#000"
    router
    @select="handleMenuSelect"
    v-else
  >
    <el-menu-item index="/admin/dashboard">
      <el-icon><House /></el-icon>
      <span>Dashboard</span>
    </el-menu-item>

    <el-menu-item index="/admin/message/send">
      <el-icon><MessageBox /></el-icon>
      <span>Send to Inbox</span>
    </el-menu-item>

    <el-sub-menu index="4">
      <template #title>
        <el-icon><CopyDocument /></el-icon>
        <span>Message Template</span>
      </template>
      <el-menu-item index="/admin/message/template/manage">
        <span>Manage Template</span>
      </el-menu-item>
      <el-menu-item index="/admin/message/template">
        <span>Edit Template</span>
      </el-menu-item>
    </el-sub-menu>

    <el-divider style="margin: 8px 0" />
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
      <el-menu-item index="/admin/users/permission">
        <span>Access Control</span>
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
      <el-menu-item index="/admin/assets/add">
        <span>Add Assets</span>
      </el-menu-item>
      <el-menu-item index="/admin/assets/types">
        <span>Asset Types</span>
      </el-menu-item>

      <el-menu-item
        v-if="isAssetDetailRoute(props.activeIndex)"
        :index="props.activeIndex"
      >
        <span>Asset detail</span>
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
        v-if="isCurrentWarningRoute(props.activeIndex)"
        :index="props.activeIndex"
      >
        <span>Current Warning</span>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>
