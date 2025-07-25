// Mock all CSS imports to prevent Node.js from trying to parse them
import { vi } from 'vitest'

// Mock CSS file imports
Object.defineProperty(window, 'CSS', {
  value: {
    supports: vi.fn(() => false)
  }
})

// Mock CSS imports at module level
vi.mock('*.css', () => ({}))
vi.mock('*.scss', () => ({}))
vi.mock('*.sass', () => ({}))
vi.mock('*.less', () => ({}))

// Specifically mock Element Plus CSS
vi.mock('element-plus/theme-chalk/base.css', () => ({}))
vi.mock('element-plus/theme-chalk/index.css', () => ({}))
vi.mock('element-plus/dist/index.css', () => ({}))
