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

export function getCarPage(params) {
  return request.get('/api/car/page', { params })
}

export function getCarTypes() {
  return request.get('/api/car/types')
}

export function addCarHistory(data) {
  return request.post('/api/car-history/add', data)
}

export function getCarHistoryPage(params) {
  return request.get('/api/car-history/page', { params })
}
