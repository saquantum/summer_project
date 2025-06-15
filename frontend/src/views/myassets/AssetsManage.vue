<script setup>
import AssetCard from '@/components/AssetCard.vue'
</script>
<template>
  <div>
    <el-input v-model="input" style="width: 240px" placeholder="Please input" />
    <el-input v-model="input" style="width: 240px" placeholder="Please input" />
    <el-input v-model="input" style="width: 240px" placeholder="Please input" />
  </div>
  <div class="card-wrapper">
    <AssetCard v-for="item in 3" :key="item"></AssetCard>
  </div>
</template>

<style scoped>
.card-wrapper {
  margin-top: 20px;
  display: flex;
  gap: 20px;
}
</style>
