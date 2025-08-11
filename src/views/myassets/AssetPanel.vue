<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ref, watch, computed } from 'vue'
import { useUserStore, useAssetStore } from '@/stores/index.ts'
import type { AssetWithWarnings } from '@/types'
import { useResponsiveAction } from '@/composables/useResponsiveAction'
import WarningLegend from '@/components/WarningLegend.vue'
const assetStore = useAssetStore()
const userStore = useUserStore()
const router = useRouter()
import { Position } from '@element-plus/icons-vue'

// filter value

const filters = ref({
  assetName: '',
  warningLevel: '',
  assetType: ''
})

// filtered assets
const currentAssets = ref<AssetWithWarnings[] | []>([])

// page change
const currentPage = ref(1)
const pageSize = 8
const handlePageChange = (page: number) => {
  currentPage.value = page
}

const currentPageAssets = computed(() => {
  if (!currentAssets.value || currentAssets.value.length === 0) return
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  const arr = currentAssets.value.slice(start, end)

  return arr
})

// watch filter condition
const warningOrder: Record<string, number> = {
  RED: 3,
  AMBER: 2,
  YELLOW: 1,
  '': 0
}

/**
 * mobile component
 */

const isMobile = ref(false)

const searchDetailVisible = ref(false)

const clearFilters = () => {
  searchDetailVisible.value = false
  filters.value.assetName = ''
  filters.value.assetType = ''
  filters.value.warningLevel = ''
}

onMounted(async () => {
  currentAssets.value = []
  let id
  if (!userStore.user?.admin) {
    id = userStore.user?.id
  } else {
    id = userStore.proxyId
  }
  if (userStore.user && id) {
    await assetStore.getUserAssets(userStore.user.admin, id)
  }

  if (assetStore.userAssets) {
    currentAssets.value = assetStore.userAssets.sort((a, b) => {
      const aLevel = a.maxWarning?.warningLevel || ''
      const bLevel = b.maxWarning?.warningLevel || ''
      return warningOrder[bLevel] - warningOrder[aLevel]
    })
  }
})

watch(
  [filters],
  async () => {
    if (!assetStore.userAssets || assetStore.userAssets.length === 0) return
    currentAssets.value = assetStore.userAssets
      .filter((item) => {
        let matchLevel = false
        if (
          filters.value.warningLevel &&
          item.maxWarning &&
          item.maxWarning.warningLevel === filters.value.warningLevel
        ) {
          matchLevel = true
        } else if (
          filters.value.warningLevel &&
          filters.value.warningLevel.includes('NO') &&
          !item.maxWarning
        ) {
          matchLevel = true
        } else if (!filters.value.warningLevel) {
          matchLevel = true
        }
        const matchName = filters.value.assetName
          ? item.asset.name
              ?.toLowerCase()
              .includes(filters.value.assetName.toLowerCase())
          : true
        const matchType = filters.value.assetType
          ? item.asset.typeId === filters.value.assetType
          : true
        return matchName && matchLevel && matchType
      })
      .sort((a, b) => {
        const aLevel = a.maxWarning?.warningLevel || ''
        const bLevel = b.maxWarning?.warningLevel || ''
        return warningOrder[bLevel] - warningOrder[aLevel]
      })
    currentPage.value = 1
  },
  {
    deep: true,
    immediate: true
  }
)

const handleSizeChange = () => {}

useResponsiveAction((width) => {
  if (width < 576) {
    isMobile.value = true
    console.log('Extra small screen, e.g., portrait phone')
    handleSizeChange()
  } else if (width >= 576 && width < 768) {
    isMobile.value = false
    handleSizeChange()
    console.log('Small screen, e.g., landscape phone or small tablet')
  } else if (width >= 768 && width < 992) {
    isMobile.value = false
    handleSizeChange()
    console.log('Medium screen, e.g., tablets or small laptops')
  } else {
    isMobile.value = false
    handleSizeChange()
    console.log('Large screen, e.g., desktops or larger')
  }
})

defineExpose({
  filters,
  currentAssets,
  currentPageAssets
})
</script>

