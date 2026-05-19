<template>
  <main class="settings-layout">
    <!-- Left Category Navigation -->
    <aside class="settings-sidebar">
      <div class="settings-sidebar-header">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="sidebar-header-icon">
          <circle cx="12" cy="12" r="3"></circle>
          <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
        </svg>
        <div class="header-text">
          <h3>Settings</h3>
          <p>控制台配置与模版调优</p>
        </div>
      </div>

      <nav class="settings-sidebar-nav">
        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'prompts' }"
          @click="activeTab = 'prompts'"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z" />
          </svg>
          <span>Prompt 词模板</span>
        </button>

        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'models' }"
          @click="activeTab = 'models'"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
            <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
            <line x1="6" y1="6" x2="6.01" y2="6" />
            <line x1="6" y1="18" x2="6.01" y2="18" />
          </svg>
          <span>模型服务配置</span>
        </button>

        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'preferences' }"
          @click="activeTab = 'preferences'"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 7h-9m3 10H5m4-5h10M4 17a2 2 0 1 0 4 0 2 2 0 1 0-4 0zm10-5a2 2 0 1 0 4 0 2 2 0 1 0-4 0zM8 7a2 2 0 1 0 4 0 2 2 0 1 0-4 0z" />
          </svg>
          <span>常规偏好设置</span>
        </button>

        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'diagnostics' }"
          @click="activeTab = 'diagnostics'"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
          <span>连接状态诊断</span>
        </button>

        <button 
          class="tab-btn" 
          :class="{ 'active': activeTab === 'about' }"
          @click="activeTab = 'about'"
          type="button"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
          <span>关于平台</span>
        </button>
      </nav>
    </aside>

    <!-- Right Pane: Active tab view -->
    <section class="settings-content-pane">
      <!-- 1. Prompt template tab (embeds PromptManager) -->
      <PromptManager 
        v-if="activeTab === 'prompts'" 
        :sidebar-collapsed="sidebarCollapsed" 
        @update:sidebar-collapsed="emit('update:sidebarCollapsed', $event)"
      />

      <!-- 2. Models configuration tab -->
      <div v-else-if="activeTab === 'models'" class="settings-view">
        <header class="top-navbar">
          <div class="nav-left">
            <h2 class="session-title">系统大模型服务对接</h2>
            <p class="session-subtitle">集成 OpenAI 协议或本地开源模型提供端</p>
          </div>
        </header>
        <div class="settings-view-body">
          <div class="settings-card">
            <h3>提供商凭证</h3>
            <p class="card-desc">配置平台在请求推理任务时调用的主流云端模型或本地端点参数。</p>
            
            <div class="form-group">
              <label>首选提供商 (Provider)</label>
              <select class="form-select">
                <option value="openai">OpenAI 兼容协议 (如 DeepSeek, GPT)</option>
                <option value="ollama">Ollama 本地服务</option>
                <option value="anthropic">Anthropic (Claude)</option>
                <option value="huggingface">Hugging Face API</option>
              </select>
            </div>

            <div class="form-group">
              <label>接口地址 (Base URL)</label>
              <input type="text" value="https://api.deepseek.com/v1" class="form-input font-mono" />
              <span class="input-helper">本地部署或自定义代理中转时需修改此项</span>
            </div>

            <div class="form-group">
              <label>鉴权密钥 (API Key)</label>
              <input type="password" value="sk-••••••••••••••••••••••••" class="form-input font-mono" />
            </div>

            <div class="form-group">
              <label>智能体模型 (Agent Chat Model)</label>
              <input type="text" value="deepseek-chat" class="form-input font-mono" />
            </div>

            <div class="form-actions-row">
              <button class="btn btn-primary" type="button" @click="mockSave">保存服务配置</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. Preferences configuration tab -->
      <div v-else-if="activeTab === 'preferences'" class="settings-view">
        <header class="top-navbar">
          <div class="nav-left">
            <h2 class="session-title">常规偏好设置</h2>
            <p class="session-subtitle">控制台界面样式与自动化底座偏好</p>
          </div>
        </header>
        <div class="settings-view-body">
          <div class="settings-card">
            <h3>界面表现</h3>
            
            <div class="form-group">
              <label>控制台主题 (Theme)</label>
              <div class="theme-picker">
                <button class="theme-option active" type="button">
                  <span class="theme-preview-dot light"></span>
                  <span>极简亮白 (Zinc Light)</span>
                </button>
                <button class="theme-option disabled" type="button">
                  <span class="theme-preview-dot dark"></span>
                  <span>深色护眼 (Zinc Dark - 敬请期待)</span>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label>语言首选项 (Interface Language)</label>
              <select class="form-select">
                <option value="zh">简体中文 (Simplified Chinese)</option>
                <option value="en">English (US)</option>
              </select>
            </div>
          </div>

          <div class="settings-card">
            <h3>自动化配置</h3>

            <div class="form-group flex-row">
              <div class="flex-row-text">
                <label>开启浏览器自动托管</label>
                <span class="input-helper">允许智能体调度 Chrome 浏览器执行页面爬虫</span>
              </div>
              <input type="checkbox" checked class="form-switch" />
            </div>

            <div class="form-group flex-row">
              <div class="flex-row-text">
                <label>实时轨迹输出跟踪</label>
                <span class="input-helper">开启此项时，执行复杂智能体工作流将自动弹开右侧执行轨迹抽屉</span>
              </div>
              <input type="checkbox" checked class="form-switch" />
            </div>

            <div class="form-group">
              <label>Prompt 文件自动保存间隔</label>
              <select class="form-select">
                <option value="manual">手动保存</option>
                <option value="5">每 5 秒自动保存</option>
                <option value="30">每 30 秒自动保存</option>
              </select>
            </div>

            <div class="form-actions-row">
              <button class="btn btn-primary" type="button" @click="mockSave">更新用户偏好</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. Diagnostics configuration tab -->
      <div v-else-if="activeTab === 'diagnostics'" class="settings-view">
        <header class="top-navbar">
          <div class="nav-left">
            <h2 class="session-title">连接状态与网络诊断</h2>
            <p class="session-subtitle">监测本控制台与 Java 服务端底座的连接细节</p>
          </div>
        </header>
        <div class="settings-view-body">
          <div class="settings-card">
            <h3>实时网络状态</h3>
            <div class="diag-status-grid">
              <div class="diag-item">
                <span class="diag-label">WebSocket 连通性</span>
                <span class="diag-value" :class="isConnected ? 'text-emerald' : 'text-amber'">
                  {{ isConnected ? '已建立 (Connected)' : '未连接 (Offline)' }}
                </span>
              </div>
              <div class="diag-item">
                <span class="diag-label">后端控制台端点</span>
                <span class="diag-value font-mono">ws://localhost:18081/bs-dialog-websocket</span>
              </div>
              <div class="diag-item">
                <span class="diag-label">REST API 连通性</span>
                <span class="diag-value text-emerald">在线 (HTTP 200 OK)</span>
              </div>
              <div class="diag-item">
                <span class="diag-label">平均响应延迟</span>
                <span class="diag-value font-mono">&lt; 5 ms</span>
              </div>
            </div>
          </div>

          <div class="settings-card">
            <h3>自动化浏览器环境诊断</h3>
            <div class="diag-status-grid">
              <div class="diag-item">
                <span class="diag-label">Playwright 核心驱动</span>
                <span class="diag-value text-emerald">已装载 (Chromium 124.0.x)</span>
              </div>
              <div class="diag-item">
                <span class="diag-label">操作系统</span>
                <span class="diag-value font-mono">macOS (Darwin x64)</span>
              </div>
              <div class="diag-item">
                <span class="diag-label">Java 环境</span>
                <span class="diag-value font-mono">OpenJDK Runtime Environment (build 17.0.x)</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 5. About configuration tab -->
      <div v-else-if="activeTab === 'about'" class="settings-view">
        <header class="top-navbar">
          <div class="nav-left">
            <h2 class="session-title">关于控制台</h2>
            <p class="session-subtitle">智能体协同开发与可视化监督底座信息</p>
          </div>
        </header>
        <div class="settings-view-body">
          <div class="settings-card text-center">
            <div class="brand-logo-large">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="12 2 2 7 12 12 22 7 12 2" />
                <polyline points="2 17 12 22 22 17" />
                <polyline points="2 12 12 17 22 12" />
              </svg>
            </div>
            <h2>Agent<span class="text-indigo">Scope</span> Orchestrator</h2>
            <p class="version font-mono">Version 1.2.0 (Stable Release)</p>
            <p class="about-desc">
              AgentScope 是一个专为基于 Java 底座的多智能体（Multi-Agent）微调与网页交互操作自动化工作流设计的可视化控制台。提供完备的运行时生命周期监控、智能体轨迹跟踪及长文本 Prompt 热替换管理。
            </p>
            <div class="tech-stack-tags">
              <span class="tech-tag">Nuxt 4</span>
              <span class="tech-tag">Vue 3</span>
              <span class="tech-tag">Playwright</span>
              <span class="tech-tag">Spring Boot</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useStomp } from '~/composables/useStomp'
