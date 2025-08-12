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

export interface ContactDetails {
  discord?: string
  email: string
  phone: string
  post?: string
  telegram?: string
  whatsapp?: string
}

export interface User {
  id: string
  password: string | null

  admin: boolean
  adminLevel: number
  avatar: string
  name: string
  permissionConfig: Permission

  address: Address

  contactDetails: ContactDetails

  contactPreferences: ContactPreferences
}

export interface LoginForm {
  username: string
  password: string
  email: string
}

export interface UserInfoForm {
  id: string
  password?: string | null
  repassword?: string
  admin?: boolean
  adminLevel?: number
  avatar?: string
  name?: string
  firstName?: string
  lastName?: string
  permissionConfig?: Permission

  address: Address

  contactDetails: ContactDetails

  contactPreferences: ContactPreferences
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
  name: string
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
  user_name?: {
    op: string
    val: string
  }
}
