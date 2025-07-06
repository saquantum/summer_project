/**
 * only admin can use the api below
 */

import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type {
  AssetWithWarnings,
  Template,
  User,
  UserItem,
  Warning
} from '@/types'

export const adminGetUsersService = (
  functionName: string,
  offset: number,
  limit: number,
  orderList: string
): Promise<ApiResponse<UserItem[]>> =>
  request.get('/admin/user/accumulate', {
    params: {
      function: functionName,
      column: '1',
      offset: offset,
      limit: limit,
      orderList: orderList
    }
  })

export const adminGetUserInfoService = (
  id: string
): Promise<ApiResponse<User>> => {
  return request.get(`/admin/user/aid/${id}`)
}

export const adminGetUserInfoByUIDService = (
  id: string
): Promise<ApiResponse<User>> => {
  return request.get(`/admin/user/uid/${id}`)
}

export const adminGetAssetsService = (
  offset: number,
  limit: number,
  orderList: string
): Promise<ApiResponse<AssetWithWarnings[]>> => {
  console.log(offset, limit, orderList)
  return request.get('/admin/asset/', {
    params: { offset: offset, limit: limit, orderList: orderList }
  })
}

export const adminGetUserAssetsService = (
  id: string
): Promise<ApiResponse<AssetWithWarnings[]>> => {
  return request.get(`/admin/user/aid/${id}/asset`)
}

export const adminGetAllWarningsService = (): Promise<ApiResponse<Warning[]>> =>
  request.get('/admin/warning/all')

export const adminGetAllLiveWarningsService = (): Promise<
  ApiResponse<Warning[]>
> => request.get('/warning')

// export const adminGetNotificationService = (): Promise<ApiResponse> => {

// }

export const adminInsertAssetService = (obj: object): Promise<ApiResponse> =>
  request.post('/admin/asset', obj)

export const adminGetUKMapService = (): Promise<ApiResponse> =>
  request.get(
    'https://raw.githubusercontent.com/codeforgermany/click_that_hood/main/public/data/united-kingdom.geojson'
  )

export const adminDeleteAssetService = (
  assets: string[]
): Promise<ApiResponse> => {
  console.log(assets)
  return request.delete('/admin/asset', {
    data: { ids: assets }
  })
}

/**
 * template
 */

export const adminGetTemplateSerive = (): Promise<ApiResponse<Template[]>> =>
  request.get('/admin/template')
