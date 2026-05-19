<template>
  <main class="main-content">
    <!-- Top Sleek Header -->
    <header class="top-navbar">
      <div class="nav-left">
        <button 
          class="expand-sidebar-btn" 
          v-if="sidebarCollapsed" 
          @click="emit('update:sidebarCollapsed', false)" 
          title="展开侧边栏"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
            <line x1="9" y1="3" x2="9" y2="21" />
          </svg>
        </button>
        <div class="session-info">
          <h2 class="session-title">Agent 配置与 Prompt 管理</h2>
          <p class="session-subtitle">系统提示词模版管理与运行时调优</p>
        </div>
      </div>
      
      <div class="nav-right">
        <!-- Small badge for online presence -->
        <span class="status-badge" :class="{ 'connected': isConnected }">
          <span class="badge-dot"></span>
          {{ isConnected ? 'Online' : 'Offline' }}
        </span>
      </div>
    </header>

    <!-- Main Manager Area -->
    <div class="manager-body">
      <!-- Left sidebar: prompt template list -->
      <aside class="prompts-sidebar">
        <!-- Search bar -->
        <div class="search-container">
          <div class="search-input-wrapper">
            <svg class="search-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="搜索 Prompt..." 
              class="search-input"
            />
            <button 
              v-if="searchQuery" 
              @click="searchQuery = ''" 
              class="clear-search-btn"
              type="button"
              title="清除输入"
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>

        <!-- Prompt template items list -->
        <div class="prompts-list-wrapper">
          <div v-if="loadingList" class="list-loader-container">
            <div class="spinner"></div>
            <span>加载模板列表中...</span>
          </div>

          <div v-else-if="filteredPrompts.length === 0" class="list-empty-state">
            <svg class="empty-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="9" y1="15" x2="15" y2="15"></line>
            </svg>
            <p>{{ searchQuery ? '未找到匹配的模板' : '暂无可用 Prompt 模板' }}</p>
          </div>

          <div v-else class="prompts-list">
            <button
              v-for="prompt in filteredPrompts"
              :key="prompt.key"
              class="prompt-item"
              :class="{ 'active': activePromptKey === prompt.key }"
              @click="handleSelectPrompt(prompt)"
              type="button"
            >
              <div class="prompt-item-main">
                <div class="prompt-item-title-row">
                  <span class="prompt-item-name">{{ prompt.name }}</span>
                  <span 
                    class="badge" 
                    :class="prompt.source === 'custom' ? 'badge-custom' : 'badge-default'"
                  >
                    {{ prompt.source === 'custom' ? '已自定义' : '默认' }}
                  </span>
                </div>
                <span class="prompt-item-key">{{ prompt.filename }}</span>
              </div>
              <svg class="arrow-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>
          </div>
        </div>
      </aside>

      <!-- Right main area: editor container -->
      <section class="editor-pane">
        <!-- Toast Notification Banner -->
        <Transition name="fade">
          <div v-if="alertMessage" class="alert-banner" :class="alertType">
            <div class="alert-content">
              <svg v-if="alertType === 'success'" class="alert-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              <svg v-else class="alert-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="8" x2="12" y2="12"></line>
                <line x1="12" y1="16" x2="12.01" y2="16"></line>
              </svg>
              <span class="alert-text">{{ alertMessage }}</span>
            </div>
            <button @click="alertMessage = null" class="close-alert-btn" type="button">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </Transition>

        <!-- No Prompt Selected Screen -->
        <div v-if="!activePromptKey" class="editor-placeholder">
          <div class="placeholder-icon-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" class="placeholder-svg">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="21" y1="9" x2="3" y2="9"></line>
              <line x1="9" y1="21" x2="9" y2="9"></line>
              <path d="M14 13h3"></path>
              <path d="M14 17h3"></path>
            </svg>
          </div>
          <h3>选择一个 Prompt 模板</h3>
          <p>从左侧列表中点击选择任一系统提示词模板，即可开始在线预览、修改和进行恢复重置操作。</p>
        </div>

        <!-- Selected Active Editor Screen -->
        <div v-else class="editor-content-area">
          <div class="editor-panel-header">
            <div class="template-details">
              <div class="title-with-source">
                <h3>{{ activePromptName }}</h3>
                <span 
                  class="badge" 
                  :class="activePromptSource === 'custom' ? 'badge-custom' : 'badge-default'"
                >
                  {{ activePromptSource === 'custom' ? '已自定义模板' : '系统默认模板' }}
                </span>
              </div>
              <div class="file-meta-row">
                <span class="meta-label">文件路径:</span>
                <span class="meta-value font-mono">data/agent-config/prompts/{{ activePromptFilename }}</span>
                <template v-if="activePromptUpdatedAt">
                  <span class="meta-separator">•</span>
                  <span class="meta-label">最后更新:</span>
                  <span class="meta-value">{{ formatTime(activePromptUpdatedAt) }}</span>
                </template>
              </div>
            </div>
          </div>

          <!-- Loading / Processing Mask -->
          <div class="editor-wrapper">
            <div v-if="processing" class="editor-overlay">
              <div class="spinner"></div>
              <span>{{ processText }}</span>
            </div>

            <textarea
              v-model="editorContent"
              class="editor-textarea font-mono"
              placeholder="请输入 Prompt 模版内容..."
              :disabled="processing"
            ></textarea>
          </div>

          <!-- Footer controls -->
          <footer class="editor-panel-footer">
            <div class="footer-left">
              <span class="char-counter">字符数: <strong>{{ editorContent.length }}</strong></span>
            </div>
            <div class="footer-actions">
              <button 
                class="btn btn-outline" 
                @click="handleReset"
                :disabled="processing"
                type="button"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M2.5 2v6h6M21.5 22v-6h-6"></path>
                  <path d="M22 11.5A10 10 0 0 0 9.59 3.42l-7.09 7.09M2 12.5a10 10 0 0 0 12.41 8.08l7.09-7.09"></path>
                </svg>
                <span>恢复默认</span>
              </button>
              <button 
                class="btn btn-primary" 
                @click="handleSave"
                :disabled="processing"
                type="button"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                  <polyline points="17 21 17 13 7 13 7 21"></polyline>
                  <polyline points="7 3 7 8 15 8"></polyline>
                </svg>
                <span>保存修改</span>
              </button>
            </div>
          </footer>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useStomp } from '~/composables/useStomp'
