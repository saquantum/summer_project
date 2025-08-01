/**
 * only admin can use the api below
 */
import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type {
  Asset,
  AssetForm,
  AssetInfoForm,
  AssetSearchBody,
  AssetType,
  AssetWithWarnings,
  Template,
  User,
  UserInfoForm,
  UserItem,
  UserSearchBody,
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

export const adminSearchUsersService = (
  functionName: string,
  obj: UserSearchBody
): Promise<ApiResponse<UserItem[]>> =>
  request.post('/admin/user/accumulate/search', obj, {
    params: { function: functionName, column: '1' }
  })

export const adminGetUserInfoService = (
  id: string
): Promise<ApiResponse<User>> => {
  return request.get(`/admin/user/uid/${id}`)
}

export const adminGetUserInfoByAIDService = (aid: string) =>
  request.get(`/admin/user/aid/${aid}`)

export const adminInsertUserService = (
  users: UserInfoForm[]
): Promise<ApiResponse> => request.post('/admin/user', users)

export const adminDeleteUserService = (users: string[]) =>
  request.delete('/admin/user', {
    data: { ids: users }
  })

export const adminUpdateUserInfoService = (userInforArr: UserInfoForm[]) =>
  request.put('/admin/user', userInforArr)

export const adminGetUsersTotalService = (obj: object) =>
  request.post('/admin/user/count', obj)
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

export const adminGetTemplateByTypesService = (
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

export const adminUpdateTemplateByIdService = (template: Template) =>
  request.put('/admin/template/id', template)

export const adminDeleteTemplateByIdService = (ids: number[]) =>
  request.delete('/admin/template', {
    data: { ids: ids }
  })

/**
 * asset
 */
export const adminGetAssetsService = (
  offset: number,
  limit: number,
  orderList: string
): Promise<ApiResponse<AssetWithWarnings[]>> =>
  request.get('/admin/asset/', {
    params: { offset: offset, limit: limit, orderList: orderList }
  })

export const adminSearchAssetService = (obj: AssetSearchBody) => {
  return request.post('/admin/asset/search', obj)
}

export const adminGetUserAssetsService = (
  id: string
): Promise<ApiResponse<AssetWithWarnings[]>> => {
  return request.get(`/admin/user/uid/${id}/asset`)
}

export const adminDeleteAssetService = (
  assets: string[]
): Promise<ApiResponse> =>
  request.delete('/admin/asset', {
    data: { ids: assets }
  })

export const adminGetAssetByIdService = (
  id: string
): Promise<ApiResponse<AssetWithWarnings[]>> =>
  request.get(`/admin/asset/${id}`)

export const adminInsertAssetService = (obj: AssetForm): Promise<ApiResponse> =>
  request.post('/admin/asset', obj)

export const adminUpdateAssetService = (obj: AssetInfoForm | Asset) =>
  request.put('/admin/asset', obj)

export const adminInsetAssetTypeService = (obj: AssetType) =>
  request.post('/admin/asset/type', obj)

export const adminUpdateAssetTypeService = (obj: AssetType) =>
  request.put('/admin/asset/type', obj)

export const adminDeleteAssetTypeService = (types: string[]) =>
  request.delete('/admin/asset/type', {
    data: { ids: types }
  })

export const adminGetAssetsTotalService = (obj: object) =>
  request.post('/admin/asset/count', obj)
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

/**
 * message
 */

export const adminSendMessageService = (obj: object) =>
  request.post('/admin/notify/inbox', obj)
