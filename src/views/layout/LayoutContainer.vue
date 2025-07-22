<script setup lang="ts">
import {
  User,
  Crop,
  SwitchButton,
  CaretBottom,
  Message,
  Operation,
  Search
} from '@element-plus/icons-vue'
import { ref, watch, computed } from 'vue'
import { useAssetStore, useUserStore } from '@/stores/index.ts'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const assetStore = useAssetStore()
const router = useRouter()
const route = useRoute()

const logout = () => {
  userStore.reset()
  assetStore.reset()
  router.push('/login')
}
const handleCommand = (command: string) => {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    if (userStore.user && !userStore.user.admin) {
      router.push('/user/profile')
    } else {
      ElMessage.error('Admin currently does not have profile')
    }
  } else if (command === 'password') {
    router.push('/security/verify-mail')
  }
}

const handleMailClick = () => {
  if (userStore.user?.admin) router.push('/admin/message')
  else router.push('/message')
}

const activeIndex = ref(route.path)

const mobileMenuVisible = ref(false)

const searchDialogVisible = ref(false)

const showUserSideBar = computed(() => {
  if (!userStore.user?.admin) {
    return true
  } else if (userStore.user.admin && userStore.proxyId) {
    return true
  }
  return false
})
watch(
  () => route.path,
  (newPath) => {
    activeIndex.value = newPath
  }
)
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <router-link to="/">
        <img src="@/assets/uob-logo.svg" class="el-aside__logo" alt="logo" />
      </router-link>

      <SideMenu
        :showUserSideBar="showUserSideBar"
        :activeIndex="activeIndex"
      ></SideMenu>

      <div class="signout-container">
        <el-button
          text
          type="danger"
          size="large"
          @click="logout"
          class="signout-button"
          >Sign out</el-button
        >
      </div>
    </el-aside>

    <el-container>
      <el-header style="display: flex; justify-content: space-between">
        <div class="header-left">
          <el-button
            class="mobile-menu"
            @click="mobileMenuVisible = true"
            plain
          >
            <el-icon><Operation /></el-icon>
          </el-button>
          <el-button @click="searchDialogVisible = true">
            <el-icon><Search /></el-icon>
          </el-button>
        </div>

        <el-drawer v-model="mobileMenuVisible" direction="ltr" size="350">
          <SideMenu
            v-model:visible="mobileMenuVisible"
            :showUserSideBar="showUserSideBar"
            :activeIndex="activeIndex"
          ></SideMenu>
        </el-drawer>
        <div class="header-right">
          <el-badge is-dot class="icon-badge">
            <el-icon @click="handleMailClick" class="bell">
              <Message />
            </el-icon>
          </el-badge>

          <el-dropdown placement="bottom-end" @command="handleCommand">
            <span class="el-dropdown__box">
              <el-avatar :src="userStore.user?.avatar" />
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">
                  My profile
                </el-dropdown-item>
                <el-dropdown-item command="avatar" :icon="Crop">
                  Change avatar
                </el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton">
                  Log out
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view></router-view>
        <CustomerService v-if="!userStore.user?.admin"></CustomerService>
        <SearchDialog v-model:visible="searchDialogVisible"></SearchDialog>
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #528add;

    &__logo {
      box-sizing: border-box;
      width: 100%;
      padding: 20px;
    }
    .el-menu {
      border-right: none;
    }
    .el-menu-item.is-active {
      font-weight: bold;
    }

    .signout-container {
      margin-top: auto;
      padding: 20px;
      display: flex;
      justify-content: center;

      .signout-button {
        background-color: transparent;
      }
    }
  }

  .el-header {
    background-color: #fff;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .bell {
        font-size: 24px;
        color: #999;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          color: #409eff;
          transform: scale(1.2);
        }
      }
    }
    .el-dropdown__box {
      display: flex;
      align-items: center;
      outline: none;
      .el-icon {
        color: #999;
        margin-left: 10px;
      }
    }
  }

  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: #666;
  }
}

@media (min-width: 768px) {
  .tabbar-disply {
    display: none;
  }
  .mobile-menu {
    display: none !important;
  }
}

@media (max-width: 768px) {
  .el-aside {
    display: none !important;
  }
}

@media (max-width: 480px) {
}
</style>
