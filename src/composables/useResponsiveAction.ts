import { ref, onMounted, onUnmounted } from 'vue'

export function useResponsiveAction(action: (width: number) => void) {
  const screenWidth = ref(window.innerWidth)
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  const handleResize = () => {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      screenWidth.value = window.innerWidth
      action(screenWidth.value)
    }, 200)
  }

  onMounted(() => {
    window.addEventListener('resize', handleResize)
    action(screenWidth.value)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    if (debounceTimer) clearTimeout(debounceTimer)
  })

  return { screenWidth }
}
