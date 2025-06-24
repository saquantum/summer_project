import request from '@/utils/request'

export const userRegisterService = ({ username, password, repassword }) =>
  request.post('/reg', { username, password, repassword })

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
