<template>
  <aside class="sidebar">
    <!-- Sidebar Header -->
    <div class="sidebar-header">
      <div class="brand">
        <svg class="brand-logo" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
        </svg>
        <span class="brand-name">Agent<span class="text-indigo">Scope</span></span>
      </div>
      <button class="collapse-btn" @click="toggleSidebar" title="Toggle Sidebar" type="button">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="11 17 6 12 11 7" />
          <polyline points="18 17 13 12 18 7" />
        </svg>
      </button>
    </div>

    <!-- Live Connection Status Panel -->
    <div class="status-card">
      <div class="status-indicator">
        <span class="status-dot" :class="{ 'connected': isConnected }"></span>
        <span class="status-text">{{ isConnected ? 'Connected to Server' : 'Connecting...' }}</span>
      </div>
      <div class="status-details">
        ws://localhost:18081/bs-dialog-websocket
      </div>
    </div>

    <!-- Sidebar Navigation Options -->
    <nav class="sidebar-nav">
      <!-- 新建对话按钮 -->
      <button class="new-chat-btn" @click="handleNewChat" type="button" title="Create New Conversation">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        <span>新建对话</span>
      </button>
      <div class="nav-section-title">历史会话</div>
      <div class="history-list">
        <button 
          v-for="conv in conversations" 
          :key="conv.conversationId"
          class="history-item"
          :class="{ 'active': activeConversationId === conv.conversationId }"
          @click="handleSwitchConversation(conv.conversationId)"
          type="button"
        >
          <svg class="history-item-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          <div class="history-item-info">
            <span class="history-item-title">{{ conv.title || '新对话' }}</span>
            <span class="history-item-time">{{ formatRelativeTime(conv.updateAt) }}</span>
          </div>
        </button>
      </div>
    </nav>

    <!-- Sidebar User Section -->
    <div class="sidebar-footer">
      <div class="user-profile">
        <div class="user-avatar-placeholder">U</div>
        <div class="user-info">
          <div class="user-name">Developer Console</div>
          <div class="user-role">Administrator</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { useStomp } from '~/composables/useStomp'

// Props & Emits
const emit = defineEmits<{
  (e: 'update:collapsed', val: boolean): void
}>()

const props = defineProps<{
  collapsed: boolean
}>()

const { 
  isConnected, 
  activeConversationId, 
  conversations, 
  createNewConversation, 
  switchConversation 
} = useStomp()

const toggleSidebar = () => {
  emit('update:collapsed', !props.collapsed)
}

const handleNewChat = async () => {
  await createNewConversation()
}

const handleSwitchConversation = async (conversationId: string) => {
  await switchConversation(conversationId)
}

const formatRelativeTime = (timestamp: number) => {
  if (!timestamp) return ''
  const now = Date.now()
  const diffMs = now - timestamp
  
  if (diffMs < 0) return '刚刚'
  
  const diffSec = Math.floor(diffMs / 1000)
  const diffMin = Math.floor(diffSec / 60)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)
  const diffWeek = Math.floor(diffDay / 7)
  const diffMonth = Math.floor(diffDay / 30)
  
  if (diffSec < 60) {
    return '刚刚'
  } else if (diffMin < 60) {
    return `${diffMin}分钟前`
  } else if (diffHour < 24) {
    return `${diffHour}小时前`
  } else if (diffDay < 7) {
    return `${diffDay}天前`
  } else if (diffWeek < 4) {
    return `${diffWeek}周前`
  } else if (diffMonth < 12) {
    return `${diffMonth}个月前`
  } else {
    return new Date(timestamp).toLocaleDateString()
  }
}
</script>
