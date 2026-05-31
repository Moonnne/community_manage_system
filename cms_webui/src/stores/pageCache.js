import { defineStore } from 'pinia'
import { ref } from 'vue'

const DEFAULT_TTL = 60 * 1000
const CLEAN_INTERVAL = 60 * 1000

function stableStringify(value) {
  if (value === null || typeof value !== 'object') return JSON.stringify(value)
  if (Array.isArray(value)) return `[${value.map(stableStringify).join(',')}]`
  return `{${Object.keys(value).sort().map(key => `${JSON.stringify(key)}:${stableStringify(value[key])}`).join(',')}}`
}

export const usePageCacheStore = defineStore('pageCache', () => {
  const cacheMap = ref(new Map())
  let cleanTimer = null

  function makeKey(config, userId) {
    const method = (config.method || 'get').toLowerCase()
    const url = config.url || ''
    const params = config.params || {}
    return `${userId || 'anonymous'}::${method}::${url}::${stableStringify(params)}`
  }

  function get(key) {
    const item = cacheMap.value.get(key)
    if (!item) return null
    if (Date.now() - item.createdAt > item.ttl) {
      cacheMap.value.delete(key)
      return null
    }
    return item.data
  }

  function set(key, data, ttl = DEFAULT_TTL) {
    cacheMap.value.set(key, { data, ttl, createdAt: Date.now() })
    ensureCleaner()
  }

  function clearAll() {
    cacheMap.value.clear()
  }

  function pruneExpired() {
    const now = Date.now()
    for (const [key, item] of cacheMap.value.entries()) {
      if (now - item.createdAt > item.ttl) {
        cacheMap.value.delete(key)
      }
    }
    if (cacheMap.value.size === 0 && cleanTimer) {
      window.clearInterval(cleanTimer)
      cleanTimer = null
    }
  }

  function ensureCleaner() {
    if (cleanTimer || typeof window === 'undefined') return
    cleanTimer = window.setInterval(pruneExpired, CLEAN_INTERVAL)
  }

  return { makeKey, get, set, clearAll, pruneExpired }
})
