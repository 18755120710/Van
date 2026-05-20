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

    <!-- VIEW 2: Three.js 3D Office Sandbox -->
    <template v-else-if="activeTab === 'sandbox'">
      <div class="sandbox-board" v-if="activeTraceMsg">
        
        <!-- Three.js 3D Viewport container -->
        <div ref="canvasContainer" class="three-canvas-container">
          <!-- Control Hint label -->
          <div class="three-hud-controls">🖱️ 拖拽以旋转视角</div>

          <!-- Dynamic Speech bubble overlays projected from 3D coordinates -->
          <div class="three-bubble-container">
            <!-- Developer bubble -->
            <div 
              class="three-bubble" 
              v-if="sandboxState.activeSpeaker === 'developer' && devBubblePos.x > 0"
              :style="{ left: `${devBubblePos.x}px`, top: `${devBubblePos.y}px` }"
            >
              {{ sandboxState.text }}
            </div>

            <!-- Planner bubble -->
            <div 
              class="three-bubble" 
              v-if="sandboxState.activeSpeaker === 'planner' && plannerBubblePos.x > 0"
              :style="{ left: `${plannerBubblePos.x}px`, top: `${plannerBubblePos.y}px` }"
            >
              {{ sandboxState.text }}
            </div>

            <!-- Browser bubble -->
            <div 
              class="three-bubble" 
              v-if="sandboxState.activeSpeaker === 'browser' && browserBubblePos.x > 0"
              :style="{ left: `${browserBubblePos.x}px`, top: `${browserBubblePos.y}px` }"
            >
              {{ sandboxState.text }}
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
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useStomp } from '~/composables/useStomp'
import * as THREE from 'three'

const rightPanelContent = ref<HTMLElement | null>(null)
const canvasContainer = ref<HTMLElement | null>(null)
const copiedIndex = ref<number | null>(null)
const activeFilter = ref<'all' | 'plan' | 'tool' | 'error'>('all')

// Tab status: sandbox (3D RPG Office), timeline (console logging)
const activeTab = ref<'sandbox' | 'timeline'>('sandbox')