import PromptManager from '~/components/agent/PromptManager.vue'

// Props & Emits
defineProps<{
  sidebarCollapsed: boolean
}>()

const emit = defineEmits<{
  (e: 'update:sidebarCollapsed', val: boolean): void
}>()

const { isConnected } = useStomp()

// Selected tab in settings left menu
const activeTab = ref<'prompts' | 'models' | 'preferences' | 'diagnostics' | 'about'>('prompts')

// Simple alert for mockup buttons
const mockSave = () => {
  alert('设置保存成功！(由于是Mock配置，更改已写入内存中演示)')
}
</script>

<style scoped>
.settings-layout {
  display: flex;
  flex: 1;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-primary);
}

/* Settings navigation sidebar styles */
.settings-sidebar {
  width: 240px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  z-index: 10;
}

.settings-sidebar-header {
  padding: 1.25rem 1rem;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.sidebar-header-icon {
  width: 20px;
  height: 20px;
  color: var(--text-primary);
  flex-shrink: 0;
}

.header-text h3 {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.header-text p {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.settings-sidebar-nav {
  flex: 1;
  padding: 0.75rem 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  overflow-y: auto;
}

/* Category menu buttons */
.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.6rem 0.75rem;
  background: transparent;
  border: none;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-weight: 550;
  font-size: 0.85rem;
  font-family: var(--font-sans);
  text-align: left;
  cursor: pointer;
  width: 100%;
  transition: all 0.15s ease;
}

.tab-btn:hover {
  background: var(--bg-active);
  color: var(--text-primary);
}

.tab-btn.active {
  background: var(--bg-active);
  color: var(--text-primary);
  font-weight: 600;
}

.tab-btn svg {
  width: 15px;
  height: 15px;
}

/* Right settings content views */
.settings-content-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-primary);
}

