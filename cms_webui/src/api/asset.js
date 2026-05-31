import request from '@/utils/request'

export function addAsset(data) {
  return request.post('/api/asset/add', data)
}

export function updateAsset(data) {
  return request.post('/api/asset/update', data)
}

export function deleteAsset(id) {
  return request.post('/api/asset/delete', null, { params: { id } })
}

export function getAssetPage(params, options = {}) {
  return request.get('/api/asset/page', { params, ...options })
}

export function getAssetTypes(options = {}) {
  return request.get('/api/asset/types', options)
}
