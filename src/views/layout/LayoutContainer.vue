<script setup>
import {
  Management,
  User,
  Crop,
  EditPen,
  SwitchButton,
  CaretBottom,
  Message,
  MessageBox
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'
import { ref, watch } from 'vue'
import { useAssetStore, useUserStore } from '@/stores'
import { useRouter, useRoute } from 'vue-router'

const userStore = useUserStore()
const assetStore = useAssetStore()
const router = useRouter()
const route = useRoute()

const logout = () => {
  userStore.reset()
  assetStore.reset()
  router.push('/login')
}
const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}

const activeIndex = ref(route.path)

const tabs = [
  { label: 'My Assets', path: '/', icon: Management },
  { label: 'My Profile', path: '/user/profile', icon: User }
]
const dialogVisible = ref(false)

watch(
  () => route.path,
  (newPath) => {
    activeIndex.value = newPath
  }
)
</script>

<template>
  <el-container class="layout-container">
    <!-- user interface -->
    <el-aside
      v-if="
        (userStore.user.admin && !route.path.includes('admin')) ||
        !userStore.user.admin
      "
      width="200px"
    >
      <router-link to="/">
        <img src="@/assets/uob-logo.svg" class="el-aside__logo" alt="logo" />
      </router-link>
      <el-menu
        :default-active="activeIndex"
        active-text-color="#ffd04b"
        background-color="#528add"
        text-color="#fff"
        router
        v-model="activeIndex"
      >
        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <span>My Profile</span>
        </el-menu-item>

        <el-sub-menu index="1">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>Asset</span>
          </template>
          <el-menu-item index="/myassets/manage">
            <span>My assets</span>
          </el-menu-item>
          <el-menu-item
            v-if="activeIndex.startsWith('/asset')"
            :index="activeIndex"
          >
            <span>Asset detail</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/message">
          <el-icon><MessageBox /></el-icon>
          <span>Message</span>
        </el-menu-item>
      </el-menu>
      <div class="signout-container">
        <el-button text type="danger" size="small" @click="logout"
          >Sign out</el-button
        >
      </div>
    </el-aside>

    <!-- admin interface -->
    <el-aside
      v-else-if="userStore.user.admin && route.path.includes('admin')"
      width="200px"
    >
      <div class="el-aside__logo"></div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#528add"
        :default-active="$route.path"
        text-color="#fff"
        router
      >
        <el-menu-item index="/admin/users">
          <el-icon><Management /></el-icon>
          <span>All Users</span>
        </el-menu-item>

        <el-menu-item index="/admin/assets">
          <el-icon><User /></el-icon>
          <span>All Assets</span>
        </el-menu-item>

        <el-menu-item index="/admin/warnings">
          <el-icon><User /></el-icon>
          <span>All Warnings</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <el-page-header
          v-if="userStore.user.admin && !route.path.includes('admin')"
          @back="router.go(-1)"
        >
        </el-page-header>
        <div>information</div>
        <div class="header-right">
          <el-badge is-dot class="icon-badge">
            <el-icon @click="router.push('/message')" class="bell">
              <Message />
            </el-icon>
          </el-badge>

          <el-dropdown placement="bottom-end" @command="handleCommand">
            <span class="el-dropdown__box">
              <el-avatar :src="avatar" />
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
                <el-dropdown-item command="password" :icon="EditPen">
                  Reset Password
                </el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton">
                  Log out
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <el-dialog v-model="dialogVisible" title="Tips" width="500">
          <span>This is a message</span>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="dialogVisible = false"> Cancel </el-button>
              <el-button type="primary" @click="dialogVisible = false">
                Confirm
              </el-button>
            </div>
          </template>
        </el-dialog>
      </el-header>

      <el-main>
        <router-view></router-view>
        <TabBar :tabs="tabs" class="tabbar-disply"></TabBar>
        <CustomerService></CustomerService>
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

    .signout-container {
      margin-top: auto;
      padding: 10px;
      display: flex;
      justify-content: center;
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
}

@media (max-width: 768px) {
  .el-aside {
    display: none;
  }
}

@media (max-width: 480px) {
}
</style>
