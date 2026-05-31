import request from '@/utils/request'

export function addGate(data) {
  return request.post('/api/gate/add', data)
}

export function updateGate(data) {
  return request.post('/api/gate/update', data)
}

export function deleteGate(id) {
  return request.post('/api/gate/delete', null, { params: { id } })
}

export function getGateList(options = {}) {
  return request.get('/api/gate/list', options)
}
