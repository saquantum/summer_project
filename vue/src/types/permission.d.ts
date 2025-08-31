export interface Permission {
  userId: string
  canCreateAsset: boolean
  canSetPolygonOnCreate: boolean
  canUpdateAssetFields: boolean
  canUpdateAssetPolygon: boolean
  canDeleteAsset: boolean
  canUpdateProfile: boolean
}

export interface PermissionGroup {
  rowId: string
  name: string
  description: string
  canCreateAsset: boolean
  canSetPolygonOnCreate: boolean
  canUpdateAssetFields: boolean
  canUpdateAssetPolygon: boolean
  canDeleteAsset: boolean
  canUpdateProfile: boolean
}
