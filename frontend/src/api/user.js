import request from '@/utils/request'

export const userRegisterService = ({ username, password, repassword }) =>
  request.post('/reg', { username, password, repassword })

export const userLoginService = ({ username, password }) =>
  request.post('/login', { username, password })

export const userGetInfoService = (id) => {
  return request.get(`/user/${id}`)
}

export const userGetIdService = () => request.get('/user/me')
