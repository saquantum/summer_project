import request from '@/utils/request'

export const adminGetUsersService = () =>
  request.get('/admin/user/accumulate/count/{column}')

export const adminGetAssetsService = () => request.get('/asset/')

export const adminGetWarningsService = () => request.get('/warning/all')
