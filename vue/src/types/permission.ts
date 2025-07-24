export interface Permission {
  userId: string
  canCreateAsset: boolean
  canSetPolygonOnCreate: boolean
  canUpdateAssetFields: boolean
  canUpdateAssetPolygon: boolean
  canDeleteAsset: boolean
  canUpdateProfile: boolean
}
