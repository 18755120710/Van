<template>
  <aside
    class="right-panel"
    :class="{ 'open': isRightPanelOpen, 'resizing': isResizingRightPanel }"
    :style="{ '--right-panel-width': `${rightPanelWidth}px` }"
  >
    <div
      v-if="isRightPanelOpen"
      class="right-panel-resize-handle"
      title="拖拽调整面板宽度"
      @mousedown="startResizeRightPanel"
    ></div>

    <div class="right-panel-header">
      <div class="right-panel-title">
        <svg class="tool-icon" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="16 18 22 12 16 6" />
          <polyline points="8 6 2 12 8 18" />
        </svg>
        <span>执行步骤与深度思考</span>
        <span class="right-panel-count" v-if="activeTraceMsg && activeTraceMsg.toolResults">
          ({{ activeTraceMsg.toolResults.length }})
        </span>
      </div>
      <button class="close-right-panel-btn" @click="isRightPanelOpen = false" title="关闭面板" type="button">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
    </div>

    <!-- Segment Switcher Tab -->
    <div class="panel-view-switcher" v-if="activeTraceMsg">
      <button 
        class="view-switch-btn" 
        :class="{ 'active': activeTab === 'sandbox' }"
        @click="activeTab = 'sandbox'"
        type="button"
      >
        🎮 3D 创想沙盒
      </button>
      <button 
        class="view-switch-btn" 
        :class="{ 'active': activeTab === 'timeline' }"
        @click="activeTab = 'timeline'"
        type="button"
      >
        📊 调试日志
      </button>
    </div>

    <!-- VIEW 1: Standard Timeline & Filters -->
    <template v-if="activeTab === 'timeline'">
      <!-- Stats & Capsules Filters Row -->
      <div class="right-panel-filter-row" v-if="activeTraceMsg && activeTraceMsg.toolResults && activeTraceMsg.toolResults.length > 0">
        <!-- Trace Stats Summary -->
        <div class="right-panel-stats">
          <div class="right-panel-stat-item" title="规划事件总数">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 2 7 12 12 22 7 12 2" />
              <polyline points="2 17 12 22 22 17" />
              <polyline points="2 12 12 17 22 12" />
            </svg>
            <span>规划: {{ stats.plans }}</span>
          </div>
          <div class="right-panel-stat-item" title="工具调用次数">
            <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="7" width="20" height="14" rx="2" ry="2" />
              <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16" />
            </svg>
            <span>工具: {{ stats.tools }}</span>
          </div>
          <div class="right-panel-stat-item" title="异常和错误数" v-if="stats.errors > 0">
            <svg style="color: #f87171" xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
            <span style="color: #fca5a5">异常: {{ stats.errors }}</span>
          </div>
        </div>

        <!-- Quick Category Filters -->
        <div class="right-panel-filters">
          <button 
            v-for="filter in filterOptions" 
            :key="filter.value" 
            class="filter-pill" 
            :class="{ 'active': activeFilter === filter.value }"
            @click="activeFilter = filter.value"
            type="button"
          >
            <span>{{ filter.label }}</span>
          </button>
        </div>
      </div>
      
      <!-- Right panel content containing the beautiful logs timeline -->
      <div class="right-panel-content" ref="rightPanelContent" v-if="activeTraceMsg">
        <div class="right-panel-empty" v-if="!filteredToolResults || filteredToolResults.length === 0">
          <div class="empty-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="16" x2="12" y2="12" />
              <line x1="12" y1="8" x2="12.01" y2="8" />
            </svg>
          </div>
          <p>{{ activeFilter !== 'all' ? '当前分类下暂无步骤' : '暂无执行步骤日志' }}</p>
        </div>
        <div class="right-tools-list" v-else>
          <div 
            v-for="(tool, index) in filteredToolResults" 
            :key="index" 
            class="tool-item" 
            :class="[tool.eventType, { 'active-running': isItemRunning(tool.originalIndex) }]"
          >
            <!-- Dashboard Connective Node -->
            <div class="tool-node-container" :title="getEventTypeName(tool.eventType)">
              <span 
                class="tool-node-dot" 
                :class="[tool.eventType, { 'running': isItemRunning(tool.originalIndex) }]"
              >
                <!-- Small specific visual indicator Inside Node -->
                <svg v-if="tool.eventType === 'error'" xmlns="http://www.w3.org/2000/svg" width="6" height="6" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="9" x2="12" y2="13" />
                  <line x1="12" y1="17" x2="12.01" y2="17" />
                </svg>
              </span>
            </div>

            <!-- Top Meta Row -->
            <div class="tool-meta">
              <div class="tool-meta-left">
                <span class="tool-badge" :class="tool.eventType">{{ getEventTypeName(tool.eventType) }}</span>
                <span class="tool-agent" v-if="tool.agentName">
                  <span class="meta-label">Agent:</span> {{ tool.agentName }}
                </span>
                <span class="tool-name-text" v-if="tool.toolName">
                  <span class="meta-label">Tool:</span> {{ tool.toolName }}
                </span>
              </div>
              <div class="tool-meta-right">
                <span class="tool-time">{{ tool.timestamp }}</span>
                <!-- Copy Raw Text Action -->
                <button 
                  v-if="tool.text"
                  class="card-action-btn" 
                  @click="copyText(tool.text, index)" 
                  :title="copiedIndex === index ? '已复制' : '复制内容'"
                  type="button"
                >
                  <svg v-if="copiedIndex === index" style="color: #34d399" xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                  </svg>
                </button>
              </div>
            </div>

            <!-- Log Content Area (Collapsible) -->
            <div 
              v-if="tool.text" 
              class="tool-content-wrapper" 
              :class="{ 'collapsed': !expandedItems[tool.originalIndex] && isLongText(tool.text) }"
            >
              <div class="tool-content">
                <pre class="tool-code"><code>{{ tool.text }}</code></pre>
              </div>
            </div>

            <!-- Chevron Collapse Action Bar -->
            <div class="tool-item-actions" v-if="tool.text && isLongText(tool.text)">
              <button 
                class="card-action-btn" 
                :class="{ 'active': expandedItems[tool.originalIndex] }"
                @click="toggleExpand(tool.originalIndex)" 
                type="button"
              >
                <span>{{ expandedItems[tool.originalIndex] ? '收起详情' : '展开详情' }}</span>
                <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="6 9 12 15 18 9" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- VIEW 2: Minecraft-style 3D Office Sandbox -->
    <template v-else-if="activeTab === 'sandbox'">
      <MinecraftSandbox
        v-if="activeTraceMsg"
        :tool-results="activeTraceMsg.toolResults || []"
      />
    </template>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useStomp } from '~/composables/useStomp'
