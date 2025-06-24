import request from '@/utils/request'

export const adminGetUsersService = () => request.get('/admin/user/all')

export const adminGetAssetsService = () => request.get('/asset/')

export const adminGetWarningsService = () => request.get('/warning/all')
