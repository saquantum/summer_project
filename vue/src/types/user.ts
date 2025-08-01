import type { Permission } from './permission'

export interface ContactPreferences {
  whatsapp: boolean
  discord: boolean
  post: boolean
  phone: boolean
  assetHolderId?: string
  telegram: boolean
  email: boolean
}

export interface Address {
  country: string
  city: string
  street: string
  postcode: string
  assetHolderId?: string
}

export interface AssetHolder {
  id?: string
  name: string
  email: string
  phone: string
  addressId?: string
  address: Address
  contactPreferencesId?: string
  lastModified?: number
  contact_preferences: ContactPreferences
}

export interface User {
  id: string
  password: string | null
  assetHolderId?: string | null
  assetHolder?: AssetHolder | null
  admin: boolean
  token: string | null
  permissionConfig: Permission
  avatar: string
}

export interface LoginForm {
  username: string
  password: string
  email: string
}

export interface UserInfoForm {
  id: string
  password: string | null
  repassword?: string
  firstName?: string
  lastName?: string
  avatar?: string
  assetHolderId?: string | null
  assetHolder: AssetHolder
}

export interface UserItem {
  user: User
  accumulation: number
}

export interface RegisterForm {
  id: string
  firstName: string
  lastName: string
  name: string
  email: string
  phone: string
  password: string
  repassword: string
}

export interface UserSearchForm {
  id: string
}

export interface UserSearchBody {
  filters: UserFilter
  orderList: string
  limit: number
  offset: number
}

export interface UserFilter {
  user_id?: {
    op: string
    val: string
  }
}
