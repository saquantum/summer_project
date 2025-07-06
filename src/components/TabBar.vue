<script setup lang="ts">
const props = defineProps({
  tabs: Object
})
</script>

<template>
  <nav class="bottom-nav">
    <router-link
      v-for="item in props.tabs"
      :key="item.path"
      :to="item.path"
      class="nav-item"
    >
      <el-icon :size="20"><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border-radius: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 8px 16px;
  display: flex;
  gap: 24px;
  align-items: center;
  z-index: 1000;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
  color: #666;
  text-decoration: none;
  transition: color 0.2s ease;
}

.nav-item.router-link-active {
  color: var(--el-color-primary);
}
</style>
