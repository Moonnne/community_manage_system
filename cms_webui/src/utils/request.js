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
      ElMessage({ message: '账号已冻结，无法执行操作', type: 'error', duration: 1000 })
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
      ElMessage({ message: res.message || '请求失败', type: 'error', duration: 1000 })
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
        ElMessage({ message: '登录已过期，请重新登录', type: 'error', duration: 1000 })
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status === 403) {
        ElMessage({ message: '无权限访问', type: 'error', duration: 1000 })
      } else {
        ElMessage({ message: error.response.data?.message || '服务器错误', type: 'error', duration: 1000 })
      }
    } else {
      ElMessage({ message: '网络连接失败', type: 'error', duration: 1000 })
    }
    return Promise.reject(error)
  }
)

export default service