.settings-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.settings-view-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
}

/* Settings Cards UI */
.settings-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-sm);
}

.settings-card h3 {
  font-size: 0.98rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.35rem;
}

.card-desc {
  font-size: 0.8rem;
  color: var(--text-muted);
  margin-bottom: 1.25rem;
}

/* Forms layout styling */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
  margin-bottom: 1.25rem;
}

.form-group.flex-row {
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-light);
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.flex-row-text {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.flex-row-text label {
  margin-bottom: 0 !important;
}

.form-group label {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-input, .form-select {
  width: 100%;
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
  transition: border-color 0.15s ease;
}

.form-input:focus, .form-select:focus {
  border-color: var(--border-medium);
}

.font-mono {
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
}

.input-helper {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.form-switch {
  width: 36px;
  height: 20px;
  cursor: pointer;
}

.theme-picker {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.2rem;
}

.theme-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.65rem;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  background: var(--bg-primary);
  font-size: 0.8rem;
  font-weight: 550;
  cursor: pointer;
  transition: all 0.15s ease;
}

.theme-option.active {
  border-color: var(--text-primary);
  background: var(--bg-tertiary);
}

.theme-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.theme-preview-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid var(--border-medium);
}

.theme-preview-dot.light {
  background: #ffffff;
}

.theme-preview-dot.dark {
  background: #18181b;
}

.form-actions-row {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.5rem 1rem;
  font-size: 0.82rem;
  font-weight: 550;
  border-radius: var(--radius-sm);
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s ease;
}

.btn-primary {
  background: var(--text-primary);
  color: #ffffff;
}

.btn-primary:hover {
  background: #27272a;
}

/* Diagnostics specific styles */
.diag-status-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.diag-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
}

.diag-label {
  font-size: 0.72rem;
  color: var(--text-muted);
  font-weight: 550;
}

.diag-value {
  font-size: 0.82rem;
  font-weight: 600;
}

.text-emerald {
  color: #059669;
}

.text-amber {
  color: var(--accent-amber);
}

/* About tab specific styling */
.text-center {
  text-align: center;
}

.brand-logo-large {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 1rem auto 1.5rem;
  box-shadow: var(--shadow-md);
}

.brand-logo-large svg {
  width: 40px;
  height: 40px;
}

.settings-card h2 {
  font-size: 1.35rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin-bottom: 0.2rem;
}

.text-indigo {
  color: var(--accent-indigo);
}

.version {
  font-size: 0.78rem;
  color: var(--text-muted);
  margin-bottom: 1.25rem;
  display: block;
}

.about-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.6;
  max-width: 540px;
  margin: 0 auto 1.75rem;
}

.tech-stack-tags {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tech-tag {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 0.2rem 0.55rem;
  border-radius: 9999px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  color: var(--text-secondary);
}
</style>
