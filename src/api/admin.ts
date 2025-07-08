/**
 * only admin can use the api below
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type {
  AssetWithWarnings,
  Template,
  User,
  UserInfoForm,
  UserItem,
  Warning
} from '@/types'
import type { Permission } from '@/types/permission'

/**
 * user
 */

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

export const adminInsertUserService = (
  users: UserInfoForm[]
): Promise<ApiResponse> => request.post('/admin/user', users)

export const adminDeleteUserService = (params: { ids: string[] }) =>
  request.delete('/admin/user', {
    data: params
  })

/**
 * template
 */
export const adminGetTemplateSerive = (
  offset: number,
  limit: number,
  orderList: string
): Promise<ApiResponse<Template[]>> =>
  request.get('/admin/template', {
    params: { offset: offset, limit: limit, orderList: orderList }
  })

export const adminUpdateUserInfoService = (userInforArr: UserInfoForm[]) =>
  request.put('/admin/user', userInforArr)

/**
 * asset
 */
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

export const adminDeleteAssetService = (
  assets: string[]
): Promise<ApiResponse> => {
  console.log(assets)
  return request.delete('/admin/asset', {
    data: { ids: assets }
  })
}

export const adminInsertAssetService = (obj: object): Promise<ApiResponse> =>
  request.post('/admin/asset', obj)

/**
 * warning
 */

export const adminGetAllWarningsService = (): Promise<ApiResponse<Warning[]>> =>
  request.get('/admin/warning/all')

export const adminGetAllLiveWarningsService = (): Promise<
  ApiResponse<Warning[]>
> => request.get('/warning')

/**
 * permisssion
 */

export const adminGetAllPermissionService = (): Promise<
  ApiResponse<Permission[]>
> => request.get('/admin/permission')

export const adminGetPermissionByUIDService = (
  id: string
): Promise<ApiResponse<Permission[]>> => request.get(`/admin/permission/${id}`)

export const adminUpdatePermissionService = (
  permission: Permission
): Promise<ApiResponse<Permission[]>> =>
  request.put(`/admin/permission/`, permission)

export const adminGetTemplateByTypes = (
  assetTypeId: string,
  warningType: string,
  severity: string,
  channel: string
) =>
  request.get('/admin/template/type', {
    params: {
      assetTypeId: assetTypeId,
      warningType: warningType,
      severity: severity,
      channel: channel
    }
  })
