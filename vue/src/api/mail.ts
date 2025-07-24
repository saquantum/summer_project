import request from '@/utils/request'

export const getMailService = (id: string) =>
  request.get(`/user/uid/${id}/inbox`)

export const readMailService = (uid: string, id: string) =>
  request.put(`/user/uid/${uid}/inbox/${id}`)
