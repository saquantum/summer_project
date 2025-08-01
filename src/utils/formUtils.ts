import { userCheckEmailService, userCheckUIDService } from '@/api/user'
import { adminGetUserInfoService } from '@/api/admin'
import type { InternalRuleItem } from 'async-validator'
import CodeUtil from './codeUtil'
import type { FormItemRule } from 'element-plus'
import type {
  AssetFilter,
  AssetSearchForm,
  UserFilter,
  UserSearchForm
} from '@/types'

export const firstNameRules = [
  { required: true, message: 'First name is required', trigger: 'blur' },
  {
    min: 2,
    max: 30,
    message: 'First name must be 2–30 characters',
    trigger: 'blur'
  }
]

export const lastNameRules = [
  { required: true, message: 'Last name is required', trigger: 'blur' },
  {
    min: 2,
    max: 30,
    message: 'Last name must be 2–30 characters',
    trigger: 'blur'
  }
]

export const usernameRules = [
  { required: true, message: 'Please input username', trigger: 'blur' },
  {
    pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]+$/,
    message:
      'Username can only contain letters, numbers, and special characters (no spaces allowed).',
    trigger: 'blur'
  },
  {
    asyncValidator: async (
      rule: InternalRuleItem,
      value: string,
      callback: (_error?: string | Error) => void
    ) => {
      try {
        const res = await userCheckUIDService(value)
        if (CodeUtil.isSuccess(res.code)) {
          callback(new Error(`Username '${value}' already exists`))
        } else {
          callback()
        }
      } catch {
        callback('Server error')
      }
    },
    trigger: 'blur'
  }
]

export const passwordRules = [
  {
    required: true,
    message: 'Please input password',
    trigger: 'blur'
  },
  {
    pattern: /^[a-zA-Z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]+$/,
    message:
      'Password can only contain letters, numbers, and special characters (no spaces allowed).',
    trigger: 'blur'
  }
]

export const createRepasswordRules = (getPassword: () => string) => [
  { required: true, message: 'Please input password', trigger: 'blur' },
  {
    validator: (
      rule: InternalRuleItem,
      value: string,
      callback: (_error?: Error) => void
    ) => {
      if (value !== getPassword()) {
        callback(new Error("Those passwords didn't match. Try again."))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

export const emailRules: FormItemRule[] = [
  { required: true, message: 'Email is required', trigger: 'blur' },
  { type: 'email', message: 'Invalid email format', trigger: 'blur' }
]

export const uniqueEmailRules: FormItemRule[] = [
  { required: true, message: 'Email is required', trigger: 'blur' },
  { type: 'email', message: 'Invalid email format', trigger: 'blur' },
  {
    asyncValidator: async (
      rule: InternalRuleItem,
      value: string,
      callback: (_error?: Error) => void
    ) => {
      const res = await userCheckEmailService(value)
      if (CodeUtil.isSuccess(res.code)) {
        callback(new Error('This email has already been used'))
      }
      callback()
    },
    trigger: 'blur'
  }
]

export const phoneRules = [
  { required: true, message: 'Phone is required', trigger: 'blur' },
  {
    validator: (
      rule: InternalRuleItem,
      value: string,
      callback: (_error?: Error) => void
    ) => {
      const phoneRegex = /^[0-9+\-()\s]{7,20}$/
      if (!phoneRegex.test(value)) {
        callback(new Error('Invalid phone number'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
]

export const postcodeRules = [
  {
    pattern: /^[a-zA-Z0-9\-\s]{3,16}$/,
    message: 'Post code must be 3-16 letters, numbers, spaces or hyphens.',
    trigger: 'blur'
  }
]

export const codeRules = [
  {
    required: true,
    message: 'Please input code',
    trigger: 'blur'
  },
  {
    min: 6,
    max: 6,
    message: 'Code must be exactly 6 characters',
    trigger: 'blur'
  },
  {
    pattern: /^[A-Za-z0-9]{6}$/,
    message: 'Code must contain only letters and numbers',
    trigger: 'blur'
  }
]

export const createUserRules = (admin: boolean) => [
  { required: true, message: 'Please input username', trigger: 'blur' },
  {
    asyncValidator: async (
      rule: FormItemRule,
      value: string,
      callback: (_error?: Error) => void
    ) => {
      try {
        const res = await userCheckUIDService(value)
        if (CodeUtil.isSuccess(res.code)) {
          if (admin) {
            const adminRes = await adminGetUserInfoService(value)
            if (adminRes.data.admin) {
              callback(new Error('Can not add asset to admin'))
              return
            }
          }
          callback()
        } else {
          callback(new Error(`Username ${value} does not exist.`))
        }
      } catch {
        callback(new Error('Server error occurred'))
      }
    },
    trigger: 'blur'
  }
]

export const trimForm = (obj: object) => {
  Object.keys(obj).forEach((key) => {
    const value = (obj as Record<string, unknown>)[key]
    if (typeof value === 'string') {
      ;(obj as Record<string, unknown>)[key] = value.trim()
    } else if (typeof value === 'object' && value !== null) {
      trimForm(value)
    }
  })
}

export const assetConverFormToFilter = (form: AssetSearchForm): AssetFilter => {
  const body: AssetFilter = {}

  if (form.id) {
    body.asset_id = { op: 'like', val: `%${form.id}%` }
  }

  if (form.name) {
    body.asset_name = { op: 'like', val: `%${form.name}%` }
  }

  if (form.ownerId) {
    body.asset_owner_id = { op: 'like', val: `%${form.ownerId}%` }
  }

  if (form.typeId) {
    body.asset_type_id = { op: 'like', val: `%${form.typeId}%` }
  }

  if (form.capacityLitres?.length === 2) {
    const [min, max] = form.capacityLitres
    body.asset_capacity_litres = {
      op: 'range',
      min,
      max
    }
  }

  if (form.material) {
    body.asset_material = { op: 'like', val: `%${form.material}%` }
  }

  if (form.status) {
    body.asset_status = { op: 'like', val: `%${form.status}%` }
  }

  if (form.installedAt?.length === 2) {
    const [start, end] = form.installedAt
    body.asset_installed_at = {
      op: 'range',
      min: start.toISOString().slice(0, 10),
      max: end.toISOString().slice(0, 10)
    }
  }

  if (form.lastInspection?.length === 2) {
    const [start, end] = form.lastInspection
    body.asset_last_inspection = {
      op: 'range',
      min: start.toISOString().slice(0, 10),
      max: end.toISOString().slice(0, 10)
    }
  }

  if (form.warningLevel && form.warningLevel !== 'NO') {
    body.warning_level = { op: 'like', val: `%${form.warningLevel}%` }
  } else if (form.warningLevel && form.warningLevel === 'NO') {
    body.warning_level = { op: 'isNull', val: '' }
  }

  console.log(body)
  return body
}

export const userConverFormToFilter = (form: UserSearchForm): UserFilter => {
  const body: UserFilter = {}

  if (form.id) {
    body.user_id = { op: 'like', val: `%${form.id}%` }
  }

  return body
}
