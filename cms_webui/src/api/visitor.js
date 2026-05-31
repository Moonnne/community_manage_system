import request from '@/utils/request'

export function addVisitor(data) {
  return request.post('/api/visitor/add', data)
}

export function updateVisitor(data) {
  return request.post('/api/visitor/update', data)
}

export function deleteVisitor(id) {
  return request.post('/api/visitor/delete', null, { params: { id } })
}

export function getVisitorPage(params, options = {}) {
  return request.get('/api/visitor/page', { params, ...options })
}

export function getVisitorTypes(options = {}) {
  return request.get('/api/visitor/types', options)
}

export function addVisitorType(data) {
  return request.post('/api/visitor/type/add', data)
}

export function deleteVisitorType(id) {
  return request.post('/api/visitor/type/delete', null, { params: { id } })
}
