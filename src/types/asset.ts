import type { Warning } from './warning'
import type { MultiPolygon } from 'geojson'
interface AssetType {
  id: string
  name: string
  description: string
}

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
  capacityLitres: string
  material: string
  status: string
  installedAt: string
  lastInspection: string
  location: MultiPolygon | ''
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