// Speech bubble coordinate projectors
const devBubblePos = ref({ x: 0, y: 0 })
const plannerBubblePos = ref({ x: 0, y: 0 })
const browserBubblePos = ref({ x: 0, y: 0 })

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
      storyText: '低功耗待机中。等待人类主宰下达初始指令，开启 3D 智能协同大厅工作舱。',
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
      storyText: '⚠️ 系统警报：人类造物主发出了 [STOP] 强制阻断指令。所有 Agent 现已安全回归各自工位待命。',
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
    storyText = `💡 决策领航者 PlannerAgent 回归工位，开始思考全局任务并制定执行规划方案。`
  } 
  // 2. Planner Calls Browser
  else if (type === 'agent_call') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    browserState = 'working'
    speechText = text || `向 BrowserAgent 发起战术连线，传输子动作指令包！`
    storyText = `📡 PlannerAgent 站起身，真实地步行前往 BrowserAgent 的工位，向其面交并详细分派子探索指令。`
  } 
  // 3. Browser Executes Tool
  else if (type === 'tool_call') {
    activeSpeaker = 'browser'
    browserState = 'working'
    speechText = `指令接收完毕！正启用工具 [${toolName}] 进行物理侧操作...`
    storyText = `⚙️ 收到Planner面交指令！BrowserAgent 坐回电脑前，屏幕发出橙光，正敲击键盘调用网页工具: [${toolName}]...`
  } 
  // 4. Browser Reports Result
  else if (type === 'tool_result') {
    activeSpeaker = 'browser'
    browserState = 'speaking'
    speechText = text ? `工具操作成功！结果上报：${text.substring(0, 75)}...` : `工具操作成功，回执结果已准备好汇报！`
    storyText = `✅ BrowserAgent 顺利采集完毕。它离开工位，走过办公室，来到 PlannerAgent 桌旁详细上报工具数据结果。`
  } 
  // 5. System Interception / Error
  else if (type === 'error') {
    activeSpeaker = 'system'
    speechText = `执行错误：${text}`
    storyText = `💥 警告：协同大厅发生设备故障！调用物理侧 [${toolName || '未知'}] 时发生系统异常！详情：${text}`
  } 
  // 6. Summary compilation
  else if (type === 'summary') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    speechText = text || '正在分析整理各步骤的输出反馈，产出最终决策摘要...'
    storyText = `📝 PlannerAgent 整理探索回执，开始在其主控工作电脑前融汇数据逻辑并撰写执行摘要报告。`
  } 
  // 7. Final Response (Deliver answer)
  else if (type === 'answer') {
    activeSpeaker = 'planner'
    plannerState = 'speaking'
    devState = 'speaking'
    speechText = '任务完美执行！这是向造物主提交的最终报告。'
    storyText = `🏆 任务圆满通关！PlannerAgent 离开自己座位，走近 Developer 工作台，将最终策略报告双手呈递给人类。`
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

// ==========================================
// THREE.JS 3D ENGINE CORE LOGIC (CLIENT SIDE)
// ==========================================

let scene: THREE.Scene | null = null
let camera: THREE.PerspectiveCamera | null = null
let renderer: THREE.WebGLRenderer | null = null
let animFrameId: number | null = null

// Mesh refs
let devMesh: THREE.Group | null = null
let plannerMesh: THREE.Group | null = null
let browserMesh: THREE.Group | null = null

// Glowing computer screen materials (to update brightness when active)
let devScreenMat: THREE.MeshEmissiveMaterial | any = null
let plannerScreenMat: THREE.MeshEmissiveMaterial | any = null
let browserScreenMat: THREE.MeshEmissiveMaterial | any = null

// 3D Target vectors
const devTarget = new THREE.Vector3()
const plannerTarget = new THREE.Vector3()
const browserTarget = new THREE.Vector3()

// Preset Static Positions
const CHAIR_HEIGHT = 0.38
const STAND_HEIGHT = 0.38

// Workstations (Chairs)
const devChairPos = new THREE.Vector3(0, CHAIR_HEIGHT, -2.6)
const plannerChairPos = new THREE.Vector3(-2.2, CHAIR_HEIGHT, 1.2)
const browserChairPos = new THREE.Vector3(2.2, CHAIR_HEIGHT, 1.2)

// Standing Walk destinations
const devDeskPos = new THREE.Vector3(0, STAND_HEIGHT, -1.5) // Planner stands next to developer desk
const browserDeskPos = new THREE.Vector3(1.2, STAND_HEIGHT, 1.2) // Planner stands next to browser desk
const plannerDeskPos = new THREE.Vector3(-1.2, STAND_HEIGHT, 1.2) // Browser stands next to planner desk

// Camera Orbit Dragging variables
let theta = 45 * Math.PI / 180
let phi = 30 * Math.PI / 180
const radius = 8.5
let isDraggingCamera = false
let previousMousePosition = { x: 0, y: 0 }

const initThreeScene = () => {
  if (!canvasContainer.value || !process.client) return

  const width = canvasContainer.value.clientWidth
  const height = canvasContainer.value.clientHeight

  // 1. Create Scene
  scene = new THREE.Scene()
  scene.background = null // Transparent/CSS gradient fallback

  // 2. Create Camera
  camera = new THREE.PerspectiveCamera(40, width / height, 0.1, 100)
  updateCameraPosition()

  // 3. Create WebGL Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  canvasContainer.value.appendChild(renderer.domElement)

  // 4. Lights
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  const sunLight = new THREE.DirectionalLight(0xffffff, 0.8)
  sunLight.position.set(5, 10, 3)
  scene.add(sunLight)

  // Soft purple glowing neon light in center of office floor
  const pointLight = new THREE.PointLight(0x6366f1, 1.2, 10)
  pointLight.position.set(0, 1.5, 0)
  scene.add(pointLight)

  // 5. Digital Grid Floor (Holographic office island)
  const floorGeo = new THREE.BoxGeometry(6.6, 0.15, 6.6)
  const floorMat = new THREE.MeshStandardMaterial({
    color: 0x111118,
    roughness: 0.8,
    metalness: 0.2
  })
  const floor = new THREE.Mesh(floorGeo, floorMat)
  floor.position.y = -0.075
  scene.add(floor)

  const gridHelper = new THREE.GridHelper(6.6, 12, 0x3b82f6, 0x27272a)
  gridHelper.position.y = 0.01
  scene.add(gridHelper)

  // 6. Build Desks, Chairs, and Computers
  // A. Developer desk
  buildDesk(0, 0, -2.0, 0x1f1f2e, 0xa855f7)
  // B. Planner desk
  buildDesk(-2.2, 0, 0.5, 0x1c2135, 0x3b82f6)
  // C. Browser desk
  buildDesk(2.2, 0, 0.5, 0x221c15, 0xf59e0b)

  // Decorative Chibi plants
  buildCutePlant(-3.0, 0, -3.0)
  buildCutePlant(3.0, 0, -3.0)

  // 7. Initialize Character meshes
  devMesh = buildChibiCharacter(0xa855f7, 0xd8b4fe) // Developer (Purple)
  plannerMesh = buildChibiCharacter(0x3b82f6, 0x93c5fd) // Planner (Blue)
  browserMesh = buildChibiCharacter(0xf59e0b, 0xfde047) // Browser (Orange)

  scene.add(devMesh)
  scene.add(plannerMesh)
  scene.add(browserMesh)

  // Snap characters initially sitting at their desks
  devMesh.position.copy(devChairPos)
  plannerMesh.position.copy(plannerChairPos)
  browserMesh.position.copy(browserChairPos)

  devTarget.copy(devChairPos)
  plannerTarget.copy(plannerChairPos)
  browserTarget.copy(browserChairPos)

  // 8. Attach Mouse Orbit Controls Event Listeners
  const canvas = renderer.domElement
  canvas.addEventListener('mousedown', onMouseDown)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)

  // Handle Resize
  window.addEventListener('resize', handleResize)

  // Run render loop
  animate()
}

