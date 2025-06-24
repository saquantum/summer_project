<script setup>
import {
  Management,
  User,
  Crop,
  EditPen,
  SwitchButton,
  CaretBottom,
  Bell
} from '@element-plus/icons-vue'
import avatar from '@/assets/default.png'
import { ref } from 'vue'
import { useAssetsStore, useUserStore, useAdminStore } from '@/stores'
import { useRouter, useRoute } from 'vue-router'

const userStore = useUserStore()
const assetsStore = useAssetsStore()
const adminStore = useAdminStore()
const router = useRouter()
const route = useRoute()
const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.reset()
    assetsStore.reset()
    adminStore.reset()
    router.push('/login')
  }
}
const tabs = [
  { label: 'My Assets', path: '/', icon: Management },
  { label: 'My Profile', path: '/user/profile', icon: User }
]
const dialogVisible = ref(false)
</script>

<template>
  <el-container class="layout-container">
    <!-- user interface -->
    <el-aside v-if="userStore.user.admin === false" width="200px">
      <div class="el-aside__logo"></div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#528add"
        :default-active="$route.path"
        text-color="#fff"
        router
      >
        <el-menu-item index="/myassets/manage">
          <el-icon><Management /></el-icon>
          <span>My assets</span>
        </el-menu-item>

        <el-menu-item index="/user/profile">
          <el-icon><User /></el-icon>
          <span>My Profile</span>
        </el-menu-item>
      </el-menu>
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
          <el-icon @click="dialogVisible = true" class="bell">
            <Bell />
          </el-icon>

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
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-aside {
    background-color: #528add;

    &__logo {
      height: 120px;
      background: url('@/assets/logo.webp') no-repeat center / 120px auto;
    }
    .el-menu {
      border-right: none;
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
