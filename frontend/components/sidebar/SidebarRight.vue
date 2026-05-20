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

    <!-- Segment Switcher Tab -->
    <div class="panel-view-switcher" v-if="activeTraceMsg">
      <button 
        class="view-switch-btn" 
        :class="{ 'active': activeTab === 'sandbox' }"
        @click="activeTab = 'sandbox'"
        type="button"
      >
        🎮 创想沙盒
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

    <!-- VIEW 2: AI RPG Game Sandbox Round Table -->
    <template v-else-if="activeTab === 'sandbox'">
      <div class="sandbox-board" v-if="activeTraceMsg">
        <!-- Interactive 3D/2.5D Conference Room Container -->
        <div class="cyber-meeting-room">
          
          <!-- Glowing holographic cyber board table -->
          <div class="cyber-table-container">
            <div class="cyber-table">
              <div class="cyber-table-radar"></div>
            </div>
          </div>

          <!-- Flying Glowing Data Bullet Package -->
          <div 
            v-if="flyingPacket" 
            class="glowing-packet" 
            :class="flyingPacket.type"
          ></div>

          <!-- NODE 1: Human / Developer (North) -->
          <div class="sandbox-character-node node-developer">
            <!-- Speaking Dialogue bubble box -->
            <div class="sandbox-bubble" v-if="sandboxState.activeSpeaker === 'developer'">
              {{ sandboxState.text }}
            </div>
            <!-- Chibi Character Card -->
            <div 
              class="character-card" 
              :class="{ 'dev-speaking': sandboxState.devState === 'speaking' }"
              title="人类主宰开发者"
            >
              <div class="character-avatar-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <span class="character-name">Developer</span>
              <span class="character-status-hint">
                {{ sandboxState.activeSpeaker === 'developer' ? '📢 发派指令' : '人类造物主' }}
              </span>
              <div class="character-pedestal"></div>
            </div>
          </div>

          <!-- NODE 2: Planner Agent (South-West) -->
          <div class="sandbox-character-node node-planner">
            <!-- Speaking Dialogue bubble box -->
            <div class="sandbox-bubble" v-if="sandboxState.activeSpeaker === 'planner'">
              {{ sandboxState.text }}
            </div>
            <!-- Chibi Character Card -->
            <div 
              class="character-card" 
              :class="{ 'planner-speaking': sandboxState.plannerState === 'speaking' }"
              title="决策领航者 (PlannerAgent)"
            >
              <div class="character-avatar-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                </svg>
              </div>
              <span class="character-name">Planner</span>
              <span class="character-status-hint">
                {{ sandboxState.activeSpeaker === 'planner' ? '💡 思考决策' : '策略决策中心' }}
              </span>
              <div class="character-pedestal"></div>
            </div>
          </div>

          <!-- NODE 3: Browser Agent (South-East) -->
          <div class="sandbox-character-node node-browser">
            <!-- Speaking Dialogue bubble box -->
            <div class="sandbox-bubble" v-if="sandboxState.activeSpeaker === 'browser'">
              {{ sandboxState.text }}
            </div>
            <!-- Chibi Character Card -->
            <div 
              class="character-card" 
              :class="{ 
                'browser-speaking': sandboxState.browserState === 'speaking', 
                'browser-working': sandboxState.browserState === 'working' 
              }"
              title="无畏探索者 (BrowserAgent)"
            >
              <div 
                class="character-avatar-wrapper"
                :class="{ 'spinning-work': sandboxState.browserState === 'working' }"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9" />
                </svg>
              </div>
              <span class="character-name">Browser</span>
              <span class="character-status-hint">
                {{ sandboxState.browserState === 'working' ? '⚙️ 操作浏览器' : (sandboxState.activeSpeaker === 'browser' ? '📢 状态上报' : '无畏执行官') }}
              </span>
              <div class="character-pedestal"></div>
            </div>
          </div>

        </div>

        <!-- Narrative Retro RPG dialogue bottom screen -->
        <div class="rpg-dialogue-console">
          <span>&gt; </span>
          <span>{{ sandboxState.storyText }}</span>
          <span class="rpg-dialogue-cursor">_</span>
        </div>
      </div>
    </template>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useStomp } from '~/composables/useStomp'

const rightPanelContent = ref<HTMLElement | null>(null)
const copiedIndex = ref<number | null>(null)
const activeFilter = ref<'all' | 'plan' | 'tool' | 'error'>('all')

// Tabs: timeline (debug logs list), sandbox (RPG conference board game)
const activeTab = ref<'sandbox' | 'timeline'>('sandbox')

// Flying energy particle flows DTO
const flyingPacket = ref<{ type: string } | null>(null)

// Expanded cards registry
const expandedItems = ref<Record<number, boolean>>({})

const { messages, activeTraceMsgId, isRightPanelOpen } = useStomp()

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
        return item.eventType === 'tool_call' || item.eventType === 'tool_result'
      }
      return true
    })
})