<template>
  <!-- assets filter -->
  <div class="search-bar">
    <TestSearch
      v-model:visible="searchDetailVisible"
      v-model:filters="filters"
      @clearFilters="clearFilters"
    >
    </TestSearch>
    <!-- warning legend -->
    <WarningLegend v-if="!isMobile" :orientation="'vertical'" :size="'small'" />
  </div>

  <!-- cards for assets -->
  <div class="assets-container" v-if="!isMobile">
    <h3 v-if="assetStore.userAssets?.length === 0">You don't have any asset</h3>
    <div class="card-grid">
      <el-card
        v-for="(item, index) in currentPageAssets"
        :key="item.asset.id"
        class="asset-card"
        shadow="hover"
      >
        <div class="card-body">
          <div class="map-container">
            <MapCard
              :map-id="'map-' + index"
              :locations="[item.asset.location]"
              :display="true"
            />
          </div>
        </div>
        <template #footer>
          <div class="card-footer-row">
            <div class="footer-top-row">
              <div
                class="asset-title"
                :class="{
                  'warning-low': !item.maxWarning,
                  'warning-medium': item.maxWarning?.warningLevel === 'YELLOW',
                  'warning-high': item.maxWarning?.warningLevel === 'AMBER',
                  'warning-severe': item.maxWarning?.warningLevel === 'RED'
                }"
              >
                {{ item.asset.name || 'Asset Name' }}
              </div>

              <div class="warning-container">
                <div
                  class="warning-bar"
                  :class="`bar-${item.maxWarning?.warningLevel || 'SUCCESS'}`"
                ></div>
                <div class="warning-text">
                  {{
                    item.maxWarning?.warningLevel === 'RED'
                      ? 'High Risk'
                      : item.maxWarning?.warningLevel === 'AMBER'
                        ? 'Medium Risk'
                        : item.maxWarning?.warningLevel === 'YELLOW'
                          ? 'Low Risk'
                          : 'No Risk'
                  }}
                </div>
              </div>
            </div>
            <el-divider style="margin: 10px 0" />
            <div class="asset-info-group">
              <div class="asset-info-item">
                <div class="info-value">{{ item.asset.id }}</div>
                <div class="info-label">Asset ID</div>
              </div>
              <div class="asset-info-item">
                <div class="info-value">{{ item.asset.capacityLitres }}L</div>
                <div class="info-label">Capacity</div>
              </div>
              <div class="asset-info-item">
                <div class="info-value">{{ item.asset.installedAt }}</div>
                <div class="info-label">Installed</div>
              </div>
            </div>
          </div>
          <el-button
            type="primary"
            @click="router.push(`/assets/${item.asset.id}`)"
            class="view-details-btn"
          >
            Detail
            <span class="btn-icon">
              <el-icon class="filled-icon"><Position /></el-icon>
            </span>
          </el-button>
        </template>
      </el-card>
    </div>
  </div>

  <div class="asset-list" v-else>
    <AssetCard
      v-for="item in currentPageAssets"
      :key="item.asset.id"
      :item="item"
    />
  </div>

  <el-pagination
    :current-page="currentPage"
    :page-size="pageSize"
    :total="currentAssets.length"
    layout="prev, pager, next"
    @current-change="handlePageChange"
    style="
      display: flex;
      justify-content: center;
      text-align: center;
      margin-top: 20px;
      position: relative;
      z-index: 2000;
    "
  />
</template>

<style scoped>
:deep(.el-card__footer) {
  border-top: none !important;
}
.search-bar {
  position: sticky;
  top: 0;
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
  padding: 10px 0;
  z-index: 1100;
}
.assets-container {
  padding: 16px;
  min-height: 100vh;
}

.asset-list {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

.card-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  max-width: calc(300px * 4 + 40px * 3);
  margin-left: auto;
  margin-right: auto;
  justify-content: flex-start;
}

.asset-card {
  display: flex;
  width: 300px;
  border-radius: 30px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  flex-direction: column;
  justify-content: space-between;
}

.asset-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}
.card-header {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: start;
  column-gap: 8px;
}
.asset-title {
  /* hide overflow text*/
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: -5px;
  max-width: calc(100% - 32px);
}

.map-container {
  padding: 16px 0;
  display: flex;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border-radius: 25px;
  height: 200px;
}

.map {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
}

.card-footer {
  display: flex;
  justify-content: center;
  padding: 0;
}
.card-footer-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 4px;
  height: 55px;
  margin-top: 10px;
}
.footer-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 4px;
}

.view-details-btn {
  display: block;
  margin: 24px auto 10px auto;
  padding: 0 35px;
  width: 120px;
  height: 35px;

  background-image: linear-gradient(
    to top,
    rgba(163, 205, 168, 0.76) 0%,
    #a4d5a1 100%
  );
  border: 1px solid #fdfdfd;
  border-radius: 999px;

  font-weight: 600;
  font-size: 16px;
  color: rgb(255, 255, 255);

  text-align: center;

  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.view-details-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  background: linear-gradient(to bottom, #d4eacb, #f4f4f4);
  color: #6b9f65;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.25);
}

.btn-icon {
  background: linear-gradient(to bottom, #ffffff, #eaeaea);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  margin-left: 8px;

  display: flex;
  align-items: center;
  justify-content: center;

  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.filled-icon svg {
  width: 18px;
  height: 18px;
  color: #8eae89;
  stroke: none !important;
}

.asset-info-group {
  display: flex;
  justify-content: space-between;
  margin-top: -15px;
  padding: 0 8px;
  gap: 45px;
  margin-bottom: 10px;
}

.info-value {
  margin-top: 5px;
  font-size: 14px;
  font-weight: 560;
  color: #222;
  white-space: nowrap;
}
.info-label {
  font-size: 12px;
  color: #777;
}

.select-style {
  width: 240px;
}

.warning-bar {
  width: 45px;
  height: 16px;
  border-radius: 4px;
  margin-left: 8px;
  margin-top: 12px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
.warning-text {
  font-size: 12px;
  font-weight: 500;
  color: #555;
  margin-top: 2px;
  text-align: center;
  margin-left: 5px;
}

.bar-RED {
  background-color: rgba(174, 9, 9, 0.79);
}

.bar-AMBER {
  background-color: rgba(255, 165, 0, 0.75);
}

.bar-YELLOW {
  background-color: rgba(244, 234, 78, 0.93);
}

.bar-SUCCESS {
  background-color: rgba(0, 128, 0, 0.64);
}

@media (max-width: 768px) {
  .assets-container {
    padding: 12px;
  }

  .card-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;

    justify-content: center !important;
  }
}

@media (max-width: 480px) {
  .card-grid {
    justify-content: flex-start;
  }
  .select-style {
    width: 100px;
  }
}

/* change defalut style */
:deep(.el-card__header) {
  padding: 12px 16px;
  height: 45px;
}

:deep(.el-card__body) {
  padding: 0px 16px 0px 16px;
}

:deep(.el-card__footer) {
  padding: 10px 16px;
  border-top: 1px solid #ebeef5;
}
</style>
