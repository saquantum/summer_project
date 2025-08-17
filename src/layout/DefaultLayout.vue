<script setup lang="ts">
import {
  User,
  SwitchButton,
  CaretBottom,
  Message,
  Operation,
  ArrowLeft,
  Lock
} from '@element-plus/icons-vue'
import { ref, watch, computed } from 'vue'
import { useUserStore, useMailStore } from '@/stores/index.ts'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useGlobalLogout } from '@/utils/logout'

const userStore = useUserStore()
const mailStore = useMailStore()
const router = useRouter()
const route = useRoute()

const { logout } = useGlobalLogout()
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
  if (userStore.user?.admin) router.push('/admin/message/send')
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
    <el-aside width="250px">
      <router-link to="/">
        <img src="@/assets/uob-logo.svg" class="el-aside__logo" alt="logo" />
      </router-link>

      <SideMenu
        :visible="true"
        :showUserSideBar="showUserSideBar"
        :activeIndex="activeIndex"
      />

      <div class="signout-container">
        <el-button
          text
          type="danger"
          size="large"
          @click="logout"
          class="signout-button"
        >
          <svg
            t="1754387182843"
            class="icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            p-id="4432"
            width="20"
            height="20"
          >
            <path
              d="M835.669333 554.666667h-473.173333A42.453333 42.453333 0 0 1 320 512a42.666667 42.666667 0 0 1 42.474667-42.666667h473.173333l-161.813333-161.834666a42.666667 42.666667 0 0 1 60.330666-60.330667l234.666667 234.666667a42.666667 42.666667 0 0 1 0 60.330666l-234.666667 234.666667a42.666667 42.666667 0 0 1-60.330666-60.330667L835.669333 554.666667zM554.666667 42.666667a42.666667 42.666667 0 1 1 0 85.333333H149.525333C137.578667 128 128 137.578667 128 149.482667v725.034666C128 886.4 137.6 896 149.525333 896H554.666667a42.666667 42.666667 0 1 1 0 85.333333H149.525333A106.816 106.816 0 0 1 42.666667 874.517333V149.482667A106.773333 106.773333 0 0 1 149.525333 42.666667H554.666667z"
              fill="currentColor"
              p-id="4433"
            ></path>
          </svg>
          <span>&nbsp;&nbsp;Log Out</span>
        </el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header style="display: flex; justify-content: space-between">
        <div class="header-left">
          <el-button aria-label="Go back" @click="router.go(-1)" :plain="true">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button
            class="mobile-menu"
            @click="mobileMenuVisible = true"
            plain
          >
            <el-icon><Operation /></el-icon>
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
          <el-badge
            :is-dot="mailStore.unreadMails.length > 0"
            class="icon-badge"
          >
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
                <el-dropdown-item command="password" :icon="Lock">
                  Change password
                </el-dropdown-item>
                <el-dropdown-item
                  command="logout"
                  :icon="SwitchButton"
                  class="logout-item"
                >
                  Log out
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view></router-view>
        <SearchDialog v-model:visible="searchDialogVisible"></SearchDialog>
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-main {
    background-color: #ffffff;
    padding: 20px;
  }
  ::v-deep(.el-menu > .el-menu-item),
  ::v-deep(.el-sub-menu__title) {
    font-size: 18px;
    color: #424242;
    border-radius: 12px;
    margin: 0;
    padding-left: 16px !important;
    transition: background-color 0.3s;
  }
  ::v-deep(.el-sub-menu .el-menu .el-menu-item) {
    font-size: 16px;
    color: #424242;
    border-radius: 8px;
    margin: 4px 30px;
    padding-left: 20px !important;
  }

  ::v-deep(.el-menu-item:hover),
  ::v-deep(.el-menu-item.is-active),
  ::v-deep(.el-sub-menu__title:hover),
  ::v-deep(.el-sub-menu__title.is-active) {
    color: #000 !important;
    font-weight: bold;
    border-radius: 12px;
    margin: 0;
    background-color: rgba(163, 205, 168, 0.76);
    position: relative;
    z-index: 2;
  }

  ::v-deep(.el-sub-menu > .el-menu) {
    position: relative;
    margin-left: 30px;
    padding-left: 0;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 10px;
      width: 1px;
      height: 100%;
      border-left: 1.2px dashed #636262;
      z-index: 0;
    }

    .el-menu-item {
      position: relative;
      padding-left: 20px;
      min-width: 200px;
    }
  }

  .el-aside {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #f2f2f3;

    &__logo {
      box-sizing: border-box;
      width: 100%;
      padding: 20px;
    }
    .el-menu {
      border-right: none;
      flex: 1;
      overflow-y: auto;
    }

    .signout-container {
      margin-top: auto;
      padding: 20px;
      display: flex;
      justify-content: center;

      .signout-button {
        background-color: transparent;
        color: #656363;
        font-size: 16px;
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
        font-size: 28px;
        padding-top: 6px;
        color: #999;
        cursor: pointer;
        transition: all 0.3s ease;
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

::v-deep(.el-dropdown-menu__item) {
  transition: background-color 0.2s ease;
}

::v-deep(.el-dropdown-menu__item:hover) {
  background-color: rgba(163, 205, 168, 0.76);
  color: #000;
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

  .el-main {
    padding: 0px 2px;
    padding-bottom: 50px;
  }
}

@media (max-width: 480px) {
}
</style>
