import request from '@/utils/request'

export const adminGetUsersService = () =>
  request.get('/admin/user/accumulate', {
    params: {
      function: 'count',
      column: '1'
    }
  })

export const adminGetAssetsService = () => request.get('/asset/')

export const adminGetWarningsService = () => request.get('/warning/all')
