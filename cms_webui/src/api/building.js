import request from '@/utils/request'

export function addBuilding(data) {
  return request.post('/api/building/add', data)
}

export function updateBuilding(data) {
  return request.post('/api/building/update', data)
}

export function deleteBuilding(id) {
  return request.post('/api/building/delete', null, { params: { id } })
}

export function getBuildingPage(params, options = {}) {
  return request.get('/api/building/page', { params, ...options })
}

export function getBuildingList(options = {}) {
  return request.get('/api/building/list', options)
}
