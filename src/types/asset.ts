import type { Warning } from './warning'
import type { MultiPolygon } from 'geojson'

export interface Asset {
  id: string
  name: string
  typeId: string
  type: AssetType
  ownerId: string
  location: MultiPolygon
  capacityLitres: number
  material: string
  status: string
  installedAt: string
  lastInspection: string
  lastModified: number
}

export interface AssetSearchForm {
  id: string
  name: string
  typeId: string
  ownerId: string
  capacityLitres: number[]
  material: string
  status: string
  installedAt: [Date, Date] | null
  lastInspection: [Date, Date] | null
}

export interface AssetWithWarnings {
  asset: Asset
  warnings: Warning[]
  status?: string
}

export interface AssetForm {
  username: string
  name: string
  typeId: string
  ownerId: string
  address: string
  locations: MultiPolygon[]
  capacityLitres: number
  material: string
  status: string
  installedAt: string
  lastInspection: string
  location: MultiPolygon
}

export interface AssetInfoForm {
  id: string
  name?: string
  typeId?: string
  capacityLitres?: number
  material?: string
  status?: string
  installedAt?: string
  lastInspection?: string
  location?: MultiPolygon
}

export interface AssetTableItem {
  id: string
  name: string
  type: string
  capacityLitres: number
  material: string
  status: string
  installedAt: string
  lastInspection: string
  assetHolderId: string
  warningLevel: string
}

export interface AssetFilter {
  asset_id?: {
    op: string
    val: string
  }
  asset_name?: {
    op: string
    val: string
  }
  asset_owner_id?: {
    op: string
    val: string
  }
  warning_level?: {
    op: string
    val: string
  }
  asset_type_id?: {
    op: string
    val: string
  }
  asset_capacity_litres?: {
    op: string
    min: number
    max: number
  }
  asset_material?: {
    op: string
    val: string
  }
  asset_status?: {
    op: string
    val: string
  }
  asset_installed_at?: {
    op: string
    min: string
    max: string
  }
  asset_last_inspection?: {
    op: string
    min: string
    max: string
  }
}

export interface AssetSearchBody {
  filters: AssetFilter
  orderList: string
  limit: number
  offset: number
}

export interface AssetType {
  id?: string
  name: string
  description: string
}
