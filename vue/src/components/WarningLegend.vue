<script setup lang="ts">
import { ref, computed } from 'vue'

export interface LegendItem {
  value: string
  label: string
  color: string
  description: string
}

interface Props {
  items?: LegendItem[]
  size?: 'small' | 'medium' | 'large'
  orientation?: 'horizontal' | 'vertical'
}

const props = withDefaults(defineProps<Props>(), {
  items: () => [
    {
      value: 'NO',
      label: 'No Warning',
      color: 'rgba(0, 128, 0, 0.64)',
      description: 'No meteorological warning for this asset.'
    },
    {
      value: 'YELLOW',
      label: 'Yellow Warning',
      color: 'rgba(244,234,78,0.93)',
      description:
        'Yellow: Be aware. Severe weather is possible. Plan ahead and check for updates.'
    },
    {
      value: 'AMBER',
      label: 'Amber Warning',
      color: 'rgba(255,165,0,0.75)',
      description:
        'Amber: Be prepared. There is an increased likelihood of impacts from severe weather, which could potentially disrupt your plans.'
    },
    {
      value: 'RED',
      label: 'Red Warning',
      color: 'rgba(174, 9, 9, 0.79)',
      description:
        'Red: Take action. Dangerous weather is expected. Avoid dangerous areas and follow official advice.'
    }
  ],
  size: 'medium',
  orientation: 'horizontal'
})

const showDialog = ref(false)

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'small':
      return 'legend--small'
    case 'large':
      return 'legend--large'
    default:
      return 'legend--medium'
  }
})

const orientationClasses = computed(() => {
  return props.orientation === 'vertical'
    ? 'legend--vertical'
    : 'legend--horizontal'
})
</script>

<template>
  <svg
    data-test="warning-legend-btn"
    t="1754662950876"
    class="icon legend-icon"
    viewBox="0 0 1024 1024"
    version="1.1"
    xmlns="http://www.w3.org/2000/svg"
    p-id="6217"
    width="35"
    height="35"
    style="cursor: pointer"
    @click="showDialog = true"
  >
    <path
      d="M334.016 727.04a32 32 0 1 0 0-64 32 32 0 0 0 0 64z m0-183.04a32 32 0 1 0 0-64 32 32 0 0 0 0 64z m0-182.016a32 32 0 1 0 0-64 32 32 0 0 0 0 64z m478.976-279.04H211.008c-37.568 0.064-67.968 30.528-68.032 68.032v722.048c0.064 37.504 30.464 67.968 68.032 67.968h601.984c37.568 0 67.968-30.464 68.032-67.968V150.976c-0.064-37.504-30.464-67.968-68.032-67.968z m-3.968 786.048H214.976V155.008h594.048v713.984zM414.016 296h307.968c5.376 0 8 2.688 8 8v48c0 5.312-2.624 8-8 8H414.08c-5.376 0-8-2.688-8-8v-48c0-5.312 2.624-8 8-8z m0 184h307.968c5.376 0 8 2.688 8 8v48c0 5.312-2.624 8-8 8H414.08c-5.376 0-8-2.688-8-8v-48c0-5.312 2.624-8 8-8z m0 184h307.968c5.376 0 8 2.688 8 8v48c0 5.312-2.624 8-8 8H414.08c-5.376 0-8-2.688-8-8v-48c0-5.312 2.624-8 8-8z"
      fill="#000000"
      p-id="6218"
    ></path>
  </svg>
  <el-dialog
    v-model="showDialog"
    title="Warning Legend"
    width="300px"
    append-to-body
    top="15vh"
  >
    <div class="warning-legend" :class="[sizeClasses, orientationClasses]">
      <el-popover
        v-for="item in items"
        :key="item.value"
        placement="top"
        :width="250"
        trigger="hover"
        :show-after="300"
        :hide-after="100"
      >
        <template #reference>
          <div class="legend-item" :style="{ '--item-color': item.color }">
            <span class="legend-bar"></span>
            <span class="legend-label">{{ item.label }}</span>
          </div>
        </template>
        <template #default>
          <div class="popover-content">
            <h4 class="popover-title" :style="{ color: item.color }">
              {{ item.label }}
            </h4>
            <p class="popover-description">
              {{ item.description }}
            </p>
          </div>
        </template>
      </el-popover>
    </div>
  </el-dialog>
</template>

<style scoped>
.warning-legend {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.legend--horizontal {
  flex-direction: row;
  gap: 18px;
  justify-content: center;
  flex-wrap: wrap;
}

.legend--vertical {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
  border-radius: 6px;
  padding: 4px 8px;
  position: relative;
  cursor: default;
}

.legend-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  filter: brightness(1.1);
}

.legend-bar {
  width: 40px;
  height: 12px;
  border-radius: 4px;
  background-color: var(--item-color);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.legend-item:hover .legend-dot {
  transform: scale(1.2);
}

.legend-label {
  color: #292828;
}

.legend-item:hover .legend-label {
  font-weight: 600;
}

/* Size variants */
.legend--small {
  font-size: 13px;
}

.legend--small .legend--horizontal {
  gap: 12px;
}

.legend--small .legend--vertical {
  gap: 8px;
}

.legend--medium {
  font-size: 15px;
}

.legend--large {
  font-size: 17px;
}

.legend--large .legend--horizontal {
  gap: 24px;
}

.legend--large .legend--vertical {
  gap: 16px;
}

.legend-card-wrapper {
  position: fixed;
  top: 120px;
  right: 24px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 12px;
  padding: 12px 16px;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  width: max-content;
}

.legend-header {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 8px;
  text-align: center;
}

.legend-icon {
  cursor: pointer;
  background-color: transparent;
  padding: 6px;
  margin-left: 15px;
  transition: background-color 0.2s ease;
}

/* Responsive design */
@media (max-width: 768px) {
  .legend--horizontal {
    flex-wrap: wrap;
    gap: 12px;
  }

  .legend-item {
    padding: 6px 10px;
  }
}

@media (max-width: 480px) {
  .warning-legend {
    font-size: 13px;
  }

  .legend--horizontal {
    gap: 8px;
  }

  .legend-item {
    padding: 4px 6px;
  }
}

/* Popover styles */
.popover-content {
  padding: 8px 0;
}

.popover-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.popover-title::before {
  content: '●';
  font-size: 14px;
}

.popover-description {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: #666;
}
</style>
