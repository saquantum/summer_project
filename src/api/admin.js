/**
 * only admin can use the api below
 */

import request from '@/utils/request'

export const adminGetUsersService = (functionName, offset, limit, orderList) =>
  request.get('/admin/user/accumulate', {
    params: {
      function: functionName,
      column: '1',
      offset: offset,
      limit: limit,
      orderList: orderList
    }
  })

export const adminGetUserInfoService = (id) => {
  return request.get(`/admin/user/aid/${id}`)
}

export const adminGetUserInfoByUIDService = (id) => {
  return request.get(`/admin/user/uid/${id}`)
}

export const adminGetAssetsService = (offset, limit, orderList) => {
  console.log(offset, limit, orderList)
  return request.get('/admin/asset/', {
    params: { offset: offset, limit: limit, orderList: orderList }
  })
}

export const adminGetUserAssetsService = (id) => {
  return request.get(`/admin/user/aid/${id}/asset`)
}

export const adminGetAllWarningsService = () =>
  request.get('/admin/warning/all')

export const adminGetAllLiveWarningsService = () => request.get('/warning')

export const adminGetNotificationService = () => {
  return
}

export const adminInsertAssetService = (obj) =>
  request.post('/admin/asset', obj)

export const adminGetUKMapService = () =>
  request.get(
    'https://raw.githubusercontent.com/codeforgermany/click_that_hood/main/public/data/united-kingdom.geojson'
  )

export const adminDeleteAssetService = (assets) => {
  console.log(assets)
  return request.delete('/admin/asset', {
    data: { ids: assets }
  })
}

/**
 * template
 */

export const adminGetTemplateSerive = () => request.get('/admin/template')