import MinecraftSandbox from '~/components/sandbox/MinecraftSandbox.vue'

const rightPanelContent = ref<HTMLElement | null>(null)
const copiedIndex = ref<number | null>(null)
const activeFilter = ref<'all' | 'plan' | 'tool' | 'error'>('all')
const rightPanelWidth = ref(520)
const isResizingRightPanel = ref(false)

// Tab status: sandbox (3D RPG Office), timeline (console logging)
const activeTab = ref<'sandbox' | 'timeline'>('sandbox')

// Expanded cards registry
const expandedItems = ref<Record<number, boolean>>({})

const { messages, activeTraceMsgId, isRightPanelOpen } = useStomp()

const clampRightPanelWidth = (value: number) => {
  if (typeof window === 'undefined') return value
  const maxWidth = Math.min(Math.floor(window.innerWidth * 0.62), 920)
  return Math.max(420, Math.min(value, maxWidth))
}

const startResizeRightPanel = (event: MouseEvent) => {
  event.preventDefault()
  isResizingRightPanel.value = true

  const startX = event.clientX
  const startWidth = rightPanelWidth.value

  const onMouseMove = (moveEvent: MouseEvent) => {
    rightPanelWidth.value = clampRightPanelWidth(startWidth + startX - moveEvent.clientX)
  }

  const onMouseUp = () => {
    isResizingRightPanel.value = false
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)
  }

  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

