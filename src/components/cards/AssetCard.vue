<script setup lang="ts">
import { Calendar, Tools, Trophy } from '@element-plus/icons-vue'
import type { AssetWithWarnings } from '@/types'
import { useRouter } from 'vue-router'

const router = useRouter()

// Component props
const props = defineProps<{
  item: AssetWithWarnings
}>()

// Get status tag type
const getStatusTagType = (
  status: string
): 'success' | 'info' | 'warning' | 'danger' => {
  const typeMap: Record<string, 'success' | 'info' | 'warning' | 'danger'> = {
    active: 'success',
    inactive: 'info',
    maintenance: 'warning'
  }
  return typeMap[status.toLowerCase()] || 'info'
}

// Get warning class
const getWarningClass = (level: string) => {
  const classMap: Record<string, string> = {
    YELLOW: 'warning-low',
    AMBER: 'warning-medium',
    RED: 'warning-high'
  }
  if (!level) return 'warning-no'
  else return classMap[level]
}

// Get warning text
const getWarningText = (level: string) => {
  const textMap: Record<string, string> = {
    YELLOW: 'Low Risk',
    AMBER: 'Medium Risk',
    RED: 'High Risk'
  }
  return textMap[level] || 'No Risk'
}

// Format date
const formatDate = (dateString: string) => {
  if (!dateString) return 'Unknown'
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// Handle card click
const handleCardClick = () => {
  router.push(`/assets/${props.item.asset.id}`)
}
</script>

<template>
  <div class="asset-card-inner" @click="handleCardClick">
    <!-- Asset image/icon area -->
    <div class="asset-image-container">
      <MapCard
        :map-id="'map-' + item.asset.id"
        :locations="[item.asset.location]"
        :display="true"
      />
      <!-- Warning level tag -->
      <div
        class="warning-tag"
        :class="getWarningClass(item.maxWarning?.warningLevel ?? '')"
      >
        {{ getWarningText(item.maxWarning?.warningLevel ?? '') }}
      </div>
    </div>

    <!-- Asset information area -->
    <div class="asset-info">
      <!-- Asset name -->
      <h3 class="asset-title">{{ item.asset.name || 'Unnamed Asset' }}</h3>

      <!-- Asset id and type -->
      <p class="asset-description">
        {{ item.asset.type.name }} • ID: {{ item.asset.id }}
      </p>

      <!-- Asset status and capacity -->
      <div class="asset-details">
        <div class="detail-item">
          <el-icon size="12"><Trophy /></el-icon>
          <span class="detail-text"
            >Capacity: {{ item.asset.capacityLitres }}L</span
          >
        </div>
        <div class="detail-item">
          <el-icon size="12"><Tools /></el-icon>
          <span class="detail-text">Material: {{ item.asset.material }}</span>
        </div>
        <div class="detail-item">
          <el-icon size="12"><Calendar /></el-icon>
          <span class="detail-text"
            >Last Inspection: {{ formatDate(item.asset.lastInspection) }}</span
          >
        </div>
        <div class="detail-item">
          <el-icon size="12"><Calendar /></el-icon>
          <span class="detail-text"
            >Installed: {{ formatDate(item.asset.installedAt) }}</span
          >
        </div>
      </div>

      <!-- Status -->
      <div class="asset-status">
        <el-tag :type="getStatusTagType(item.asset.status)" size="small">
          {{ item.asset.status }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.asset-card-inner {
  display: flex;
  align-items: stretch;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  margin-bottom: 8px;
  height: 140px;
  width: 100%;
}

.asset-image-container {
  position: relative;
  flex-shrink: 0;
  width: 140px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

  .asset-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .warning-tag {
    position: absolute;
    top: 8px;
    right: 8px;
    color: white;
    padding: 2px 6px;
    border-radius: 10px;
    font-size: 10px;
    font-weight: bold;
    z-index: 400;

    &.warning-no {
      background: green;
    }

    &.warning-low {
      background: #e6a23c;
    }

    &.warning-medium {
      background: #f56c6c;
    }

    &.warning-high {
      background: #ff4757;
    }
  }
}

.asset-info {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;

  .asset-title {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 8px 0;
    line-height: 1.4;
    height: auto;
    min-height: 22px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .asset-description {
    font-size: 12px;
    color: #666;
    margin: 0 0 8px 0;
    line-height: 1.3;
  }

  .asset-details {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4px 8px;
    margin-bottom: 8px;

    .detail-item {
      display: flex;
      align-items: center;
      gap: 4px;

      .detail-text {
        font-size: 11px;
        color: #606266;
      }
    }
  }

  .asset-status {
    display: flex;
    align-items: center;
    margin-top: auto;
  }
}

// Mobile responsive
@media (max-width: 768px) {
  .asset-card-inner {
    margin-bottom: 6px;
    height: 140px;
  }

  .asset-image-container {
    width: 140px;
    height: 140px;

    .asset-icon {
      width: 70px;
      height: 70px;
    }
  }

  .asset-info {
    padding: 10px;

    .asset-title {
      font-size: 14px;
      line-height: 1.4;
      min-height: 20px;
      margin-bottom: 10px;
    }

    .asset-description {
      font-size: 11px;
    }

    .asset-details .detail-item .detail-text {
      font-size: 10px;
    }
  }
}
@media (min-width: 480px) {
  .asset-card-inner {
    height: 140px;
    margin-bottom: 4px;
  }

  .asset-image-container {
    width: 140px;
    height: 140px;

    .asset-icon {
      width: 60px;
      height: 60px;
    }
  }

  .asset-info {
    padding: 8px;

    .asset-details {
      grid-template-columns: 1fr 1fr;
      gap: 2px 4px;
    }
  }
}

// Extra small screen responsive
@media (max-width: 479px) {
  .asset-card-inner {
    height: 190px;
    margin-bottom: 4px;
  }

  .asset-title {
    font-size: 12px !important;
  }

  .asset-image-container {
    width: 100px;
    height: 190px;
    .asset-icon {
      width: 30px !important;
      height: 30px !important;
    }
  }

  .asset-info {
    padding-left: 4px;

    .asset-details {
      grid-template-columns: 1fr 1fr;
      gap: 4px 4px;
    }
  }
}
</style>
