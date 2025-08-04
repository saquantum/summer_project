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
    <el-aside width="250px">
      <router-link to="/">
        <img src="@/assets/uob-logo.svg" class="el-aside__logo" alt="logo" />
      </router-link>

      <div class="el-aside__search">
        <el-input placeholder="Search" size="small" class="custom-search-input">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

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
        >
          <img src="@/assets/logout.png" class="icon-image" alt="logout" />
          <span>Log Out</span>
        </el-button>
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
              <div class="welcome-text">
                <div class="welcome-title">Welcome back</div>
              </div>
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
.icon-image {
  width: 20px;
  margin-right: 8px;
  vertical-align: middle;
  opacity: 0.7;
}

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
    margin: 6px 12px;
    padding-left: 16px !important;
    transition: background-color 0.3s;
  }

  ::v-deep(.el-sub-menu .el-menu .el-menu-item) {
    font-size: 16px;
    color: #424242;
    border-radius: 12px;
    margin: 6px 12px;
    padding-left: 36px !important;
    position: relative;
  }

  ::v-deep(.el-sub-menu .el-menu .el-menu-item)::before {
    content: '';
    position: absolute;
    top: 0;
    left: -12px;
    width: 12px;
    height: 100%;
    border-left: 1px dashed #9f2222;
  }

  ::v-deep(.el-menu-item:hover),
  ::v-deep(.el-menu-item.is-active),
  ::v-deep(.el-sub-menu__title:hover),
  ::v-deep(.el-sub-menu__title.is-active) {
    color: #000 !important;
    font-weight: bold;
    border-radius: 12px;
    margin: 6px 12px;
  }

  .el-aside__search {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 12px 0;

    .custom-search-input {
      width: 220px;

      ::v-deep(.el-input__wrapper) {
        border-radius: 18px;
        background-color: #ffffff;
        box-shadow: none;
        border: 1px solid #dcdfe6;
        padding: 2px 10px;
        height: 36px;

        input {
          font-size: 14px;
          line-height: 20px;
        }

        input::placeholder {
          color: #aaa;
        }

        .el-input__prefix {
          margin-right: 6px;
          color: #555;
        }
      }
    }
  }

  ::v-deep(.el-sub-menu .el-menu) {
    margin-left: 30px;
    padding-left: 0;

    .el-menu-item {
      position: relative;
      padding-left: 25px;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 20px;
        height: 50%;
        border-left: 1px dashed #636262;
        border-bottom: 1px dashed #636262;
        border-bottom-left-radius: 6px;
        box-sizing: border-box;
      }

      &:not(:last-child)::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 0;
        width: 0;
        height: 50%;
        border-left: 1px dashed #636262;
        border-bottom-left-radius: 6px;
        box-sizing: border-box;
      }
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
    .el-menu-item.is-active {
      font-weight: bold;
    }

    ::v-deep(.el-sub-menu__title:hover),
    ::v-deep(.el-menu-item:hover) {
      background-color: rgba(130, 185, 186, 0.62) !important;
      color: #000 !important;
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

.welcome-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;

  .welcome-title {
    font-weight: bold;
    font-size: 16px;
    color: #000;
    margin-left: 7px;
  }
}

::v-deep(.el-dropdown-menu__item) {
  transition: background-color 0.2s ease;
}

::v-deep(.el-dropdown-menu__item:hover) {
  background-color: rgba(199, 202, 202, 0.55);
  color: #000;
}
::v-deep(.el-dropdown-menu__item.logout-item) {
  color: #d32f2f;
  font-weight: bold;
}

::v-deep(.el-dropdown-menu__item.logout-item:hover) {
  background-color: #fddede;
  color: #d32f2f;
}

::v-deep(.icon-badge .el-badge__content.is-dot) {
  top: 10px;
  right: 6px;
  background-color: #933232;
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
    padding: 0px, 2px;
    padding-bottom: 50px;
  }
}

@media (max-width: 480px) {
}
</style>
