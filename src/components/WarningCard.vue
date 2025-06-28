<script setup>
import { ref } from 'vue'
import {
  Warning,
  Location,
  Calendar,
  InfoFilled
} from '@element-plus/icons-vue'

// Warning data
const warningData = ref({
  id: 'ID1608',
  type: 'RAIN',
  level: 'RED',
  headline:
    'Prolonged heavy rain may lead to flooding and disruption in parts of Scotland.',
  validFrom: '2025/6/25 03:40:00',
  validTo: '2026/6/25 03:40:00',
  impact: '4/High',
  likelihood: '4/Very Likely',
  affectedAreas:
    'Scotland (Highlands, Eilean Siar, Grampian, Central, Tayside & Fife, Strathclyde, Lothian, Borders)',
  details:
    'Persistent heavy rain over higher ground is expected. 50-80 mm of rain may fall in some areas, possibly more locally.',
  updateDescription:
    'Initial issue of warning for Scotland region due to forecasted weather system.'
})

// Get styles based on warning level
const getLevelStyle = (level) => {
  const styles = {
    RED: {
      color: '#F56C6C',
      bgColor: '#FEF0F0',
      borderColor: '#F56C6C'
    },
    AMBER: {
      color: '#E6A23C',
      bgColor: '#FAECD8',
      borderColor: '#E6A23C'
    },
    YELLOW: {
      color: '#F7BA2A',
      bgColor: '#FBEAA0',
      borderColor: '#F7BA2A'
    }
  }
  return styles[level] || styles.RED
}

// Format date string
const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('en-GB')
}

const levelStyle = getLevelStyle(warningData.value.level)
</script>

<template>
  <div class="warning-container">
    <el-card
      class="warning-card"
      :style="{
        borderLeft: `4px solid ${levelStyle.borderColor}`,
        backgroundColor: levelStyle.bgColor
      }"
      shadow="hover"
    >
      <!-- Card header -->
      <template #header>
        <div class="warning-header">
          <div class="warning-title">
            <el-icon :style="{ color: levelStyle.color }" size="20">
              <Warning />
            </el-icon>
            <span class="warning-type">{{ warningData.type }} WARNING</span>
            <el-tag
              :color="levelStyle.color"
              effect="dark"
              size="small"
              class="level-tag"
            >
              {{ warningData.level }}
            </el-tag>
          </div>
          <div class="warning-id">ID: {{ warningData.id }}</div>
        </div>
      </template>

      <!-- Card content -->
      <div class="warning-content">
        <!-- Main description -->
        <div class="headline">
          <el-icon :style="{ color: levelStyle.color }">
            <InfoFilled />
          </el-icon>
          <p>{{ warningData.headline }}</p>
        </div>

        <!-- Detailed Information -->
        <el-collapse class="details-collapse" v-if="false">
          <el-collapse-item title="Additional Details" name="details">
            <div class="details-content">
              <div class="detail-row">
                <span class="detail-label">Impact Level:</span>
                <el-tag type="danger" size="small">{{
                  warningData.impact
                }}</el-tag>
              </div>
              <div class="detail-row">
                <span class="detail-label">Likelihood:</span>
                <el-tag type="warning" size="small">{{
                  warningData.likelihood
                }}</el-tag>
              </div>
              <div class="detail-section">
                <span class="detail-label">Detailed Description:</span>
                <p>{{ warningData.details }}</p>
              </div>
              <div class="detail-section">
                <span class="detail-label">Update Notes:</span>
                <p>{{ warningData.updateDescription }}</p>
              </div>
            </div>
            <!-- Valid Period -->
            <div class="time-info">
              <div class="time-item">
                <el-icon><Calendar /></el-icon>
                <div>
                  <span class="time-label">Valid Period:</span>
                  <div class="time-range">
                    <span>From: {{ formatDate(warningData.validFrom) }}</span>
                    <span>To: {{ formatDate(warningData.validTo) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Affected Areas -->
            <div class="affected-areas">
              <el-icon><Location /></el-icon>
              <div>
                <span class="area-label">Affected Areas:</span>
                <p class="area-text">{{ warningData.affectedAreas }}</p>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.warning-container {
  max-width: 600px;
}

.warning-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.warning-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.warning-type {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.level-tag {
  font-size: 12px;
  font-weight: bold;
}

.warning-id {
  font-size: 14px;
  color: #909399;
  font-family: monospace;
}

.warning-content {
  space-y: 16px;
}

.headline {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 20px;
}

.headline p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.5;
}

.time-info {
  margin-bottom: 20px;
}

.time-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.time-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
  display: block;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-range span {
  font-size: 14px;
  color: #303133;
}

.affected-areas {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 20px;
}

.area-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
  display: block;
}

.area-text {
  margin: 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
}

.details-collapse {
  border: none;
}

.details-content {
  padding: 0;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-label {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
  display: block;
}

.detail-section p {
  margin: 0;
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
}

/* Responsive design */
@media (max-width: 768px) {
  .warning-container {
    padding: 0 12px;
  }

  .warning-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .time-range {
    gap: 2px;
  }
}
</style>
