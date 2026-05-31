import request from '@/utils/request'

export function login(data) {
  return request.post('/api/user/login', data)
}

export function register(data) {
  return request.post('/api/user/register', data)
}

export function getUserInfo() {
  return request.get('/api/user/info')
}

export function updateUser(data) {
  return request.post('/api/user/update', data)
}

export function changePassword(data) {
  return request.post('/api/user/password', data)
}

export function addUser(data) {
  return request.post('/api/user/add', data)
}

export function deleteUser(id) {
  return request.post('/api/user/delete', null, { params: { id } })
}

export function updateUserStatus(data) {
  return request.post('/api/user/status', data)
}

export function uploadUserAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/user/avatar/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getUserPage(params) {
  return request.get('/api/user/list', { params })
}

export function getUserRoles() {
  return request.get('/api/user/roles')
}

export function addUserRole(data) {
  return request.post('/api/user/role/add', data)
}

export function deleteUserRole(id) {
  return request.post('/api/user/role/delete', null, { params: { id } })
}
