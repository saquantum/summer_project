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

export const adminGetAssetsService = (offset, limit, orderList) => {
  console.log(offset, limit, orderList)
  return request.get('/admin/asset/', {
    params: { offset: offset, limit: limit, orderList: orderList }
  })
}

export const adminGetUserAssetsService = (id) => {
  return request.get(`/admin/user/aid/${id}/asset`)
}

export const adminGetWarningsService = () => request.get('/admin/warning/all')

export const adminGetUKMapService = () =>
  request.get(
    'https://raw.githubusercontent.com/codeforgermany/click_that_hood/main/public/data/united-kingdom.geojson'
  )
