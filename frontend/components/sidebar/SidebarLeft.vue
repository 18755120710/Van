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
      <div class="nav-section-title">Workspace</div>
      <button class="nav-item active" type="button">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
        <span>Chat Console</span>
      </button>
      
      <button class="nav-item" @click="handleClearHistory" type="button" title="Clear Chat History">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2" />
        </svg>
        <span>Clear Conversation</span>
      </button>

      <div class="nav-section-title">Framework Capabilities</div>
      <div class="capability-list">
        <div class="cap-tag"><span class="cap-dot"></span> Browser-Use Automation</div>
        <div class="cap-tag"><span class="cap-dot"></span> Java Spring Boot Core</div>
        <div class="cap-tag"><span class="cap-dot"></span> WebSocket STOMP Sync</div>
        <div class="cap-tag"><span class="cap-dot"></span> Asynchronous Agent Loop</div>
        <div class="cap-tag"><span class="cap-dot"></span> Rich Media Render</div>
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

const { isConnected, clearMessages } = useStomp()

const toggleSidebar = () => {
  emit('update:collapsed', !props.collapsed)
}

const handleClearHistory = () => {
  if (confirm('确定要清除所有对话记录吗？')) {
    clearMessages()
  }
}
</script>
