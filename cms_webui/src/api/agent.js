import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

function parseSseBlock(block) {
  const event = { type: 'message', data: null }
  const dataLines = []
  block.split('\n').forEach(line => {
    if (line.startsWith('event:')) {
      event.type = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  })
  const dataText = dataLines.join('\n')
  if (dataText) {
    try {
      event.data = JSON.parse(dataText)
    } catch {
      event.data = dataText
    }
  }
  return event
}

export async function sendAgentMessage(data, handlers = {}) {
  const userStore = useUserStore()
  const response = await fetch('/api/agent/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${userStore.token}`
    },
    body: JSON.stringify(data)
  })

  if (!response.ok || !response.body) {
    throw new Error('AI服务连接失败')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const blocks = buffer.split('\n\n')
    buffer = blocks.pop() || ''
    blocks.forEach(block => {
      if (!block.trim()) return
      const event = parseSseBlock(block)
      handlers[event.type]?.(event.data)
      handlers.event?.(event)
    })
  }
}

export function executeAgentAction(data) {
  return request.post('/api/agent/execute', data)
}
