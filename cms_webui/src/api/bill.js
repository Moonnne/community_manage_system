import request from '@/utils/request'

export function addBill(data) {
  return request.post('/api/bill/add', data)
}

export function deleteBill(id) {
  return request.post('/api/bill/delete', null, { params: { id } })
}

export function updateBill(data) {
  return request.post('/api/bill/update', data)
}

export function updateBillStatus(data) {
  return request.post('/api/bill/status', data)
}

export function getBill(id) {
  return request.get(`/api/bill/${id}`)
}

export function getBillPage(params) {
  return request.get('/api/bill/page', { params })
}

export function addBillDetail(data) {
  return request.post('/api/bill/detail/add', data)
}

export function updateBillDetail(data) {
  return request.post('/api/bill/detail/update', data)
}

export function deleteBillDetail(id) {
  return request.post('/api/bill/detail/delete', null, { params: { id } })
}

export function getBillDetails(billId) {
  return request.get(`/api/bill/detail/${billId}`)
}

export function getBillTypes() {
  return request.get('/api/bill/types')
}

export function addBillType(data) {
  return request.post('/api/bill/type/add', data)
}

export function deleteBillType(id) {
  return request.post('/api/bill/type/delete', null, { params: { id } })
}
