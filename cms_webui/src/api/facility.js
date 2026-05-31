import request from '@/utils/request'

export function addFacility(data) {
  return request.post('/api/facility/add', data)
}

export function updateFacility(data) {
  return request.post('/api/facility/update', data)
}

export function deleteFacility(id) {
  return request.post('/api/facility/delete', null, { params: { id } })
}

export function getFacilityPage(params, options = {}) {
  return request.get('/api/facility/page', { params, ...options })
}

export function getNearExpirationFacilities(months, options = {}) {
  return request.get('/api/facility/near-expiration', { params: { months }, ...options })
}

export function getFacilityTypes(options = {}) {
  return request.get('/api/facility/types', options)
}

export function addFacilityType(data) {
  return request.post('/api/facility/type/add', data)
}

export function deleteFacilityType(id) {
  return request.post('/api/facility/type/delete', null, { params: { id } })
}
