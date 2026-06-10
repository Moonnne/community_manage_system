import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export const AGENT_QUERY_KEY = '__agentAction'

export function buildAgentRoute(routePath, action = {}) {
  const query = {
    [AGENT_QUERY_KEY]: String(Date.now()),
    agentType: action.type || 'search'
  }

  Object.entries(action.payload || {}).forEach(([key, value]) => {
    if (key === 'routePath') return
    if (value === null || value === undefined || value === '') return
    query[key] = String(value)
  })

  if (action.actionName) query.agentActionName = action.actionName
  if (action.targetId !== null && action.targetId !== undefined) query.agentTargetId = String(action.targetId)

  return { path: routePath, query }
}

export function useAgentPageControl(options) {
  const route = useRoute()
  const router = useRouter()
  const {
    query,
    queryKeys = [],
    numericKeys = [],
    onSearch,
    onRefresh,
    onOpenAdd,
    onOpenEdit
  } = options

  const numericKeySet = new Set(numericKeys)

  function applySearch(routeQuery = route.query) {
    if (!query?.value) return false
    let changed = false
    queryKeys.forEach(key => {
      if (routeQuery[key] === undefined || !(key in query.value)) return
      query.value[key] = numericKeySet.has(key) ? Number(routeQuery[key]) : String(routeQuery[key])
      changed = true
    })
    if (changed) {
      query.value.pageNum = Number(routeQuery.pageNum || 1)
      query.value.pageSize = Number(routeQuery.pageSize || query.value.pageSize || 10)
    }
    return changed
  }

  async function handleAgentAction(routeQuery = route.query) {
    if (!routeQuery[AGENT_QUERY_KEY]) return
    const type = routeQuery.agentType || 'search'
    if (type === 'refresh') {
      await onRefresh?.()
      return
    }
    if (type === 'open_add') {
      await onOpenAdd?.(routeQuery)
      return
    }
    if (type === 'open_edit') {
      await onOpenEdit?.(routeQuery.agentTargetId, routeQuery)
      return
    }
    if (applySearch(routeQuery)) {
      await onSearch?.()
    } else {
      await onRefresh?.()
    }
  }

  watch(() => route.query[AGENT_QUERY_KEY], () => handleAgentAction())
  onMounted(() => handleAgentAction())

  return {
    applySearch,
    handleAgentAction,
    router
  }
}
