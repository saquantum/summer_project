import request from '@/utils/request'

export const userRegisterService = (obj) => request.post('/register', obj)

export const userLoginService = ({ username, password }) => {
  const id = username
  return request.post('/login', { id, password })
}

export const userGetInfoService = (id) => {
  return request.get(`/user/uid/${id}`)
}

export const userUpdateService = (id) => {
  return request.get(`/user/uid/${id}`)
}

export const userCheckUIDService = (id) => request.get(`exists/uid/${id}`)

export const userCheckEmailService = (email) =>
  request.get(`exists/email/${email}`)
