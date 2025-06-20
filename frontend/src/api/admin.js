import request from '@/utils/request'

export const adminGetUsersService = () => request.get('/user/holder/')

export const adminGetAssetsService = () => request.get('/user/asset/')

export const adminGetWarningsService = () => request.get('/user/warning/')

// useless api
export const adminProxyUserService = (id) => request.get(`/admin/as/${id}`)