import { useAgentConfig } from '~/composables/useAgentConfig'
import type { PromptInfo } from '~/types/agentConfig'

// Props & Emits
defineProps<{
  sidebarCollapsed: boolean
}>()

const emit = defineEmits<{
  (e: 'update:sidebarCollapsed', val: boolean): void
}>()

// Hooks & States
const { isConnected } = useStomp()
const { listPrompts, getPromptContent, savePrompt, resetPrompt } = useAgentConfig()

// Lists & search state
const promptsList = ref<PromptInfo[]>([])
const loadingList = ref(false)
const searchQuery = ref('')

// Selected template details
const activePromptKey = ref<string | null>(null)
const activePromptName = ref('')
const activePromptFilename = ref('')
const activePromptSource = ref<'default' | 'custom'>('default')
const activePromptUpdatedAt = ref<number | null>(null)

// Editor content & lock
const editorContent = ref('')
const processing = ref(false)
const processText = ref('加载模板内容中...')

// Notifications status
const alertMessage = ref<string | null>(null)
const alertType = ref<'success' | 'error'>('success')

// Filtered prompt list
const filteredPrompts = computed(() => {
  if (!searchQuery.value.trim()) {
    return promptsList.value
  }
  const q = searchQuery.value.toLowerCase()
  return promptsList.value.filter(p => 
    p.name.toLowerCase().includes(q) || 
    p.key.toLowerCase().includes(q) || 
    p.filename.toLowerCase().includes(q)
  )
})

// Display alert helper
const triggerAlert = (message: string, type: 'success' | 'error' = 'success') => {
  alertMessage.value = message
  alertType.value = type
  setTimeout(() => {
    if (alertMessage.value === message) {
      alertMessage.value = null
    }
  }, 4000)
}

// Fetch list of templates from server
const fetchPromptsList = async () => {
  loadingList.value = true
  try {
    promptsList.value = await listPrompts()
  } catch (error: any) {
    console.error('Failed to load prompts:', error)
    triggerAlert(error?.message || '获取 Prompt 模板列表失败', 'error')
  } finally {
    loadingList.value = false
  }
}

