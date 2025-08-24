import request from '@/utils/request'

export const getMailService = (id: string) =>
  request.get(`/user/uid/${id}/inbox`)

export const readMailService = (uid: string, id: string) =>
  request.put(`/user/uid/${uid}/inbox/${id}`)

// currently only support single delete at a time
export const deleteMailService = (uid: string, id: number) =>
  request.delete(`/user/uid/${uid}/inbox/${id}`)
