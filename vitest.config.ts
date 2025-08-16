import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config'
import { config } from '@vue/test-utils'
config.global.config.warnHandler = () => {}

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      exclude: [...configDefaults.exclude, 'e2e/**'],
      root: fileURLToPath(new URL('./', import.meta.url)),
      server: {
        deps: {
          inline: ['element-plus']
        }
      },
      coverage: {
        provider: 'v8',
        reporter: ['text', 'html'],
        include: ['src/**/*.{ts,tsx,vue}'],
        exclude: [
          'src/main.ts',
          'src/App.vue',
          'src/types/**',
          'src/api/**',
          'src/router/**',
          'src/api/[...all].vue'
        ]
      }
    }
  })
)