// Load content for clicked template item
const handleSelectPrompt = async (prompt: PromptInfo) => {
  activePromptKey.value = prompt.key
  activePromptName.value = prompt.name
  activePromptFilename.value = prompt.filename
  activePromptSource.value = prompt.source
  activePromptUpdatedAt.value = prompt.updatedAt || null
  editorContent.value = ''
  
  processing.value = true
  processText.value = '正在加载 Prompt 内容...'
  
  try {
    const data = await getPromptContent(prompt.key)
    editorContent.value = data.content || ''
    activePromptSource.value = data.source
    activePromptUpdatedAt.value = data.updatedAt || null
  } catch (error: any) {
    console.error('Failed to fetch prompt content:', error)
    triggerAlert(error?.message || '读取 Prompt 内容失败', 'error')
  } finally {
    processing.value = false
  }
}

// Save active prompt template
const handleSave = async () => {
  if (!activePromptKey.value) return
  
  processing.value = true
  processText.value = '正在保存修改...'
  
  try {
    const data = await savePrompt(activePromptKey.value, editorContent.value)
    editorContent.value = data.content || ''
    activePromptSource.value = data.source
    activePromptUpdatedAt.value = data.updatedAt || null
    
    // Refresh sidebar list info immediately
    await fetchPromptsList()
    
    triggerAlert('Prompt 保存成功，已生效！', 'success')
  } catch (error: any) {
    console.error('Failed to save prompt:', error)
    triggerAlert(error?.message || '保存 Prompt 模板失败', 'error')
  } finally {
    processing.value = false
  }
}

// Reset active prompt to original default
const handleReset = async () => {
  if (!activePromptKey.value) return
  if (!confirm('确认将当前 Prompt 还原为默认模版吗？未保存的修改将会丢失。')) {
    return
  }

  processing.value = true
  processText.value = '正在恢复默认模版...'

  try {
    const data = await resetPrompt(activePromptKey.value)
    editorContent.value = data.content || ''
    activePromptSource.value = data.source
    activePromptUpdatedAt.value = data.updatedAt || null
    
    // Refresh sidebar list info immediately
    await fetchPromptsList()
    
    triggerAlert('Prompt 恢复默认成功！', 'success')
  } catch (error: any) {
    console.error('Failed to reset prompt:', error)
    triggerAlert(error?.message || '重置 Prompt 模版失败', 'error')
  } finally {
    processing.value = false
  }
}

// Helpers
const formatTime = (ts: number) => {
  return new Date(ts).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// Initial fetch
onMounted(() => {
  fetchPromptsList()
})
</script>

<style scoped>
.manager-body {
  display: flex;
  flex: 1;
  height: calc(100vh - 52px);
  overflow: hidden;
}

/* Left Sidebar list style */
.prompts-sidebar {
  width: 320px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.search-container {
  padding: 1rem 0.85rem;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-secondary);
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  width: 15px;
  height: 15px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 0.5rem 2rem 0.5rem 2.2rem;
  font-size: 0.82rem;
  font-family: var(--font-sans);
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  transition: all 0.15s ease;
}

.search-input:focus {
  border-color: var(--text-muted);
  box-shadow: 0 0 0 2px rgba(9, 9, 11, 0.05);
}

.clear-search-btn {
  position: absolute;
  right: 0.6rem;
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 2px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-search-btn:hover {
  background: var(--bg-active);
  color: var(--text-primary);
}

.clear-search-btn svg {
  width: 14px;
  height: 14px;
}

/* Scroll list */
.prompts-list-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.list-loader-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  gap: 0.75rem;
  color: var(--text-muted);
  font-size: 0.82rem;
}

.list-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 1rem;
  gap: 0.75rem;
  color: var(--text-muted);
  text-align: center;
}

.empty-icon {
  width: 32px;
  height: 32px;
  color: var(--text-disabled);
}

.prompts-list {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.prompt-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.8rem 0.85rem;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  text-align: left;
  transition: all 0.18s cubic-bezier(0.2, 0.8, 0.2, 1);
  width: 100%;
}

.prompt-item:hover {
  background: var(--bg-primary);
  border-color: var(--border-light);
  box-shadow: var(--shadow-sm);
}

.prompt-item.active {
  background: var(--bg-primary);
  border-color: var(--border-medium);
  box-shadow: var(--shadow-md);
}

.prompt-item-main {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  flex: 1;
  min-width: 0;
}

