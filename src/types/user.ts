export interface ContactPreferences {
  whatsapp: boolean
  discord: boolean
  post: boolean
  phone: boolean
  assetHolderId: string
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
  contactPreferencesId: string
  lastModified: number
  contact_preferences: ContactPreferences
}

export interface User {
  id: string
  password: string | null
  assetHolderId: string | null
  assetHolder: AssetHolder | null
  admin: boolean
  token: string | null
}

export interface LoginForm {
  username: string
  password: string
  email: string
}

export interface UserInfoForm {
  id: string
  password?: string
  repassword?: string
  firstName?: string
  lastName?: string
  assetHolder: {
    name: string
    email: string
    phone: string
    address: {
      street: string
      postcode: string
      city: string
      country: string
    }
    contact_preferences: {
      email: boolean
      phone: boolean
      whatsapp: boolean
      discord: boolean
      post: boolean
      telegram: boolean
    }
  }
}

export interface UserItem {
  user: User
  count: number
}
