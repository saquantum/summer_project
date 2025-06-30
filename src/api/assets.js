import request from '@/utils/request'

export const assetsGetInfoService = (userId) =>
  request.get(`/user/uid/${userId}/asset`)

export const assetUpdateInfoService = (id, ownerId, location) => {
  const obj = {
    id: id,
    ownerId: ownerId,
    location: location
  }
  console.log(obj)
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
