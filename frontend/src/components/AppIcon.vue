<template>
  <svg
    class="app-icon"
    :class="`app-icon--${size}`"
    :width="iconSize"
    :height="iconSize"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.8"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path v-if="name === 'arrow-right'" d="M5 12h13M13 6l6 6-6 6" />
    <path v-else-if="name === 'arrow-left'" d="M19 12H6m5-6-6 6 6 6" />
    <path v-else-if="name === 'send'" d="m22 2-7 20-4-9-9-4Z M22 2 11 13" />
    <path v-else-if="name === 'stop'" d="M7 7h10v10H7z" />
    <path v-else-if="name === 'rotate'" d="M20 11a8 8 0 1 0 2 5.3M20 5v6h-6" />
    <path v-else-if="name === 'plus'" d="M12 5v14M5 12h14" />
    <path v-else-if="name === 'chevron-down'" d="m6 9 6 6 6-6" />
    <path v-else-if="name === 'heart'" d="M20.8 8.8c0 5.1-8.8 10.2-8.8 10.2S3.2 13.9 3.2 8.8A4.5 4.5 0 0 1 12 6.5a4.5 4.5 0 0 1 8.8 2.3Z" />
    <path v-else-if="name === 'brain'" d="M9.5 4.2A3.2 3.2 0 0 0 6 7.3 3.1 3.1 0 0 0 3.8 12 3.3 3.3 0 0 0 7 16.4h.4A3.2 3.2 0 0 0 12 19.5V5.1a3.2 3.2 0 0 0-2.5-.9ZM14.5 4.2A3.2 3.2 0 0 1 18 7.3a3.1 3.1 0 0 1 2.2 4.7 3.3 3.3 0 0 1-3.2 4.4h-.4a3.2 3.2 0 0 1-4.6 3.1V5.1a3.2 3.2 0 0 1 2.5-.9ZM7 9h2m-2 4h2m6-4h2m-2 4h2" />
    <path v-else-if="name === 'sparkles'" d="m12 3-1.2 4.8L6 9l4.8 1.2L12 15l1.2-4.8L18 9l-4.8-1.2ZM19 14l-.7 2.3L16 17l2.3.7L19 20l.7-2.3L22 17l-2.3-.7ZM5 15l-.6 1.9L2.5 17.5l1.9.6L5 20l.6-1.9 1.9-.6-1.9-.6Z" />
    <path v-else-if="name === 'message'" d="M20 11.5a7.5 7.5 0 0 1-8 7.5 8.7 8.7 0 0 1-3-.5L4 20l1.5-3A7 7 0 0 1 4 11.5 7.5 7.5 0 0 1 12 4a7.5 7.5 0 0 1 8 7.5Z" />
    <path v-else-if="name === 'shield'" d="M12 3 20 6v5.2c0 4.7-3.4 8.4-8 9.8-4.6-1.4-8-5.1-8-9.8V6l8-3Z M8.5 12l2.2 2.2 4.8-4.8" />
    <path v-else-if="name === 'check'" d="m5 12 4.2 4.2L19 6.5" />
    <circle v-else-if="name === 'dot'" cx="12" cy="12" r="3" fill="currentColor" stroke="none" />
    <path v-else d="M12 3v18M3 12h18" />
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: String, default: 'md' },
})

const iconSize = computed(() => ({ sm: 16, md: 20, lg: 24, xl: 32 }[props.size] || 20))
</script>
