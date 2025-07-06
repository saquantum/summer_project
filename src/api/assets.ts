import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { AssetWithWarnings } from '@/types'
import type { MultiPolygon } from 'geojson'
export const assetsGetInfoService = (
  userId: string
): Promise<ApiResponse<AssetWithWarnings[]>> =>
  request.get(`/user/aid/${userId}/asset`)

export const assetUpdateInfoService = (
  id: string,
  ownerId: string,
  location: MultiPolygon
): Promise<ApiResponse> => {
  const obj = {
    id: id,
    ownerId: ownerId,
    location: location
  }
  console.log(obj)
  return request.put('/admin/asset', obj)
}

export const assetInsertService = (obj: object): Promise<ApiResponse> => {
  return request.post('/admin/asset', obj)
}
