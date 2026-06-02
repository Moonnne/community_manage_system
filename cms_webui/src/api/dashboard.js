import request from '@/utils/request'

export function getSecurityDashboard(params, options = {}) {
  return request.get('/api/dashboard/security', { params, ...options })
}

export function getFacilityDashboard(options = {}) {
  return request.get('/api/dashboard/facility', options)
}

export function getAssetDashboard(params, options = {}) {
  return request.get('/api/dashboard/asset', { params, ...options })
}

export function getTaskDashboard(params, options = {}) {
  return request.get('/api/dashboard/task', { params, ...options })
}
