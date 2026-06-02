import request from '@/utils/request'

export function addBill(data) {
  return request.post('/api/bill/add', data)
}

export function deleteBill(id) {
  return request.post('/api/bill/delete', null, { params: { id } })
}

export function batchDeleteBills(ids) {
  return request.post('/api/bill/delete/batch', { ids })
}

export function updateBill(data) {
  return request.post('/api/bill/update', data)
}

export function updateBillStatus(data) {
  return request.post('/api/bill/status', data)
}

export function getBill(id, options = {}) {
  return request.get(`/api/bill/${id}`, options)
}

export function getBillPage(params, options = {}) {
  return request.get('/api/bill/page', { params, ...options })
}

export function getMyBillSummary(options = {}) {
  return request.get('/api/bill/my/summary', options)
}

export function getMyUnpaidBills(params, options = {}) {
  return request.get('/api/bill/my/unpaid', { params, ...options })
}

export function payBill(id) {
  return request.post('/api/bill/pay', { id })
}

export function payBillDetail(data) {
  return request.post('/api/bill/detail/pay', data)
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

export function getBillDetails(billId, options = {}) {
  return request.get(`/api/bill/detail/${billId}`, options)
}

export function getBillTypes(options = {}) {
  return request.get('/api/bill/types', options)
}

export function addBillType(data) {
  return request.post('/api/bill/type/add', data)
}

export function deleteBillType(id) {
  return request.post('/api/bill/type/delete', null, { params: { id } })
}