// Procedural mahogany desk builder with computers
const buildDesk = (x: number, y: number, z: number, woodColor: number, neonColor: number) => {
  if (!scene) return

  // Desk top wood
  const deskGeo = new THREE.BoxGeometry(1.4, 0.08, 0.8)
  const deskMat = new THREE.MeshStandardMaterial({ color: woodColor, roughness: 0.6 })
  const desk = new THREE.Mesh(deskGeo, deskMat)
  desk.position.set(x, 0.6, z)
  scene.add(desk)

  // Legs (two boxes at sides)
  const legMat = new THREE.MeshStandardMaterial({ color: 0x1e293b, roughness: 0.7 })
  const leg1 = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.6, 0.7), legMat)
  leg1.position.set(x - 0.6, 0.3, z)
  scene.add(leg1)

  const leg2 = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.6, 0.7), legMat)
  leg2.position.set(x + 0.6, 0.3, z)
  scene.add(leg2)

  // Laptop body
  const laptopGeo = new THREE.BoxGeometry(0.35, 0.02, 0.25)
  const laptop = new THREE.Mesh(laptopGeo, legMat)
  laptop.position.set(x, 0.65, z)
  scene.add(laptop)

  // Laptop glowing screen (Emissive)
  const screenGeo = new THREE.BoxGeometry(0.35, 0.22, 0.02)
  const screenMat = new THREE.MeshStandardMaterial({
    color: 0x09090b,
    emissive: neonColor,
    emissiveIntensity: 0.8,
    roughness: 0.1
  })
  const screen = new THREE.Mesh(screenGeo, screenMat)
  screen.position.set(x, 0.77, z - 0.1)
  screen.rotation.x = -10 * Math.PI / 180 // slight tilt back
  scene.add(screen)

  // Save computer screen references to fluctuate intensities
  if (neonColor === 0xa855f7) devScreenMat = screenMat
  if (neonColor === 0x3b82f6) plannerScreenMat = screenMat
  if (neonColor === 0xf59e0b) browserScreenMat = screenMat

  // Small office chair
  const seatGeo = new THREE.BoxGeometry(0.44, 0.08, 0.44)
  const seat = new THREE.Mesh(seatGeo, legMat)
  seat.position.set(x, 0.34, z + 0.6)
  scene.add(seat)

  const backGeo = new THREE.BoxGeometry(0.44, 0.35, 0.06)
  const back = new THREE.Mesh(backGeo, legMat)
  back.position.set(x, 0.52, z + 0.8)
  scene.add(back)
}

