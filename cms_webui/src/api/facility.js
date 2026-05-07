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

export function getFacilityPage(params) {
  return request.get('/api/facility/page', { params })
}

export function getNearExpirationFacilities(months) {
  return request.get('/api/facility/near-expiration', { params: { months } })
}

export function getFacilityTypes() {
  return request.get('/api/facility/types')
}

export function addFacilityType(data) {
  return request.post('/api/facility/type/add', data)
}

export function deleteFacilityType(id) {
  return request.post('/api/facility/type/delete', null, { params: { id } })
}
