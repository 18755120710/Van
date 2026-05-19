<template>
  <main class="main-content">
    <!-- Top Sleek Header -->
    <header class="top-navbar">
      <div class="nav-left">
        <button 
          class="expand-sidebar-btn" 
          v-if="sidebarCollapsed" 
          @click="emit('update:sidebarCollapsed', false)" 
          title="Expand Sidebar"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <line x1="9" y1="3" x2="9" y2="21" />
          </svg>
        </button>
        <div class="session-info">
          <h2 class="session-title">{{ activeConversation?.title || '新对话' }}</h2>
          <p class="session-subtitle">Interactive Web Automation Agent</p>
        </div>
      </div>
      
      <div class="nav-right">
        <!-- Small badge for online presence -->
        <span class="status-badge" :class="{ 'connected': isConnected }">
          <span class="badge-dot"></span>
          {{ isConnected ? 'Online' : 'Offline' }}
        </span>
        <button class="icon-action-btn" @click="handleReconnect" title="Force Reconnect" type="button">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67" />
          </svg>
        </button>
      </div>
    </header>

    <!-- Chat Viewport -->
    <div class="chat-viewport">
      <!-- Scrollable Messages Container -->
      <div class="messages-container" ref="messagesContainer">
        
        <!-- Elegant Welcome Screen for Clean Empty State -->
        <WelcomeGrid 
          v-if="messages.length === 0" 
          @select-prompt="handleSelectPrompt"
        />

        <!-- Dynamic Chat Conversation Stream -->
        <div class="chat-flow-list" v-else>
          <ChatBubble 
            v-for="(message, index) in messages" 
            :key="index" 
            :message="message"
          />
        </div>
      </div>

      <!-- Floating Input Footer Panel -->
      <InputPanel ref="inputPanelRef" />
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import { useStomp } from '~/composables/useStomp'
import WelcomeGrid from '~/components/chat/WelcomeGrid.vue'
import ChatBubble from '~/components/chat/ChatBubble.vue'
import InputPanel from '~/components/chat/InputPanel.vue'

// Props & Emits
const props = defineProps<{
  sidebarCollapsed: boolean
}>()

const emit = defineEmits<{
  (e: 'update:sidebarCollapsed', val: boolean): void
}>()

// Component Refs
const messagesContainer = ref<HTMLElement | null>(null)
const inputPanelRef = ref<InstanceType<typeof InputPanel> | null>(null)

// Load Stomp hooks
const { messages, isConnected, connect, disconnect, activeConversationId, conversations } = useStomp()

const activeConversation = computed(() => {
  return conversations.value.find(c => c.conversationId === activeConversationId.value)
})

// Prefill text into input field
const handleSelectPrompt = (text: string) => {
  inputPanelRef.value?.prefillPrompt(text)
}

// Reconnection controller
const handleReconnect = () => {
  console.log('Force reconnecting STOMP client...')
  disconnect()
  setTimeout(() => {
    connect()
  }, 200)
}

// Auto scroll viewport to bottom
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Watchers for scrolling triggers
watch(() => messages.value.length, scrollToBottom)

watch(
  () => messages.value[messages.value.length - 1]?.text,
  scrollToBottom
)
</script>