// Cute procedurally generated 3D plant pot
const buildCutePlant = (x: number, y: number, z: number) => {
  if (!scene) return
  // Brown pot
  const potGeo = new THREE.CylinderGeometry(0.2, 0.15, 0.35, 8)
  const potMat = new THREE.MeshStandardMaterial({ color: 0x78350f })
  const pot = new THREE.Mesh(potGeo, potMat)
  pot.position.set(x, 0.175, z)
  scene.add(pot)

  // Green spherical leaves
  const leafMat = new THREE.MeshStandardMaterial({ color: 0x10b981, roughness: 0.9 })
  const leaf1 = new THREE.Mesh(new THREE.SphereGeometry(0.24, 8, 8), leafMat)
  leaf1.position.set(x, 0.4, z)
  scene.add(leaf1)

  const leaf2 = new THREE.Mesh(new THREE.SphereGeometry(0.18, 8, 8), leafMat)
  leaf2.position.set(x - 0.1, 0.5, z + 0.05)
  scene.add(leaf2)
}

// Procedural Chibi character builder
const buildChibiCharacter = (color: number, brightColor: number) => {
  const group = new THREE.Group()

  // 1. Cylinder Round Body
  const bodyGeo = new THREE.CylinderGeometry(0.18, 0.24, 0.5, 12)
  const bodyMat = new THREE.MeshStandardMaterial({ color: color, roughness: 0.5 })
  const body = new THREE.Mesh(bodyGeo, bodyMat)
  body.position.y = 0.25
  group.add(body)

  // 2. Chibi Sphere Head
  const headGeo = new THREE.SphereGeometry(0.22, 16, 16)
  const headMat = new THREE.MeshStandardMaterial({ color: 0xfca5a5, roughness: 0.6 }) // skin-like cute tone
  const head = new THREE.Mesh(headGeo, headMat)
  head.position.y = 0.6
  group.add(head)

  // 3. Cyber neon glowing visor on head face (pointing forward: +Z direction)
  const visorGeo = new THREE.BoxGeometry(0.28, 0.09, 0.06)
  const visorMat = new THREE.MeshStandardMaterial({
    color: 0x0f172a,
    emissive: brightColor,
    emissiveIntensity: 1.0
  })
  const visor = new THREE.Mesh(visorGeo, visorMat)
  visor.position.set(0, 0.62, 0.18)
  group.add(visor)

  // 4. Little round feet
  const shoeMat = new THREE.MeshStandardMaterial({ color: 0x1e293b })
  const footL = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.06, 0.12), shoeMat)
  footL.position.set(-0.09, 0.03, 0.02)
  group.add(footL)

  const footR = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.06, 0.12), shoeMat)
  footR.position.set(0.09, 0.03, 0.02)
  group.add(footR)

  return group
}

// Mouse dragging controls for perspective orbiting
const onMouseDown = (e: MouseEvent) => {
  isDraggingCamera = true
  previousMousePosition = { x: e.clientX, y: e.clientY }
}

const onMouseMove = (e: MouseEvent) => {
  if (!isDraggingCamera || !camera) return

  const deltaX = e.clientX - previousMousePosition.x
  const deltaY = e.clientY - previousMousePosition.y

  theta -= deltaX * 0.005
  phi = Math.max(0.12, Math.min(Math.PI / 2 - 0.12, phi - deltaY * 0.005))

  previousMousePosition = { x: e.clientX, y: e.clientY }
  updateCameraPosition()
}

const onMouseUp = () => {
  isDraggingCamera = false
}

const updateCameraPosition = () => {
  if (!camera) return
  camera.position.x = radius * Math.sin(theta) * Math.cos(phi)
  camera.position.y = radius * Math.sin(phi)
  camera.position.z = radius * Math.cos(theta) * Math.cos(phi)
  camera.lookAt(0, 0.45, 0)
}

const handleResize = () => {
  if (!canvasContainer.value || !camera || !renderer) return
  const w = canvasContainer.value.clientWidth
  const h = canvasContainer.value.clientHeight
  camera.aspect = w / h
  camera.updateProjectionMatrix()
  renderer.setSize(w, h)
}

