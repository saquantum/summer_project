export interface Mail {
  issuedDate: number | string
  hasRead: boolean
  validUntil: number
  title: string
  message: string
  userId: string
  rowId: number
}
