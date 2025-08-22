<script setup lang="ts">
import {
  useRoute,
  type RouteLocationRaw,
  type RouteLocationNormalizedLoaded
} from 'vue-router'

type TabItem = {
  label: string
  to: RouteLocationRaw
  disabled?: boolean
  match?: (_route: RouteLocationNormalizedLoaded) => boolean
}

defineProps<{ tabs: TabItem[] }>()
const route = useRoute()
const extraActive = (tab: TabItem) => (tab.match ? tab.match(route) : false)
</script>

<template>
  <div class="top-tabs">
    <RouterLink
      v-for="t in tabs"
      :key="t.label"
      :to="t.to"
      custom
      v-slot="{ navigate, isActive, isExactActive }"
    >
      <button
        type="button"
        class="tab-btn"
        :class="{
          active: extraActive(t) || isActive || isExactActive,
          disabled: t.disabled
        }"
        :disabled="t.disabled"
        @click="!t.disabled && navigate()"
      >
        {{ t.label }}
      </button>
    </RouterLink>
  </div>
</template>

<style scoped>
.top-tabs {
  position: absolute;
  left: 0;
  top: -35px;
  display: inline-flex;
  gap: 8px;
  z-index: 10;
}
.tab-btn {
  appearance: none;
  border: 0;
  padding: 8px 16px;
  border-radius: 8px 8px 0 0;
  background: #e6eaef;
  color: #2b3a4b;
  font-weight: 700;
  font-size: 16px;
  line-height: 1;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}
.tab-btn:hover {
  background: #dce3ea;
}
.tab-btn.active {
  background: #06365f;
  color: #fff;
}
.tab-btn.disabled,
.tab-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
@media (max-width: 768px) {
  .top-tabs {
    top: -29px;
  }
  .tab-btn {
    font-size: 14px;
    padding: 6px 12px;
  }
}
</style>
