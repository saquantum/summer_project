import request from '@/utils/request'

export const assetsGetInfoService = (userId) =>
  request.get(`/user/holder/${userId}/asset/`)
