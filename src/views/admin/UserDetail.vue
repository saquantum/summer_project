<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/index.ts'
import { adminGetUserInfoService } from '@/api/admin'
import type { User } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const user = ref()

const userCardRef = ref()
const isEdit = ref(false)
const headerUser = ref<User | null>(null)

const proxyUser = () => {
  // goto user interface
  const id = route.query.id
  if (typeof id === 'string') {
    userStore.setProxyId(id)
    router.push('/assets')
  }
}

const submit = async () => {
  userCardRef.value.submit()
}

const handleCancel = () => {
  userCardRef.value?.cancelEdit()
  isEdit.value = false
}

onMounted(async () => {
  const id = route.query.id
  if (typeof id === 'string') {
    const res = await adminGetUserInfoService(id)
    user.value = res.data
  }
})
</script>

<template>
  <div class="page-surface">
    <!--div class="page-title">User Profile</div-->
    <div class="top-tabs">
      <button class="tab-btn" type="button">User Profile</button>
    </div>

    <div class="topbar">
      <div class="title-wrap">
        <div class="subtle" v-if="headerUser">
          <span class="muted">uid</span>&nbsp;{{ headerUser.id }}
          <span class="dot">·</span>
          {{ headerUser.name }}
          <span class="dot" v-if="headerUser.contactDetails?.email">·</span>
          <span v-if="headerUser.contactDetails?.email">
            {{ headerUser.contactDetails.email }}
          </span>
        </div>
      </div>

      <UserCard ref="userCardRef" v-model:isEdit="isEdit">
        <template #header-actions>
          <div class="actions-inline">
            <el-button
              v-if="!isEdit"
              class="styled-btn"
              type="primary"
              @click="isEdit = true"
            >
              Edit
            </el-button>

            <template v-else>
              <el-button class="styled-btn btn-cancel" @click="handleCancel">
                Cancel
              </el-button>
              <el-button class="styled-btn" type="success" @click="submit">
                Submit
              </el-button>
            </template>
          </div>
        </template>
      </UserCard>
    </div>
    <el-button class="styled-btn pro-btn" @click="proxyUser" v-if="!isEdit">
      Proxy as this user</el-button
    >
  </div>
</template>

<style scoped>
.page-surface {
  position: relative;
  background: #f3f5f7;
  border: 1px solid #e6eaee;
  border-radius: 3px;
  padding: 20px;
  margin: 60px auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  width: 1100px;
  box-sizing: border-box;
}

.top-tabs {
  position: absolute;
  display: inline-flex;
  gap: 8px;
  left: 0;
  top: -35px;
  border-radius: 10px 10px 0 0;
  overflow: visible;
  box-shadow: none;
  box-sizing: border-box;
  z-index: 3;
}

.top-tabs .tab-btn {
  appearance: none;
  border: none;
  padding: 8px 16px;
  border-radius: 8px 8px 0 0;
  background: #06365f;
  color: #fff;
  font-weight: 600;
  font-size: 18px;
  line-height: 1;
}
/* top bar */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -8px 0 12px;
}
.title-wrap {
  min-width: 0;
}

.subtle {
  margin-top: 4px;
  color: #5b6472;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.subtle .muted {
  opacity: 0.7;
}
.subtle .dot {
  opacity: 0.45;
}
/* same buttons */
.styled-btn {
  padding: 8px 20px;
  font-weight: 600;
  font-size: 16px;
  border-radius: 12px;
  color: #ffffff;
  border: 1px solid #fff;
  text-shadow: 0 4px 4px rgba(0, 0, 0, 0.5);
  background-image: linear-gradient(
    180deg,
    #e4dfd8 0%,
    #9bb7d4 60%,
    rgba(58, 78, 107, 0.58) 100%
  );
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}
.styled-btn:hover {
  background: linear-gradient(
    180deg,
    #f0e6d2 0%,
    rgba(125, 140, 163, 0.44) 100%
  );
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-cancel {
  background: linear-gradient(to bottom, #ffffff, #dfdcdc);
  color: #7f0505;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.15);
}
.styled-btn.danger {
  background: linear-gradient(180deg, #f44336 0%, #b71c1c 100%);
}

.actions-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}
.pro-btn {
  display: block;
  margin: 0 auto;
  background: linear-gradient(130deg, #f0e6d2 0%, rgba(31, 87, 174, 0.44) 100%);
  color: #39435b;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
  font-weight: 800;
}

.pro-btn:hover {
  background: linear-gradient(
    130deg,
    rgba(31, 87, 174, 0.44) 0%,
    rgba(240, 230, 210, 0.73) 100%
  );
  color: #eceff3;
  text-shadow: 0 6px 14px rgba(0, 0, 0, 0.5);
  border: 1px solid #fff;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
  font-weight: 800;
}
/* responsive */
@media (max-width: 768px) {
  .page-surface {
    width: min(1100px, calc(100vw - 24px));
    margin: 40px auto;
    padding: 16px;
  }
  .page-title {
    font-size: 18px;
  }
}
@media (max-width: 480px) {
  .page-surface {
    background: transparent;
    border: 0;
    box-shadow: none;
    padding: 0;
    margin-top: 24px;
  }
}
</style>
