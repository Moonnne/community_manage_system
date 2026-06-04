import request from '@/utils/request'

export function addCar(data) {
  return request.post('/api/car/add', data)
}

export function updateCar(data) {
  return request.post('/api/car/update', data)
}

export function deleteCar(id) {
  return request.post('/api/car/delete', null, { params: { id } })
}

export function getCarPage(params, options = {}) {
  return request.get('/api/car/page', { params, ...options })
}

export function getCarTypes(options = {}) {
  return request.get('/api/car/types', options)
}

export function addCarHistory(data) {
  return request.post('/api/car-history/add', data)
}

export function updateCarHistory(data) {
  return request.post('/api/car-history/update', data)
}

export function deleteCarHistory(id) {
  return request.post('/api/car-history/delete', null, { params: { id } })
}

export function batchDeleteCarHistory(ids) {
  return request.post('/api/car-history/delete/batch', { ids })
}

export function getCarHistoryPage(params, options = {}) {
  return request.get('/api/car-history/page', { params, ...options })
}