// Animate loop with physics, waddles and projected dialogue coordinate projects
const animate = () => {
  if (!scene || !renderer || !camera) return
  animFrameId = requestAnimationFrame(animate)

  const time = Date.now() * 0.001

  // Walk physics simulation for all characters
  const moveTowardsTarget = (mesh: THREE.Group | null, target: THREE.Vector3, speed: number, characterName: string) => {
    if (!mesh) return

    const currentPos = mesh.position
    const dir = new THREE.Vector3().copy(target).sub(currentPos)
    const dist = dir.length()

    if (dist > 0.06) {
      // 1. Waddling animation: Translate position and face direction
      dir.normalize()
      currentPos.add(dir.multiplyScalar(speed))

      const targetRotationY = Math.atan2(dir.x, dir.z)
      mesh.rotation.y = targetRotationY

      // Bounce and swing legs!
      mesh.position.y = STAND_HEIGHT + Math.abs(Math.sin(time * 12)) * 0.1
      mesh.rotation.z = Math.sin(time * 12) * 0.06
    } else {
      // 2. Sit/Stand idle and snap facing direction
      mesh.position.copy(target)
      mesh.rotation.z = 0

      // Face the desk or each other depending on character state
      if (characterName === 'planner') {
        const state = sandboxState.value.activeSpeaker
        if (state === 'browser' && plannerTarget.equals(plannerChairPos)) {
          // Look at browser standing next to planner's desk
          mesh.rotation.y = 90 * Math.PI / 180
          mesh.position.y = CHAIR_HEIGHT
        } else if (plannerTarget.equals(plannerChairPos)) {
          // Look forward at computer
          mesh.rotation.y = 180 * Math.PI / 180
          mesh.position.y = CHAIR_HEIGHT
        } else if (plannerTarget.equals(browserDeskPos)) {
          // Stand next to browser desk and look at browser
          mesh.rotation.y = 90 * Math.PI / 180
          mesh.position.y = STAND_HEIGHT
        } else if (plannerTarget.equals(devDeskPos)) {
          // Stand next to dev and look at dev
          mesh.rotation.y = 0 * Math.PI / 180
          mesh.position.y = STAND_HEIGHT
        }
      } 
      
      else if (characterName === 'browser') {
        if (browserTarget.equals(browserChairPos)) {
          mesh.rotation.y = 180 * Math.PI / 180
          mesh.position.y = CHAIR_HEIGHT
        } else if (browserTarget.equals(plannerDeskPos)) {
          mesh.rotation.y = -90 * Math.PI / 180
          mesh.position.y = STAND_HEIGHT
        }
      } 
      
      else if (characterName === 'developer') {
        mesh.rotation.y = 0 * Math.PI / 180
        mesh.position.y = CHAIR_HEIGHT
      }
    }
  }

  // Animate walking speeds
  moveTowardsTarget(devMesh, devTarget, 0.06, 'developer')
  moveTowardsTarget(plannerMesh, plannerTarget, 0.065, 'planner')
  moveTowardsTarget(browserMesh, browserTarget, 0.065, 'browser')

  // Fluctuating Computer Screen emissive light intensity based on active speaker
  const pulseScale = Math.sin(time * 6) * 0.15 + 0.85
  if (devScreenMat) devScreenMat.emissiveIntensity = sandboxState.value.activeSpeaker === 'developer' ? 1.4 * pulseScale : 0.4
  if (plannerScreenMat) plannerScreenMat.emissiveIntensity = sandboxState.value.activeSpeaker === 'planner' ? 1.4 * pulseScale : 0.4
  if (browserScreenMat) browserScreenMat.emissiveIntensity = sandboxState.value.browserState === 'working' ? 1.4 * pulseScale : 0.4

  // Render Three Scene
  renderer.render(scene, camera)

  // Project 3D Speech bubbles into HTML screen overlays coordinates!
  projectDialogueBubbles()
}

