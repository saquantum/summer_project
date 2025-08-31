import type { MultiPolygon } from 'geojson'

export interface Style {
  weight: number
  fillOpacity: number
  color: string
  fillColor: string
}

export interface Area {
  type: string
  coordinates: number[][][][]
  style: Style
}

export interface Warning {
  id: number
  weatherType: string
  warningLevel: string
  warningHeadLine: string
  validFrom: number
  validTo: number
  warningImpact: string
  warningLikelihood: string
  affectedAreas: string
  whatToExpect: string
  warningFurtherDetails: string
  warningUpdateDescription: string
  area: MultiPolygon
}
