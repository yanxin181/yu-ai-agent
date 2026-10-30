<template>
  <div class="chat-page" :class="`chat-page--${accent}`">
    <header class="chat-header page-container">
      <button class="icon-button" type="button" aria-label="返回首页" @click="$emit('back')">
        <AppIcon name="arrow-left" />
      </button>
      <div class="chat-heading">
        <div class="eyebrow"><span class="status-dot" /> {{ eyebrow }}</div>
        <h1>{{ title }}</h1>
        <p>{{ subtitle }}</p>
      </div>
      <div class="chat-header-actions">
        <div class="session-pill" title="当前会话 ID">
          <span class="session-pill__label">SESSION</span>
          <span class="session-pill__value">{{ sessionLabel }}</span>
        </div>
        <button class="icon-button icon-button--ghost" type="button" aria-label="开启新会话" @click="chat.resetConversation">
          <AppIcon name="plus" />
        </button>
      </div>
    </header>

    <main class="chat-layout page-container">
      <section ref="messagesPanel" class="messages-panel" aria-label="聊天记录" role="log" aria-live="polite">
        <div class="conversation-note">
          <span class="conversation-note__icon"><AppIcon name="shield" size="sm" /></span>
          <span>你的对话只用于当前会话，建议不要分享敏感信息。</span>
        </div>

        <article
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="`message-row--${message.role}`"
        >
          <div class="message-avatar" :class="`message-avatar--${message.role}`" aria-hidden="true">
            <AppIcon :name="message.role === 'assistant' ? assistantIcon : 'dot'" size="sm" />
          </div>
          <div class="message-column">
            <div class="message-meta">
              <strong>{{ message.role === 'assistant' ? assistantName : '你' }}</strong>
              <time>{{ formatTime(message.createdAt) }}</time>
            </div>
            <div class="message-bubble" :class="{ 'message-bubble--pending': message.pending }">
              <span v-if="message.content" class="message-text">{{ message.content }}</span>
              <span v-else class="typing-indicator" aria-label="正在生成">
                <i /><i /><i />
              </span>
            </div>
          </div>
        </article>

        <div v-if="error" class="error-banner" role="alert">
          <div>
            <strong>连接遇到问题</strong>
            <span>{{ error }}</span>
          </div>
          <button type="button" class="text-button" @click="chat.retryLast">
            <AppIcon name="rotate" size="sm" /> 再试一次
          </button>
        </div>
      </section>

      <aside class="chat-aside">
        <div class="aside-card aside-card--accent">
          <div class="aside-card__icon"><AppIcon :name="assistantIcon" size="md" /></div>
          <div>
            <span class="aside-card__label">{{ assistantName }}</span>
            <p>{{ chat.intro }}</p>
          </div>
        </div>
        <div class="aside-card aside-card--plain">
          <span class="aside-card__label">对话提示</span>
          <ul>
            <li><span class="list-dot" />尽量描述具体的情境</li>
            <li><span class="list-dot" />可以连续追问和补充背景</li>
            <li><span class="list-dot" />按 Enter 发送，Shift + Enter 换行</li>
          </ul>
        </div>
      </aside>
    </main>

    <footer class="composer-wrap">
      <form class="composer page-container" @submit.prevent="chat.sendMessage()">
        <div class="composer-field">
          <label class="sr-only" :for="textareaId">输入消息</label>
          <textarea
            :id="textareaId"
            v-model="input"
            rows="1"
            maxlength="2000"
            :placeholder="placeholder"
            :disabled="isStreaming"
            @keydown.enter.exact.prevent="chat.sendMessage()"
          />
          <div class="composer-footer">
            <span class="composer-hint">{{ input.length }}/2000</span>
            <div class="composer-actions">
              <button v-if="isStreaming" class="stop-button" type="button" @click="chat.stopGeneration">
                <AppIcon name="stop" size="sm" /> 停止生成
              </button>
              <button v-else class="send-button" type="submit" :disabled="!canSend">
                <span>发送消息</span>
                <AppIcon name="send" size="sm" />
              </button>
            </div>
          </div>
        </div>
      </form>
      <p class="composer-disclaimer">AI 生成的内容仅供参考，请结合实际情况判断。</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import AppIcon from './AppIcon.vue'

defineEmits(['back'])
const props = defineProps({
  chat: { type: Object, required: true },
  title: { type: String, required: true },
  subtitle: { type: String, required: true },
  eyebrow: { type: String, default: '在线服务' },
  assistantName: { type: String, required: true },
  assistantIcon: { type: String, default: 'sparkles' },
  accent: { type: String, default: 'violet' },
  placeholder: { type: String, default: '输入你想聊的内容…' },
  textareaId: { type: String, default: 'chat-message' },
})

const messagesPanel = ref(null)
const messages = computed(() => props.chat.messages.value)
const input = computed({
  get: () => props.chat.input.value,
  set: (value) => { props.chat.input.value = value },
})
const isStreaming = computed(() => props.chat.isStreaming.value)
const error = computed(() => props.chat.error.value)
const canSend = computed(() => props.chat.canSend.value)
const sessionLabel = computed(() => props.chat.sessionLabel.value)

function formatTime(value) {
  return new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit' }).format(value)
}

async function scrollToBottom() {
  await nextTick()
  if (messagesPanel.value) messagesPanel.value.scrollTo({ top: messagesPanel.value.scrollHeight, behavior: 'smooth' })
}

watch(() => messages.value.length, scrollToBottom)
watch(() => messages.value.map((message) => message.content).join(''), scrollToBottom)
onMounted(scrollToBottom)
</script>
