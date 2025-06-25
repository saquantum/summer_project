import request from '@/utils/request'

export const assetsGetInfoService = (userId) =>
  request.get(`/asset/holder/${userId}`)

export const assetUpdateInfoService = (id, ownerId, location) => {
  const obj = {
    id: id,
    ownerId: ownerId,
    location: location
  }
  return request.put('/asset', obj)
}

export const assetInsertService = (id, ownerId, location) => {
  const obj = {
    id: id,
    ownerId: ownerId,
    location: location
  }
  return request.post('/asset', obj)
}
