import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { RegisterForm, User } from '@/types'
export const userRegisterService = (obj: RegisterForm): Promise<ApiResponse> =>
  request.post('/register', obj)

export const userLoginService = ({
  username,
  password
}: {
  username: string
  password: string
}): Promise<ApiResponse<User>> => {
  const id = username
  return request.post('/login', { id, password })
}

export const userGetInfoService = (id: string): Promise<ApiResponse<User>> => {
  return request.get(`/user/uid/${id}`)
}

export const userUpdateInfoService = (
  id: string,
  obj: object
): Promise<ApiResponse> => {
  return request.put(`/user/uid/${id}`, obj)
}

export const userCheckUIDService = (id: string): Promise<ApiResponse> =>
  request.get(`exists/uid/${id}`)

export const userCheckEmailService = (email: string): Promise<ApiResponse> =>
  request.get(`exists/email/${email}`)

export const userInsertAssetService = (
  assetHolderId: string,
  obj: object
): Promise<ApiResponse> => request.post(`/user/aid/${assetHolderId}/asset`, obj)

export const userGetEmailService = (email: string): Promise<ApiResponse> =>
  request.post('/email/code', { email })

export const userEmailVerificationService = ({
  email,
  code
}: {
  email: string
  code: string
}): Promise<ApiResponse> => request.post('/email/verification', { email, code })

export const userResetPasswordService = ({
  email,
  password
}: {
  email: string
  password: string
}): Promise<ApiResponse> => request.post('/email/password', { email, password })
