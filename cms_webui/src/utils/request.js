import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: '/',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    const method = (config.method || 'get').toLowerCase()
    const isWriteRequest = !['get', 'head', 'options'].includes(method)
    const isAllowedFrozenRequest = config.url === '/api/user/login' || config.url === '/api/user/register'
    if (userStore.isFrozen && isWriteRequest && !isAllowedFrozenRequest) {
      ElMessage.error('账号已冻结，无法执行操作')
      return Promise.reject(new Error('账号已冻结，无法执行操作'))
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (res.code === 403 && res.message?.includes('冻结')) {
        const userStore = useUserStore()
        userStore.updateUserInfo({ userStatus: 1 })
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else {
        ElMessage.error(error.response.data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default service