// Computed trace message object for right panel
const activeTraceMsg = computed(() => {
  if (!activeTraceMsgId.value) return null
  return messages.value.find(m => m.traceId === activeTraceMsgId.value) || null
})

// Filter definition list
const filterOptions = [
  { label: '全部步骤', value: 'all' as const },
  { label: '规划路径', value: 'plan' as const },
  { label: '工具痕迹', value: 'tool' as const },
  { label: '异常拦截', value: 'error' as const }
]

// Stats Calculator DTO
const stats = computed(() => {
  const list = activeTraceMsg.value?.toolResults || []
  let plans = 0
  let tools = 0
  let errors = 0
  
  list.forEach(item => {
    if (item.eventType === 'plan') plans++
    if (item.eventType === 'tool_call') tools++
    if (item.eventType === 'error') errors++
  })

  return { plans, tools, errors }
})

// Dynamically filter results based on selected pill
const filteredToolResults = computed(() => {
  const list = activeTraceMsg.value?.toolResults || []
  return list
    .map((item, index) => ({ ...item, originalIndex: index }))
    .filter(item => {
      if (activeFilter.value === 'all') return true
      if (activeFilter.value === 'plan') return item.eventType === 'plan'
      if (activeFilter.value === 'error') return item.eventType === 'error'
      if (activeFilter.value === 'tool') {
        return item.eventType === 'tool_call' || item.eventType === 'tool_result' || item.eventType === 'token_usage'
      }
      return true
    })
})

// Helper to determine if item is currently running in streaming mode
const isItemRunning = (originalIndex: number) => {
  if (!activeTraceMsg.value || !activeTraceMsg.value.streaming) return false
  const list = activeTraceMsg.value.toolResults || []
  return originalIndex === list.length - 1
}

// Check if log block contains massive long lines/content
const isLongText = (text: string) => {
  if (!text) return false
  return text.length > 100 || text.split('\n').length > 2
}

// Collapser action toggle
const toggleExpand = (originalIndex: number) => {
  expandedItems.value[originalIndex] = !expandedItems.value[originalIndex]
}

// Copy clipboard action
const copyText = async (text: string, index: number) => {
  try {
    await navigator.clipboard.writeText(text)
    copiedIndex.value = index
    setTimeout(() => {
      if (copiedIndex.value === index) {
        copiedIndex.value = null
      }
    }, 1500)
  } catch (err) {
    console.error('Failed to copy step trace payload:', err)
  }
}

// Translation helpers for task type labels
const getEventTypeName = (eventType: string) => {
  switch (eventType) {
    case 'plan': return '规划步骤'
    case 'agent_call': return 'Agent 调用'
    case 'tool_call': return '工具调用'
    case 'tool_result': return '工具结果'
    case 'status': return '状态更新'
    case 'error': return '异常错误'
    case 'summary': return '执行摘要'
    case 'answer': return '最终回答'
    case 'stopped': return '执行停止'
    case 'token_usage': return 'Token 消耗'
    default: return eventType || '执行步骤'
  }
}

// Watch toolResults to dynamically recalculate 3D targets in real-time
const toolResultsLength = computed(() => {
  return activeTraceMsg.value?.toolResults?.length || 0
})

watch(toolResultsLength, (newLength) => {
  nextTick(() => {
    // Keep standard timeline scrolled down
    if (rightPanelContent.value) {
      rightPanelContent.value.scrollTop = rightPanelContent.value.scrollHeight
    }
  })
})
</script>
