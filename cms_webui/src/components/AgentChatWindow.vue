<template>
  <div
    v-if="userStore.isLoggedIn"
    class="agent-shell"
    :class="{ collapsed }"
    :style="shellStyle"
  >
    <button
      v-if="collapsed"
      type="button"
      class="agent-launcher"
      @click="collapsed = false"
      title="AI 助手"
    >
      <el-icon :size="22"><ChatDotRound /></el-icon>
    </button>

    <section v-else class="agent-panel">
      <header class="agent-header" @pointerdown="startDrag">
        <div class="agent-title">
          <el-icon :size="18"><ChatDotRound /></el-icon>
          <span>AI 助手</span>
        </div>
        <div class="agent-actions">
          <button type="button" class="icon-button" title="清空" @click.stop="clearMessages">
            <el-icon><Delete /></el-icon>
          </button>
          <button type="button" class="icon-button" title="折叠" @click.stop="collapsed = true">
            <el-icon><Minus /></el-icon>
          </button>
        </div>
      </header>

      <div ref="messageListRef" class="agent-messages">
        <div
          v-for="message in messages"
          :key="message.id"
          class="agent-message"
          :class="`agent-message-${message.role}`"
        >
          <div class="message-bubble">
            <p v-if="message.content">{{ message.content }}</p>

            <div v-if="message.action" class="action-card">
              <div class="action-card-head">
                <span>{{ message.action.title || '待确认操作' }}</span>
                <el-tag size="small" type="warning">需确认</el-tag>
              </div>
              <p class="action-summary">{{ message.action.summary }}</p>
              <dl class="action-payload">
                <template v-for="item in payloadEntries(message.action.payload)" :key="item.key">
                  <dt>{{ item.key }}</dt>
                  <dd>{{ item.value }}</dd>
                </template>
              </dl>
              <div class="action-card-foot">
                <el-button size="small" @click="cancelAction(message)">取消</el-button>
                <el-button size="small" type="primary" :loading="message.executing" @click="confirmAction(message)">
                  确认执行
                </el-button>
              </div>
            </div>

            <div v-if="message.result" class="result-card">
              <div class="result-title">{{ message.result.title || '查询结果' }}</div>
              <pre>{{ formatResult(message.result.data) }}</pre>
              <el-button
                v-if="message.result.routePath"
                text
                type="primary"
                size="small"
                @click="router.push(message.result.routePath)"
              >
                打开页面
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <form class="agent-input" @submit.prevent="sendMessage">
        <el-input
          v-model="input"
          :disabled="loading"
          resize="none"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="输入要查询、跳转或操作的内容"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :loading="loading" :disabled="!input.trim()" native-type="submit">
          发送
        </el-button>
      </form>

      <div class="resize-handle" @pointerdown="startResize"></div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { sendAgentMessage, executeAgentAction } from '@/api/agent'
import { useUserStore } from '@/stores/user'
import { usePageCacheStore } from '@/stores/pageCache'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const pageCacheStore = usePageCacheStore()

const collapsed = ref(true)
const loading = ref(false)
const input = ref('')
const messageListRef = ref(null)
const messages = ref([
  {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: '你好，我可以帮你查询、跳转页面，或执行确认后的操作。'
  }
])

const state = reactive({
  left: 24,
  bottom: 24,
  width: 420,
  height: 560
})

const shellStyle = computed(() => ({
  left: `${state.left}px`,
  bottom: `${state.bottom}px`,
  width: collapsed.value ? '56px' : `${state.width}px`,
  height: collapsed.value ? '56px' : `${state.height}px`
}))

function clearMessages() {
  messages.value = []
}

async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return
  input.value = ''
  loading.value = true
  const assistantMessage = {
    id: crypto.randomUUID(),
    role: 'assistant',
    content: ''
  }
  messages.value.push({ id: crypto.randomUUID(), role: 'user', content: text })
  messages.value.push(assistantMessage)
  await scrollToBottom()

  try {
    await sendAgentMessage({
      message: text,
      currentPath: route.path,
      history: messages.value
        .filter(item => item.content)
        .slice(-8)
        .map(item => ({ role: item.role, content: item.content }))
    }, {
      message(data) {
        assistantMessage.content += data?.content || ''
        scrollToBottom()
      },
      action(data) {
        handleAction(data, assistantMessage)
      },
      pending_action(data) {
        assistantMessage.action = data
        scrollToBottom()
      },
      error(data) {
        assistantMessage.content += data?.message || 'AI服务暂时不可用'
      }
    })
  } catch (error) {
    assistantMessage.content += error.message || 'AI服务暂时不可用'
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

function handleAction(action, message) {
  if (!action) return
  if (action.type === 'navigate' && action.data?.routePath) {
    router.push(action.data.routePath)
    return
  }
  message.result = action
  scrollToBottom()
}

async function confirmAction(message) {
  if (!message.action || message.executing) return
  message.executing = true
  try {
    const res = await executeAgentAction({
      actionName: message.action.actionName,
      payload: message.action.payload || {}
    })
    pageCacheStore.clearAll()
    message.action = null
    message.result = res.data
    if (res.data?.routePath) {
      router.push(res.data.routePath)
    }
    ElMessage.success('操作已完成')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    message.executing = false
    await scrollToBottom()
  }
}

function cancelAction(message) {
  message.action = null
  message.content += '\n已取消该操作。'
}

function payloadEntries(payload = {}) {
  return Object.entries(payload)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => ({
      key,
      value: typeof value === 'object' ? JSON.stringify(value) : String(value)
    }))
}

