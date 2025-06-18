<script setup>
import { onMounted } from 'vue'
import { useAssetsStore } from '@/stores/modules/assets'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useRouter } from 'vue-router'

const assetsStore = useAssetsStore()
const router = useRouter()

onMounted(() => {
  assetsStore.getAssets()

  assetsStore.assets.forEach((asset, index) => {
    const map = L.map('map-' + index).setView([0, 0], 13)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map)
    const geoLayer = L.geoJSON(asset.drainArea).addTo(map)
    map.fitBounds(geoLayer.getBounds())
  })
})
</script>

<template>
  <div class="assets-container">
    <div class="card-grid">
      <el-card
        v-for="(asset, index) in assetsStore.assets"
        :key="asset.id"
        class="asset-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <h3 class="asset-title">{{ asset.name || 'Asset Name' }}</h3>
            <span class="asset-id">ID: {{ asset.id }}</span>
          </div>
        </template>

        <div class="map-container">
          <div :id="'map-' + index" class="map"></div>
        </div>

        <template #footer>
          <div class="card-footer">
            <el-button
              type="primary"
              @click="router.push(`/asset/${asset.id}`)"
              class="view-details-btn"
            >
              Show Detail
            </el-button>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.assets-container {
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.card-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: flex-start;
  max-width: 1200px;
  margin: 0 auto;
}

.asset-card {
  width: 300px;
  height: 360px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.asset-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.asset-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
}

.asset-id {
  font-size: 12px;
  color: #7f8c8d;
  background: #ecf0f1;
  padding: 4px 8px;
  border-radius: 12px;
}

.map-container {
  padding: 16px 0;
  display: flex;
  justify-content: center;
}

.map {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-footer {
  display: flex;
  justify-content: center;
  padding: 0;
}

.view-details-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

@media (max-width: 768px) {
  .assets-container {
    padding: 12px;
  }

  .card-grid {
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .card-grid {
    justify-content: flex-start;
  }
}

:deep(.el-card__header) {
  padding: 12px 16px;
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-card__footer) {
  background: #fafafa;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
}
</style>
