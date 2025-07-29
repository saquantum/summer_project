<script setup lang="ts"></script>

<template>
  <div class="login-page">
    <div class="card-wrapper">
      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  background: url('@/assets/login_bg.png') no-repeat center / cover;
}

.card-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.glass-effect {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  width: 90%;
  max-width: 400px;
}

@media (min-width: 2560px) {
  .glass-effect {
    max-width: 700px !important;
    padding: 48px 40px;
  }
}
</style>
