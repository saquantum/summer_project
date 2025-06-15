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
import { onMounted, ref } from 'vue'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
onMounted(() => {
  userStore.getUser()
})

const dialogVisible = ref(false)
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="200px">
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

    <el-container>
      <el-header>
        <div>username</div>

        <div class="header-right">
          <el-icon @click="dialogVisible = true" class="bell">
            <Bell />
          </el-icon>

          <el-dropdown placement="bottom-end">
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
      </el-main>

      <el-footer>This is a footer</el-footer>
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
      background: url('@/assets/logo.png') no-repeat center / 120px auto;
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
</style>
