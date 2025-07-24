/**
 * Environment-specific upload configuration
 * This file allows different configurations for different environments
 */

import { uploadConfig, type UploadConfig } from './upload'

// Development environment overrides
const developmentConfig: Partial<UploadConfig> = {
  fileSizeLimit: {
    avatar: 2, // Smaller limits for development
    document: 5,
    image: 3
  }
}

// Production environment overrides
const productionConfig: Partial<UploadConfig> = {
  fileSizeLimit: {
    avatar: 10, // Larger limits for production
    document: 20,
    image: 15
  }
}

// Test environment overrides
const testConfig: Partial<UploadConfig> = {
  fileSizeLimit: {
    avatar: 1, // Smallest limits for testing
    document: 2,
    image: 1
  }
}

/**
 * Get configuration based on current environment
 * @returns Complete upload configuration for current environment
 */
export const getEnvironmentConfig = (): UploadConfig => {
  const env = import.meta.env.MODE

  let envConfig: Partial<UploadConfig> = {}

  switch (env) {
    case 'development':
      envConfig = developmentConfig
      break
    case 'production':
      envConfig = productionConfig
      break
    case 'test':
      envConfig = testConfig
      break
    default:
      envConfig = {}
  }

  // Merge default config with environment-specific config
  return {
    ...uploadConfig,
    ...envConfig,
    auth: {
      ...uploadConfig.auth,
      ...envConfig.auth
    },
    fileSizeLimit: {
      ...uploadConfig.fileSizeLimit,
      ...envConfig.fileSizeLimit
    },
    allowedTypes: {
      ...uploadConfig.allowedTypes,
      ...envConfig.allowedTypes
    },
    endpoints: {
      ...uploadConfig.endpoints,
      ...envConfig.endpoints
    }
  }
}
