/**
 * only admin can use the api below
 */

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

export const adminGetUKMapService = () =>
  request.get(
    'https://raw.githubusercontent.com/codeforgermany/click_that_hood/main/public/data/united-kingdom.geojson'
  )
