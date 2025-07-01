import request from '@/utils/request'

export const assetsGetInfoService = (userId) =>
  request.get(`/user/aid/${userId}/asset`)

export const assetUpdateInfoService = (id, ownerId, location) => {
  const obj = {
    id: id,
    ownerId: ownerId,
    location: location
  }
  console.log(obj)
  return request.put('/admin/asset', obj)
}

export const assetInsertService = (id, ownerId, location) => {
  const obj = {
    id: id,
    ownerId: ownerId,
    location: location
  }
  return request.post('/asset', obj)
}
