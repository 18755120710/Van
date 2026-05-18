<template>
  <aside class="right-panel" :class="{ 'open': isRightPanelOpen }">
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
    
    <!-- Right panel content containing the beautiful logs list -->
    <div class="right-panel-content" ref="rightPanelContent" v-if="activeTraceMsg">
      <div class="right-panel-empty" v-if="!activeTraceMsg.toolResults || activeTraceMsg.toolResults.length === 0">
        <div class="empty-icon">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
        </div>
        <p>暂无执行步骤日志</p>
      </div>
      <div class="right-tools-list" v-else>
        <div v-for="(tool, tIdx) in activeTraceMsg.toolResults" :key="tIdx" class="tool-item" :class="tool.eventType">
          <div class="tool-meta">
            <span class="tool-dot" :class="tool.eventType"></span>
            <span class="tool-badge" :class="tool.eventType">{{ getEventTypeName(tool.eventType) }}</span>
            <span class="tool-agent" v-if="tool.agentName">
              <span class="meta-label">Agent:</span> {{ tool.agentName }}
            </span>
            <span class="tool-name-text" v-if="tool.toolName">
              <span class="meta-label">Tool:</span> {{ tool.toolName }}
            </span>
            <span class="tool-time">{{ tool.timestamp }}</span>
          </div>
          <div class="tool-content" v-if="tool.text">
            <pre class="tool-code"><code>{{ tool.text }}</code></pre>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useStomp } from '~/composables/useStomp'

const rightPanelContent = ref<HTMLElement | null>(null)

const { messages, activeTraceMsgId, isRightPanelOpen } = useStomp()

// Computed trace message object for right panel
const activeTraceMsg = computed(() => {
  if (!activeTraceMsgId.value) return null
  return messages.value.find(m => m.traceId === activeTraceMsgId.value) || null
})

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
    default: return eventType || '执行步骤'
  }
}

// Watch toolResults length to auto-scroll logs list
const toolResultsLength = computed(() => {
  return activeTraceMsg.value?.toolResults?.length || 0
})

watch(toolResultsLength, () => {
  nextTick(() => {
    if (rightPanelContent.value) {
      rightPanelContent.value.scrollTop = rightPanelContent.value.scrollHeight
    }
  })
})
</script>