// Visual Sandbox Intelligent RPG State Interpreter
const sandboxState = computed(() => {
  const list = activeTraceMsg.value?.toolResults || []
  if (list.length === 0) {
    return {
      activeSpeaker: 'developer',
      text: '正在等待开发者下达探索任务...',
      storyText: '等待人类主宰下达初始指令，开启全新的 Agent 创想与行动协作大厅。',
      plannerState: 'idle',
      browserState: 'idle',
      devState: 'speaking'
    }
  }

  // Look at the last step event
  const lastItem = list[list.length - 1]
  const type = lastItem.eventType
  const text = lastItem.text || ''
  const toolName = lastItem.toolName || ''

  let activeSpeaker: 'developer' | 'planner' | 'browser' | 'system' = 'planner'
  let plannerState: 'idle' | 'speaking' = 'idle'
  let browserState: 'idle' | 'speaking' | 'working' = 'idle'
  let devState: 'idle' | 'speaking' = 'idle'

  let speechText = text
  let storyText = ''

  if (type === 'stopped') {
    return {
      activeSpeaker: 'system',
      text: '指令中断。已成功强制停止当前的协同执行。',
      storyText: '⚠️ 系统警报：人类主宰发出了 [STOP] 强制阻断指令。所有 Agent 现已立即中断执行并回归待机。',
      plannerState: 'idle',
      browserState: 'idle',
      devState: 'idle'
    }
  }

  // 1. Planner Plan state
  if (type === 'plan') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    speechText = text || '正在分析开发者指令，开始规划多步拆解动作...'
    storyText = `💡 决策领航者 PlannerAgent 闭目沉思，正在对全局任务进行矩阵拆解，制定执行计划。`
  } 
  // 2. Planner Calls Browser
  else if (type === 'agent_call') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    browserState = 'working'
    speechText = text || `向 BrowserAgent 发起战术连线，传输子动作指令包！`
    storyText = `📡 PlannerAgent 完成决策调度，向 BrowserAgent 发送高优先级执行数据包包，请 BrowserAgent 立即着手动作。`
  } 
  // 3. Browser Executes Tool
  else if (type === 'tool_call') {
    activeSpeaker = 'browser'
    browserState = 'working'
    speechText = `指令接收完毕！正启用工具 [${toolName}] 进行物理侧操作...`
    storyText = `⚙️ 收到Planner调度指令！BrowserAgent 激活探索者护目镜，正熟练操作底层网络硬件工具: [${toolName}]...`
  } 
  // 4. Browser Reports Result
  else if (type === 'tool_result') {
    activeSpeaker = 'browser'
    browserState = 'speaking'
    speechText = text ? `工具操作成功！结果上报：${text.substring(0, 75)}...` : `工具操作成功，回执结果已准备好汇报！`
    storyText = `✅ BrowserAgent 顺利采集到网页反馈，已将高精工具 [${toolName}] 的物理侧输出结果封包汇报给 PlannerAgent。`
  } 
  // 5. System Interception / Error
  else if (type === 'error') {
    activeSpeaker = 'system'
    speechText = `执行错误：${text}`
    storyText = `💥 警告：协同链路在执行工具 [${toolName || '未知'}] 时发生系统异常！详情：${text}`
  } 
  // 6. Summary compilation
  else if (type === 'summary') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    speechText = text || '正在分析整理各步骤的输出反馈，产出最终决策摘要...'
    storyText = `📝 PlannerAgent 梳理各方线索，开始融合最终的数据逻辑并编写任务总结汇报。`
  } 
  // 7. Final Response (Deliver answer)
  else if (type === 'answer') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    devState = 'speaking'
    speechText = '任务完美执行！这是向造物主提交的最终报告。'
    storyText = `🏆 任务圆满通关！PlannerAgent 将最终策略报告双手呈递给人类造物主，等待全新指令。`
  }

  return {
    activeSpeaker,
    text: speechText,
    storyText,
    plannerState,
    browserState,
    devState
  }
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
    default: return eventType || '执行步骤'
  }
}

// Watch toolResults length to auto-scroll logs list & auto-expand new steps
const toolResultsLength = computed(() => {
  return activeTraceMsg.value?.toolResults?.length || 0
})

watch(toolResultsLength, (newLength, oldLength) => {
  nextTick(() => {
    // 1. By default, automatically expand newly arrived execution trace nodes
    if (newLength > 0) {
      expandedItems.value[newLength - 1] = true
    }
    
    // 2. Trigger glowing data flying particles DTO inside sandbox
    if (newLength > oldLength && newLength > 0) {
      const list = activeTraceMsg.value?.toolResults || []
      const lastItem = list[list.length - 1]
      const type = lastItem.eventType
      
      let packetType = ''
      if (type === 'plan') {
        packetType = 'dev-to-planner'
      } else if (type === 'agent_call') {
        packetType = 'planner-to-browser'
      } else if (type === 'tool_result') {
        packetType = 'browser-to-planner'
      } else if (type === 'answer') {
        packetType = 'planner-to-dev'
      }
      
      if (packetType) {
        flyingPacket.value = { type: packetType }
        setTimeout(() => {
          flyingPacket.value = null
        }, 800) // Match 0.8s CSS flight keyframe animation
      }
    }
    
    // 3. Keep standard timeline scrolled down
    if (rightPanelContent.value) {
      rightPanelContent.value.scrollTop = rightPanelContent.value.scrollHeight
    }
  })
})
</script>
