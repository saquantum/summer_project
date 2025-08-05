import request from '@/utils/request'

export const getMailService = (id: string) =>
  request.get(`/user/uid/${id}/inbox`)

export const readMailService = (uid: string, id: string) =>
  request.put(`/user/uid/${uid}/inbox/${id}`)

export const deleteMailService = (uid: string, ids: string[]) =>
  request.delete(`/user/uid/${uid}/inbox`, {
    data: { ids: ids }
  })
