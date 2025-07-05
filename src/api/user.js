import request from '@/utils/request'

export const userRegisterService = (obj) => request.post('/register', obj)

export const userLoginService = ({ username, password }) => {
  const id = username
  return request.post('/login', { id, password })
}

export const userGetInfoService = (id) => {
  return request.get(`/user/uid/${id}`)
}

export const userUpdateInfoService = (id, obj) => {
  return request.put(`/user/uid/${id}`, obj)
}

export const userCheckUIDService = (id) => request.get(`exists/uid/${id}`)

export const userCheckEmailService = (email) =>
  request.get(`exists/email/${email}`)

export const userInsertAssetService = (assetHolderId, obj) =>
  request.post(`/user/aid/${assetHolderId}/asset`, obj)

export const userGetEmailService = (email) =>
  request.post('/email/code', { email })

export const userEmailVerificationService = ({ email, code }) =>
  request.post('/email/verification', { email, code })

export const userResetPasswordService = ({ email, password }) =>
  request.post('/email/password', { email, password })
