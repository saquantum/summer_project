import request from '@/utils/request'

export const assetsGetInfoService = (userId) =>
  request.get(`/asset/holder/${userId}`)

export const assetUpdateInfoService = (id, drainArea) => {
  return request.put('/asset', {
    id: id,
    drainArea: drainArea
  })
}