// 3D coordinate Vector to 2D Screen Projection projection helper
const projectDialogueBubbles = () => {
  if (!camera || !renderer || !canvasContainer.value) return

  const width = canvasContainer.value.clientWidth
  const height = canvasContainer.value.clientHeight

  const projectNode = (mesh: THREE.Group | null, refVar: any) => {
    if (!mesh) {
      refVar.value = { x: -1000, y: -1000 }
      return
    }

    const vector = new THREE.Vector3()
    mesh.getWorldPosition(vector)
    vector.y += 0.85 // height offset above head

    vector.project(camera!)

    const x = (vector.x * 0.5 + 0.5) * width
    const y = (-(vector.y * 0.5) + 0.5) * height

    // Hide bubbles if they project behind the camera viewport
    if (vector.z > 1) {
      refVar.value = { x: -1000, y: -1000 }
    } else {
      refVar.value = { x, y }
    }
  }

  projectNode(devMesh, devBubblePos)
  projectNode(plannerMesh, plannerBubblePos)
  projectNode(browserMesh, browserBubblePos)
}

// Coordinate Target calculation watchers mapping event timeline to 3D targets
const sync3DTargetsWithState = () => {
  const list = activeTraceMsg.value?.toolResults || []
  if (list.length === 0) {
    devTarget.copy(devChairPos)
    plannerTarget.copy(plannerChairPos)
    browserTarget.copy(browserChairPos)
    return
  }

  const lastItem = list[list.length - 1]
  const type = lastItem.eventType

  if (type === 'stopped') {
    devTarget.copy(devChairPos)
    plannerTarget.copy(plannerChairPos)
    browserTarget.copy(browserChairPos)
  } else if (type === 'plan') {
    devTarget.copy(devChairPos)
    plannerTarget.copy(plannerChairPos)
    browserTarget.copy(browserChairPos)
  } else if (type === 'agent_call') {
    // Planner walks over to Browser's workstation to deliver data package
    plannerTarget.copy(browserDeskPos)
    devTarget.copy(devChairPos)
    browserTarget.copy(browserChairPos)
  } else if (type === 'tool_call') {
    // Browser is working, Planner stays next to Browser desk collaborating
    plannerTarget.copy(browserDeskPos)
    devTarget.copy(devChairPos)
    browserTarget.copy(browserChairPos)
  } else if (type === 'tool_result') {
    // Browser stands up and walks over to Planner desk to submit results
    browserTarget.copy(plannerDeskPos)
    plannerTarget.copy(plannerChairPos)
    devTarget.copy(devChairPos)
  } else if (type === 'summary') {
    // Planner sits back at chair compiling data, Browser walks back to sit
    plannerTarget.copy(plannerChairPos)
    browserTarget.copy(browserChairPos)
    devTarget.copy(devChairPos)
  } else if (type === 'answer') {
    // Planner stands up and walks to developer desk to hand in final reports
    plannerTarget.copy(devDeskPos)
    devTarget.copy(devChairPos)
    browserTarget.copy(browserChairPos)
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'sandbox') {
    nextTick(() => {
      initThreeScene()
      sync3DTargetsWithState()
    })
  } else {
    cleanThreeScene()
  }
})

const cleanThreeScene = () => {
  if (animFrameId) {
    cancelAnimationFrame(animFrameId)
    animFrameId = null
  }

  // Clean events
  if (renderer && renderer.domElement) {
    const canvas = renderer.domElement
    canvas.removeEventListener('mousedown', onMouseDown)
  }
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
  window.removeEventListener('resize', handleResize)

  // Empty container
  if (canvasContainer.value) {
    canvasContainer.value.innerHTML = ''
  }

  scene = null
  camera = null
  renderer = null
  devMesh = null
  plannerMesh = null
  browserMesh = null
  devScreenMat = null
  plannerScreenMat = null
  browserScreenMat = null
}

onMounted(() => {
  nextTick(() => {
    if (activeTab.value === 'sandbox') {
      initThreeScene()
      sync3DTargetsWithState()
    }
  })
})

onUnmounted(() => {
  cleanThreeScene()
})

// Watch toolResults to dynamically recalculate 3D targets in real-time
const toolResultsLength = computed(() => {
  return activeTraceMsg.value?.toolResults?.length || 0
})

watch(toolResultsLength, (newLength) => {
  nextTick(() => {
    // 1. Recalculate 3D walking targets
    if (activeTab.value === 'sandbox') {
      sync3DTargetsWithState()
    }

    // 2. Keep standard timeline scrolled down
    if (rightPanelContent.value) {
      rightPanelContent.value.scrollTop = rightPanelContent.value.scrollHeight
    }
  })
})
</script>
