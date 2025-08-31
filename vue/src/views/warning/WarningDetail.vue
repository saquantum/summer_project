<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useWarningStore } from '@/stores'
import { onMounted, computed, ref } from 'vue'
import type { Style } from '@/types'
const route = useRoute()
const warningStore = useWarningStore()

const mapId = 'map-' + route.params.id

const warning = computed(() =>
  warningStore.allWarnings.find((item) => item.id === Number(route.params.id))
)

const style = ref<Style>({
  weight: 1,
  fillOpacity: 0,
  color: '',
  fillColor: ''
})

const setWarningLevelStyle = (level: string): Style => {
  const style = { weight: 2, fillOpacity: 0.4, color: '', fillColor: '' }

  const l = level.toUpperCase()
  if (l === 'YELLOW') {
    style.color = '#cc9900'
    style.fillColor = '#ffff00'
  } else if (l === 'AMBER') {
    style.color = '#cc6600'
    style.fillColor = '#ffcc00'
  } else if (l === 'RED') {
    style.color = '#800000'
    style.fillColor = '#ff0000'
  }
  return style
}

const displayData = computed(() => {
  if (!warning.value) return []
  return [
    { label: 'Warning ID', value: warning.value.id },
    { label: 'Weather Type', value: warning.value.weatherType },
    { label: 'Warning Level', value: warning.value.warningLevel },
    { label: 'Warning HeadLine', value: warning.value.warningHeadLine },
    {
      label: 'Valid From',
      value: new Date(warning.value.validFrom * 1000).toLocaleString()
    },
    {
      label: 'Valid To',
      value: new Date(warning.value.validTo * 1000).toLocaleString()
    },
    { label: 'Warning Impact', value: warning.value.warningImpact },
    { label: 'Warning Likelihood', value: warning.value.warningLikelihood },
    {
      label: 'Affected Areas',
      value: warning.value.affectedAreas.replace(/\\n/g, '\n'),
      isMultiline: true
    },
    {
      label: 'Further Details',
      value: warning.value.warningFurtherDetails.replace(/\\n/g, '\n'),
      isMultiline: true
    },
    {
      label: 'Update Description',
      value: warning.value.warningUpdateDescription
    }
  ]
})

onMounted(async () => {
  // get warnings if not exist
  if (warningStore.allWarnings.length === 0) {
    warningStore.getAllWarnings()
  }

  if (warning.value) {
    style.value = setWarningLevelStyle(warning.value.warningLevel)
  }
})
</script>

<template>
  <div class="warning-detail-page">
    <el-row :gutter="16" class="content-grid no-wrap">
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="card-elevated map-card">
          <div class="map-container">
            <MapCard
              :map-id="mapId"
              :locations="[warning?.area]"
              :styles="[style]"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="card-elevated info-card">
          <div class="card-header header-with-meta">
            <h3>Warning Detail</h3>
            <div class="header-meta">
              <span class="pill">{{ warning?.weatherType }}</span>
              <span
                class="pill"
                :class="{
                  'pill-red': warning?.warningLevel?.includes('RED'),
                  'pill-amber': warning?.warningLevel?.includes('AMBER'),
                  'pill-yellow': warning?.warningLevel?.includes('YELLOW')
                }"
                >{{ warning?.warningLevel }}</span
              >
            </div>
          </div>
          <span class="range" v-if="warning">
            {{ new Date(warning.validFrom * 1000).toLocaleString() }} –
            {{ new Date(warning.validTo * 1000).toLocaleString() }}
          </span>
          <div class="info-body">
            <div class="kv-wrap">
              <dl class="kv-grid">
                <div
                  v-for="(item, index) in displayData"
                  :key="index"
                  class="kv-item"
                  :class="{ 'span-all': item.isMultiline }"
                  :data-label="item.label"
                >
                  <dt class="kv-label">{{ item.label }}</dt>
                  <dd
                    class="kv-value"
                    :class="{ 'multiline-text': item.isMultiline }"
                  >
                    {{ item.value }}
                  </dd>
                </div>
              </dl>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.warning-detail-page {
  --gap: 16px;
  --radius: 14px;
  --shadow: 0 6px 18px rgba(0, 0, 0, 0.06);
  --soft-border: #ebeef5;
  --title: #1f2d3d;
  padding: var(--gap);
  margin: 0 auto;
  --panel-h: clamp(360px, 60vh, 680px);
  --card-header-h: 56px;
  --card-body-py: 16px;
}

.content-grid {
  margin-top: var(--gap);
}

.card-elevated {
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  border: 1px solid var(--soft-border);
  background: #fff;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  min-height: 40px;
  font-weight: 600;
  color: var(--title);
}

.kv-item.span-all {
  grid-column: 1 / -1;
  padding-top: 4px;
}

.multiline-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.map-card {
  height: 700px;
  display: flex;
  flex-direction: column;
  margin-top: 20px;
}

.map-card :deep(.el-card__body) {
  padding: var(--card-body-py) 16px;
  flex: 1;
  display: flex;
  box-sizing: border-box;
}

.map-container {
  flex: 1;
  min-height: 0;
}

.info-card {
  height: 700px;
  display: flex;
  flex-direction: column;
  margin-top: 20px;
}
.info-body {
  height: var(--panel-h);
  overflow: auto;
  padding: var(--card-body-py) 8px 8px;
}

.header-with-meta {
  display: flex;
  align-items: left;
  justify-content: space-between;
  gap: 12px;
}
.header-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 0;
}
.pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #e6eaf2;
  background: #f7f9fc;
  color: #4a5568;
}
.pill-red {
  background: #fff5f5;
  border-color: #fed7d7;
  color: #c53030;
}
.pill-amber {
  background: #fffaf0;
  border-color: #feebc8;
  color: #b7791f;
}
.pill-yellow {
  background: #fffff0;
  border-color: #fefcbf;
  color: #975a16;
}
.range {
  font-size: 12px;
  color: #8c8c8c;
}

.kv-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px 24px;
}
.kv-item {
  display: flex;
  flex-direction: column;
  padding-bottom: 10px;
  border-bottom: 1px solid #eef2f7;
}

.kv-item[data-label='Warning HeadLine'],
.kv-item[data-label='Affected Areas'],
.kv-item[data-label='Further Details'],
.kv-item[data-label='Update Description'] {
  grid-column: 1 / -1;
}
.kv-label {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  color: #015697;
  margin-bottom: 6px;
}
.kv-value {
  font-size: 15px;
  line-height: 1.7;
  color: #303133;
  word-break: break-word;
  white-space: pre-wrap;
}
.kv-wrap dl,
.kv-wrap dt,
.kv-wrap dd {
  margin: 0;
}
.kv-value {
  margin-inline-start: 0;
}
.kv-grid > .kv-item:last-child {
  border-bottom: none;
}

@media (min-width: 1024px) {
  .warning-detail-page {
    width: 1380px;
  }
}
@media (max-width: 1024px) and (min-width: 768px) {
  .warning-detail-page {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .warning-detail-page {
    width: 100% !important;
    max-width: 100%;
    margin: 0;
    padding: 12px;
    height: auto !important;
    --panel-h-mobile: 340px;
  }
  .info-card {
    margin-top: 20px;
    width: 100%;
    height: auto !important;
  }

  .info-body {
    height: auto !important;
    overflow: visible !important;
    padding: 12px 8px !important;
  }
}
</style>
