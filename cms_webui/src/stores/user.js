import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('cms_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('cms_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userRole = computed(() => userInfo.value?.userRole ?? -1)
  const userStatus = computed(() => userInfo.value?.userStatus ?? 2)
  const canOperate = computed(() => userStatus.value === 0)
  const isFrozen = computed(() => userStatus.value === 1)
  const isDisabled = computed(() => userStatus.value === 2)

  const ROLE_ADMIN = 0
  const ROLE_MANAGER = 1
  const ROLE_PROPERTY = 2
  const ROLE_SECURITY = 3
  const ROLE_OWNER = 4

  function setLogin(data) {
    token.value = data.token
    userInfo.value = data
    localStorage.setItem('cms_token', data.token)
    localStorage.setItem('cms_user', JSON.stringify(data))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('cms_token')
    localStorage.removeItem('cms_user')
  }

  function updateUserInfo(data) {
    userInfo.value = { ...userInfo.value, ...data }
    localStorage.setItem('cms_user', JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRole,
    userStatus,
    canOperate,
    isFrozen,
    isDisabled,
    ROLE_ADMIN,
    ROLE_MANAGER,
    ROLE_PROPERTY,
    ROLE_SECURITY,
    ROLE_OWNER,
    setLogin,
    logout,
    updateUserInfo
  }
})
