import { computed, nextTick, ref } from 'vue'
import api from '../services/api'

function createChatId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) return crypto.randomUUID()
  return `chat-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function createMessage(role, content = '', extra = {}) {
  return {
    id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2)}`,
    role,
    content,
    createdAt: new Date(),
    ...extra,
  }
}

function parsePayload(payload) {
  const normalized = payload.replace(/^\uFEFF/, '')
  if (normalized === '[DONE]') return null
  try {
    const parsed = JSON.parse(normalized)
    if (typeof parsed === 'string') return parsed
    if (parsed && typeof parsed === 'object') {
      return parsed.data ?? parsed.content ?? parsed.text ?? parsed.message ?? normalized
    }
  } catch {
    // Most Spring WebFlux chunks are plain text, so JSON parsing is optional.
  }
  return normalized
}

function consumeEventBlock(block, append) {
  const lines = block.split(/\r?\n/)
  const dataLines = lines.filter((line) => line.startsWith('data:'))
  const payload = dataLines.length
    ? dataLines.map((line) => line.slice(5).replace(/^ /, '')).join('\n')
    : lines.filter(Boolean).join('\n')
  if (!payload) return false
  const parsed = parsePayload(payload)
  if (parsed !== null && parsed !== undefined) append(String(parsed))
  return parsed === null
}

export function useSseChat({ path, welcome, intro, requiresChatId = false }) {
  const chatId = ref(createChatId())
  const input = ref('')
  const messages = ref([createMessage('assistant', welcome)])
  const isStreaming = ref(false)
  const error = ref('')
  const lastPrompt = ref('')
  const streamMessageId = ref('')
  let controller = null

  const canSend = computed(() => input.value.trim().length > 0 && !isStreaming.value)
  const sessionLabel = computed(() => `${chatId.value.slice(0, 8)}…`)

  function appendChunk(content) {
    const target = messages.value.find((message) => message.id === streamMessageId.value)
    if (target) {
      target.content += content
      target.pending = false
    }
  }

  function finishStream() {
    const target = messages.value.find((message) => message.id === streamMessageId.value)
    if (target) target.pending = false
    isStreaming.value = false
    controller = null
  }

  async function sendMessage(prompt = input.value) {
    const message = prompt.trim()
    if (!message || isStreaming.value) return

    input.value = ''
    error.value = ''
    lastPrompt.value = message
    messages.value.push(createMessage('user', message))
    const assistantMessage = createMessage('assistant', '', { pending: true })
    messages.value.push(assistantMessage)
    streamMessageId.value = assistantMessage.id
    isStreaming.value = true
    controller = new AbortController()

    const params = { message }
    if (requiresChatId) params.chatId = chatId.value

    try {
      const response = await fetch(api.getUri({ url: path, params }), {
        method: 'GET',
        headers: { Accept: 'text/event-stream' },
        signal: controller.signal,
      })
      if (!response.ok) throw new Error(`请求失败（${response.status}）`)
      if (!response.body) throw new Error('服务没有返回可读取的流')

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let eventData = []
      let done = false

      while (!done) {
        const result = await reader.read()
        done = result.done
        buffer += decoder.decode(result.value || new Uint8Array(), { stream: !done })
        const lines = buffer.split(/\r?\n/)
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.trim() === '') {
            if (eventData.length) {
              if (consumeEventBlock(eventData.join('\n'), appendChunk)) {
                done = true
                break
              }
              eventData = []
            }
            continue
          }
          eventData.push(line)
        }
      }

      buffer += decoder.decode()
      if (eventData.length) consumeEventBlock(eventData.join('\n'), appendChunk)
      if (buffer.trim()) consumeEventBlock(buffer.trim(), appendChunk)
      finishStream()
    } catch (requestError) {
      const aborted = requestError?.name === 'AbortError'
      const target = messages.value.find((item) => item.id === streamMessageId.value)
      if (aborted) {
        if (target && !target.content) target.content = '已停止生成。'
      } else {
        error.value = requestError?.message || '连接服务失败，请稍后重试。'
        if (target && !target.content) target.content = '这次连接没有完成，请检查服务后重试。'
      }
      if (target) target.pending = false
      isStreaming.value = false
      controller = null
    } finally {
      await nextTick()
    }
  }

  function stopGeneration() {
    controller?.abort()
  }

  function resetConversation() {
    stopGeneration()
    chatId.value = createChatId()
    messages.value = [createMessage('assistant', welcome)]
    input.value = ''
    error.value = ''
    lastPrompt.value = ''
    streamMessageId.value = ''
  }

  function retryLast() {
    if (lastPrompt.value && !isStreaming.value) sendMessage(lastPrompt.value)
  }

  return {
    chatId,
    sessionLabel,
    input,
    messages,
    isStreaming,
    error,
    intro,
    canSend,
    sendMessage,
    stopGeneration,
    resetConversation,
    retryLast,
  }
}
