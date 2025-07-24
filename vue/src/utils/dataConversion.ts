import type {
  AssetFilter,
  AssetSearchForm,
  UserFilter,
  UserSearchForm
} from '@/types'

export const assetConverFormToFilter = (form: AssetSearchForm): AssetFilter => {
  const body: AssetFilter = {}

  if (form.id) {
    body.asset_id = { op: 'like', val: `%${form.id}%` }
  }

  if (form.name) {
    body.asset_name = { op: 'like', val: `%${form.name}%` }
  }

  if (form.ownerId) {
    body.asset_owner_id = { op: 'like', val: `%${form.ownerId}%` }
  }

  if (form.typeId) {
    body.asset_type_id = { op: 'like', val: `%${form.typeId}%` }
  }

  if (form.capacityLitres?.length === 2) {
    const [min, max] = form.capacityLitres
    body.asset_capacity_litres = {
      op: 'range',
      min,
      max
    }
  }

  if (form.material) {
    body.asset_material = { op: 'like', val: `%${form.material}%` }
  }

  if (form.status) {
    body.asset_status = { op: 'like', val: `%${form.status}%` }
  }

  if (form.installedAt?.length === 2) {
    const [start, end] = form.installedAt
    body.asset_installed_at = {
      op: 'range',
      min: start.toISOString().slice(0, 10),
      max: end.toISOString().slice(0, 10)
    }
  }

  if (form.lastInspection?.length === 2) {
    const [start, end] = form.lastInspection
    body.asset_last_inspection = {
      op: 'range',
      min: start.toISOString().slice(0, 10),
      max: end.toISOString().slice(0, 10)
    }
  }

  if (form.warningLevel && form.warningLevel !== 'NO') {
    body.warning_level = { op: 'like', val: `%${form.warningLevel}%` }
  } else if (form.warningLevel && form.warningLevel === 'NO') {
    body.warning_level = { op: 'isNull', val: '' }
  }

  return body
}

export const userConverFormToFilter = (form: UserSearchForm): UserFilter => {
  const body: UserFilter = {}

  if (form.id) {
    body.user_id = { op: 'like', val: `%${form.id}%` }
  }

  return body
}
