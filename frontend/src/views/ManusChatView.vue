<template>
  <ChatShell
    :chat="chat"
    title="AI 超级智能体"
    subtitle="从一个想法出发，陪你走完完整的执行路径。"
    eyebrow="MANUS · 在线协作"
    assistant-name="超级智能体"
    assistant-icon="brain"
    accent="blue"
    placeholder="描述你想完成的目标或任务…"
    textarea-id="manus-message"
    @back="goHome"
  />
</template>

<script setup>
import { useRouter } from 'vue-router'
import ChatShell from '../components/ChatShell.vue'
import { useSseChat } from '../composables/useSseChat'

const router = useRouter()
const chat = useSseChat({
  path: '/ai/manus/chat',
  welcome: '你好，我是超级智能体。告诉我你想完成什么，我会先理解目标，再帮你拆解并推进每一步。',
  intro: '可以搜索资料、分析信息、拆解任务，并把复杂目标变成清晰的行动路径。',
})

function goHome() {
  chat.stopGeneration()
  router.push('/')
}
</script>