function formatResult(data) {
  if (!data) return '无数据'
  if (data.records) {
    return JSON.stringify(data.records.slice(0, 5), null, 2)
  }
  return JSON.stringify(data, null, 2)
}

async function scrollToBottom() {
  await nextTick()
  const el = messageListRef.value
  if (el) el.scrollTop = el.scrollHeight
}

function startDrag(event) {
  if (event.button !== 0) return
  const startX = event.clientX
  const startY = event.clientY
  const startLeft = state.left
  const startBottom = state.bottom

  function move(moveEvent) {
    state.left = clamp(startLeft + moveEvent.clientX - startX, 8, window.innerWidth - state.width - 8)
    state.bottom = clamp(startBottom - (moveEvent.clientY - startY), 8, window.innerHeight - state.height - 8)
  }

  bindPointer(move)
}

function startResize(event) {
  event.preventDefault()
  const startX = event.clientX
  const startY = event.clientY
  const startWidth = state.width
  const startHeight = state.height

  function move(moveEvent) {
    state.width = clamp(startWidth + moveEvent.clientX - startX, 320, Math.min(720, window.innerWidth - state.left - 8))
    state.height = clamp(startHeight + moveEvent.clientY - startY, 420, Math.min(760, window.innerHeight - state.bottom - 8))
  }

  bindPointer(move)
}

function bindPointer(move) {
  function up() {
    window.removeEventListener('pointermove', move)
    window.removeEventListener('pointerup', up)
  }
  window.addEventListener('pointermove', move)
  window.addEventListener('pointerup', up, { once: true })
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}
</script>

<style scoped>
.agent-shell {
  position: fixed;
  z-index: 1200;
  max-width: calc(100vw - 16px);
  max-height: calc(100vh - 16px);
}

.agent-launcher {
  width: 56px;
  height: 56px;
  border: 0;
  border-radius: 50%;
  color: #fff;
  background: var(--vp-c-brand-1);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.22);
  cursor: pointer;
}

.agent-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
  box-shadow: 0 18px 46px rgba(0, 0, 0, 0.2);
}

.agent-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 46px;
  padding: 0 12px;
  border-bottom: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  cursor: move;
  user-select: none;
}

.agent-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--vp-c-text-1);
  font-size: 14px;
  font-weight: 700;
}

.agent-actions {
  display: flex;
  gap: 4px;
}

.icon-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 6px;
  color: var(--vp-c-text-2);
  background: transparent;
  cursor: pointer;
}

.icon-button:hover {
  color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

.agent-messages {
  flex: 1;
  min-height: 0;
  padding: 14px;
  overflow-y: auto;
}

.agent-message {
  display: flex;
  margin-bottom: 12px;
}

.agent-message-user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 86%;
  padding: 10px 12px;
  border-radius: 8px;
  color: var(--vp-c-text-1);
  background: var(--vp-c-bg-soft);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.agent-message-user .message-bubble {
  color: #fff;
  background: var(--vp-c-brand-1);
}

.message-bubble p {
  margin: 0;
}

.action-card,
.result-card {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
}

.action-card-head,
.action-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.action-summary {
  margin: 8px 0;
  color: var(--vp-c-text-2);
}

.action-payload {
  display: grid;
  grid-template-columns: minmax(72px, 0.4fr) 1fr;
  gap: 4px 8px;
  margin: 8px 0 12px;
  font-size: 12px;
}

.action-payload dt {
  color: var(--vp-c-text-3);
}

.action-payload dd {
  min-width: 0;
  margin: 0;
  overflow-wrap: anywhere;
}

.result-title {
  margin-bottom: 6px;
  font-weight: 700;
}

.result-card pre {
  max-height: 160px;
  margin: 0;
  overflow: auto;
  font-family: ui-monospace, SFMono-Regular, Consolas, monospace;
  font-size: 12px;
  white-space: pre-wrap;
}

.agent-input {
  display: grid;
  grid-template-columns: 1fr 76px;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
}

.resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 18px;
  height: 18px;
  cursor: nwse-resize;
}

.resize-handle::after {
  position: absolute;
  right: 5px;
  bottom: 5px;
  width: 8px;
  height: 8px;
  border-right: 2px solid var(--vp-c-divider);
  border-bottom: 2px solid var(--vp-c-divider);
  content: "";
}

@media (max-width: 768px) {
  .agent-shell:not(.collapsed) {
    left: 8px !important;
    bottom: 8px !important;
    width: calc(100vw - 16px) !important;
    height: min(620px, calc(100vh - 80px)) !important;
  }

  .agent-input {
    grid-template-columns: 1fr;
  }
}
</style>
