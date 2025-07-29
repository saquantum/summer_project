<script setup lang="ts">
import { computed } from 'vue'

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
      color: 'green',
      description: 'No meteorological warning for this asset.'
    },
    {
      value: 'YELLOW',
      label: 'Yellow Warning',
      color: '#ffe923',
      description:
        'Yellow: Be aware. Severe weather is possible. Plan ahead and check for updates.'
    },
    {
      value: 'AMBER',
      label: 'Amber Warning',
      color: '#f90',
      description:
        'Amber: Be prepared. There is an increased likelihood of impacts from severe weather, which could potentially disrupt your plans.'
    },
    {
      value: 'RED',
      label: 'Red Warning',
      color: 'red',
      description:
        'Red: Take action. Dangerous weather is expected. Avoid dangerous areas and follow official advice.'
    }
  ],
  size: 'medium',
  orientation: 'horizontal'
})

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
          <span class="legend-dot">●</span>
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
}

.legend--vertical {
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--item-color);
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

.legend-dot {
  font-size: inherit;
  line-height: 1;
  transition: transform 0.3s ease;
}

.legend-item:hover .legend-dot {
  transform: scale(1.2);
}

.legend-label {
  white-space: nowrap;
  transition: all 0.3s ease;
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
