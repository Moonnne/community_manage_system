import request from '@/utils/request'

export function addTask(data) {
  return request.post('/api/task/add', data)
}

export function updateTask(data) {
  return request.post('/api/task/update', data)
}

export function deleteTask(id) {
  return request.post('/api/task/delete', null, { params: { id } })
}

export function getTaskPage(params, options = {}) {
  return request.get('/api/task/page', { params, ...options })
}

export function processTask(params) {
  return request.post('/api/task/process', null, { params })
}

export function getTaskTypes(options = {}) {
  return request.get('/api/task/types', options)
}

export function addTaskType(data) {
  return request.post('/api/task/type/add', data)
}

export function deleteTaskType(id) {
  return request.post('/api/task/type/delete', null, { params: { id } })
}