.prompt-item-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.prompt-item-name {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.prompt-item-key {
  font-size: 0.72rem;
  color: var(--text-muted);
  font-family: monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  width: 14px;
  height: 14px;
  color: var(--text-disabled);
  margin-left: 0.5rem;
  flex-shrink: 0;
  transition: transform 0.15s ease;
}

.prompt-item:hover .arrow-icon,
.prompt-item.active .arrow-icon {
  color: var(--text-primary);
  transform: translateX(2px);
}

/* Badges styling */
.badge {
  display: inline-flex;
  align-items: center;
  font-size: 0.65rem;
  font-weight: 600;
  padding: 0.1rem 0.35rem;
  border-radius: 9999px;
  border: 1px solid transparent;
  flex-shrink: 0;
  line-height: 1;
}

.badge-default {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border-color: var(--border-light);
}

.badge-custom {
  background: rgba(16, 185, 129, 0.08);
  color: #059669;
  border-color: rgba(16, 185, 129, 0.2);
}

/* Right Editor Pane styling */
.editor-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
}

/* No selection placeholder screen */
.editor-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
  gap: 1rem;
}

.placeholder-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  margin-bottom: 0.5rem;
}

.placeholder-svg {
  width: 32px;
  height: 32px;
}

.editor-placeholder h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.editor-placeholder p {
  font-size: 0.85rem;
  color: var(--text-muted);
  max-width: 420px;
  line-height: 1.5;
}

/* Loaded active editor screen layout */
.editor-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-panel-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-primary);
}

.template-details {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.title-with-source {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.title-with-source h3 {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.file-meta-row {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.72rem;
  color: var(--text-muted);
  flex-wrap: wrap;
}

.meta-separator {
  color: var(--text-disabled);
}

.meta-value {
  color: var(--text-secondary);
}

.font-mono {
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, Courier, monospace;
}

/* Main Textarea box & overlay */
.editor-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 1rem 1.5rem;
  background: var(--bg-secondary);
}

.editor-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(1px);
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  font-size: 0.85rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.editor-textarea {
  flex: 1;
  width: 100%;
  min-height: 400px;
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  font-size: 0.88rem;
  line-height: 1.6;
  color: var(--text-primary);
  resize: none;
  outline: none;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
}

.editor-textarea:focus {
  border-color: var(--border-medium);
  box-shadow: var(--shadow-md), 0 0 0 2px rgba(9, 9, 11, 0.04);
}

.editor-textarea:disabled {
  background: var(--bg-tertiary);
  color: var(--text-muted);
}

/* Footer panel */
.editor-panel-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-light);
  background: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.char-counter {
  font-size: 0.78rem;
  color: var(--text-muted);
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

/* Toast alert styling */
.alert-banner {
  position: absolute;
  top: 1rem;
  left: 1.5rem;
  right: 1.5rem;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.65rem 1rem;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  box-shadow: var(--shadow-md);
}

.alert-banner.success {
  background-color: #ecfdf5;
  border-color: #a7f3d0;
  color: #065f46;
}

.alert-banner.error {
  background-color: #fff5f5;
  border-color: #feb2b2;
  color: #9b2c2c;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.alert-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.alert-text {
  font-size: 0.82rem;
  font-weight: 550;
}

.close-alert-btn {
  background: transparent;
  border: none;
  color: currentColor;
  cursor: pointer;
  padding: 2px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
  transition: opacity 0.15s ease;
}

.close-alert-btn:hover {
  opacity: 1;
}

.close-alert-btn svg {
  width: 14px;
  height: 14px;
}

/* Transition styles */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* Spinner styling */
.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-medium);
  border-top-color: var(--text-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Buttons general styling */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  padding: 0.5rem 1rem;
  font-size: 0.82rem;
  font-family: var(--font-sans);
  font-weight: 550;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.15s ease;
  border: 1px solid transparent;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn svg {
  width: 14px;
  height: 14px;
}

.btn-primary {
  background: var(--text-primary);
  color: #ffffff;
}

.btn-primary:hover:not(:disabled) {
  background: #27272a;
}

.btn-outline {
  background: transparent;
  border-color: var(--border-light);
  color: var(--text-secondary);
}

.btn-outline:hover:not(:disabled) {
  background: var(--bg-active);
  color: var(--text-primary);
}
</style>
