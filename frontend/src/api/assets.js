import request from '@/utils/request'

export const assetsGetInfoService = (userId) =>
  request.get(`/user/holder/${userId}/asset/`)

export const assetUpdateInfoService = (id, drainArea) => {
  return request.put('/user/asset', {
    id: id,
    drainArea: drainArea
  })
}
