<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'
const router = useRouter()

const src =
  'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const asset = ref({
  id: 1
})
</script>
<template>
  <el-card
    @click="router.push(`/asset/${asset.id}`)"
    style="width: 90%; max-width: 400px"
  >
    <template #header>Asset Name</template>
    <el-image :src="src"></el-image>
  </el-card>
</template>
