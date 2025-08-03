<script setup lang="ts">
import {
  User,
  SwitchButton,
  CaretBottom,
  Message,
  Operation,
  Search,
  Lock
} from '@element-plus/icons-vue'
import { ref, watch, computed } from 'vue'
import { useUserStore, useGlobalLogout, useMailStore } from '@/stores/index.ts'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

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
    <el-aside width="200px">
      <router-link to="/">
        <img src="@/assets/uob-logo.svg" class="el-aside__logo" alt="logo" />
      </router-link>

      <SideMenu
        :visible="true"
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
    width: 200px;
    overflow: auto;
    scrollbar-gutter: stable;

    &__logo {
      display: block;
      padding: 16px;
      width: 100%;
      box-sizing: border-box;
      margin-right: auto;
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

  .el-main {
    /* 为主内容区域也预留滚动条空间，保持整体布局稳定 */
    scrollbar-gutter: stable;
    /* 明确设置滚动行为 */
    overflow-y: auto;
    height: calc(100vh - 60px); /* 减去header高度 */
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

  .el-main {
    padding: 0;
    padding-bottom: 50px;
  }
}

@media (max-width: 480px) {
}
</style>
