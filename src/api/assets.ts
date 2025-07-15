import request from '@/utils/request'
import type { ApiResponse } from '@/types/api'
import type { Asset, AssetInfoForm, AssetWithWarnings } from '@/types'

export const assetsGetInfoService = (
  userId: string
): Promise<ApiResponse<AssetWithWarnings[]>> =>
  request.get(`/user/uid/${userId}/asset`)

export const getAssetByIdService = (uid: string, id: string) =>
  request.get(`user/uid/${uid}/asset/${id}`)

export const updateAssetByIdService = (
  uid: string,
  obj: AssetInfoForm | Asset
) => request.put(`/user/uid/${uid}/asset`, obj)
