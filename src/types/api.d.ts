export interface ApiResponse<T = void> {
  code: number
  message: string
  data: T
}

export interface NominatimResult {
  place_id: string
  name?: string
  display_name?: string
  class?: string
  type?: string
  importance?: number
  boundingbox?: string[]
  lat: string
  lon: string
}
