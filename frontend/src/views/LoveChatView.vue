<template>
  <ChatShell
    :chat="chat"
    title="AI 恋爱大师"
    subtitle="温柔地拆解关系，也认真地听你说。"
    eyebrow="LOVE APP · 在线陪伴"
    assistant-name="恋爱大师"
    assistant-icon="heart"
    accent="rose"
    placeholder="告诉我最近让你困惑的关系…"
    textarea-id="love-message"
    @back="goHome"
  />
</template>

<script setup>
import { useRouter } from 'vue-router'
import ChatShell from '../components/ChatShell.vue'
import { useSseChat } from '../composables/useSseChat'

const router = useRouter()
const chat = useSseChat({
  path: '/ai/love_app/chat/sse',
  requiresChatId: true,
  welcome: '你好，我是你的恋爱大师。无论是暧昧期的进退、争吵后的沟通，还是你说不清的情绪，都可以从这里开始。',
  intro: '擅长理解关系中的情绪和分寸，给你具体、可执行的沟通建议。',
})

function goHome() {
  chat.stopGeneration()
  router.push('/')
}
</script>
