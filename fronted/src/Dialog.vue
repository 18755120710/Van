<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- Left Navigation Sidebar -->
    <aside class="sidebar">
      <!-- Sidebar Header -->
      <div class="sidebar-header">
        <div class="brand">
          <svg class="brand-logo" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
          <span class="brand-name">Agent<span class="text-indigo">Scope</span></span>
        </div>
        <button class="collapse-btn" @click="isSidebarCollapsed = !isSidebarCollapsed" title="Toggle Sidebar">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="11 17 6 12 11 7"/><polyline points="18 17 13 12 18 7"/></svg>
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
        <a href="#" class="nav-item active" @click.prevent>
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          <span>Chat Console</span>
        </a>
        <a href="#" class="nav-item" @click.prevent="clearChatHistory">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>
          <span>Clear Conversation</span>
        </a>

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

    <!-- Main Content Area -->
    <main class="main-content">
      <!-- Top Sleek Header -->
      <header class="top-navbar">
        <div class="nav-left">
          <button class="expand-sidebar-btn" v-if="isSidebarCollapsed" @click="isSidebarCollapsed = false" title="Expand Sidebar">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/></svg>
          </button>
          <div class="session-info">
            <h2 class="session-title">Main Orchestration Console</h2>
            <p class="session-subtitle">Interactive Web Automation Agent</p>
          </div>
        </div>
        <div class="nav-right">
          <!-- Small badge for online presence -->
          <span class="status-badge" :class="{ 'connected': isConnected }">
            <span class="badge-dot"></span>
            {{ isConnected ? 'Online' : 'Offline' }}
          </span>
          <button class="icon-action-btn" @click="reconnectWebSocket" title="Force Reconnect">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/></svg>
          </button>
        </div>
      </header>

      <!-- Chat Viewport -->
      <div class="chat-viewport">
        <!-- Scrollable Messages Container -->
        <div class="messages-container" ref="messagesContainer">
          
          <!-- Elegant Welcome Screen for Clean Empty State -->
          <div class="welcome-screen" v-if="messages.length === 0">
            <div class="welcome-hero">
              <div class="welcome-logo">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>
              </div>
              <h1>Welcome to AgentScope</h1>
              <p>A high-performance orchestration console designed to supervise local Java web-automation workflows.</p>
            </div>
            
            <div class="welcome-grid">
              <div class="welcome-card" @click="prefillPrompt('Search Google for today\'s top news on AI tools')">
                <div class="card-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="4"/><line x1="4.93" y1="4.93" x2="9.17" y2="9.17"/><line x1="19.07" y1="4.93" x2="14.83" y2="9.17"/><line x1="14.83" y1="14.83" x2="19.07" y2="19.07"/><line x1="9.17" y1="14.83" x2="4.93" y2="19.07"/></svg>
                </div>
                <h3>Web Crawling</h3>
                <p>Deploy Chrome drivers to navigate and crawl unstructured information dynamically.</p>
                <span class="card-arrow">→</span>
              </div>
              
              <div class="welcome-card" @click="prefillPrompt('Analyze my target directories and compile a summary log')">
                <div class="card-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                </div>
                <h3>Data Extraction</h3>
                <p>Process, summarize, and aggregate local files or remote datasets with ease.</p>
                <span class="card-arrow">→</span>
              </div>
              
              <div class="welcome-card" @click="prefillPrompt('Deploy standard browser-use tests')">
                <div class="card-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"/><rect x="2" y="14" width="20" height="8" rx="2" ry="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg>
                </div>
                <h3>Diagnostic Checks</h3>
                <p>Ensure connection speeds, WebSocket packet round-trips, and broker nodes are stable.</p>
                <span class="card-arrow">→</span>
              </div>
            </div>
          </div>

          <!-- Dynamic Chat Conversation Stream -->
          <div class="chat-flow-list" v-else>
            <div v-for="(message, index) in messages" :key="index" :class="['message-row', message.type]">
              
              <!-- Custom Visual Avatar -->
              <div class="message-avatar-container">
                <div class="message-avatar" v-if="message.type === 'user'">U</div>
                <div class="message-avatar server" v-else>
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
                </div>
              </div>

              <!-- Message Details -->
              <div class="message-body">
                <div class="message-sender-meta">
                  <span class="sender-name">{{ message.type === 'user' ? 'Developer' : 'AgentScope' }}</span>
                  <span class="timestamp">{{ getFormattedTime() }}</span>
                </div>

                <div class="message-bubble">
                  <!-- 思考中 Skeleton 骨架屏动画 -->
                  <div v-if="!message.text && message.streaming" class="thinking-placeholder">
                    <div class="thinking-dots">
                      <span class="thinking-dot"></span>
                      <span class="thinking-dot"></span>
                      <span class="thinking-dot"></span>
                    </div>
                    <span class="thinking-text">AgentScope 正在规划步骤...</span>
                  </div>

                  <!-- 推理主文本流 -->
                  <div v-else class="message-text">
                    {{ message.text }}<span v-if="message.streaming" class="streaming-cursor">▌</span>
                  </div>

                  <!-- 工具调用折叠式终端日志面板 -->
                  <div v-if="message.toolResults && message.toolResults.length > 0" class="tools-execution-panel">
                    <div class="tools-header" @click="message.showTools = !message.showTools">
                      <div class="tools-title">
                        <svg class="tool-icon" xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
                        <span>执行步骤日志 ({{ message.toolResults.length }})</span>
                      </div>
                      <span class="toggle-arrow" :class="{ 'expanded': message.showTools }">
                        <svg xmlns="http://www.w3.org/2000/svg" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"/></svg>
                      </span>
                    </div>
                    <div v-show="message.showTools !== false" class="tools-list">
                      <div v-for="(tool, tIdx) in message.toolResults" :key="tIdx" class="tool-item">
                        <div class="tool-meta">
                          <span class="tool-dot"></span>
                          <span class="tool-time">{{ tool.timestamp }}</span>
                        </div>
                        <pre class="tool-code"><code>{{ tool.text }}</code></pre>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 丰富的媒体图片展示 (如有) -->
                  <div v-if="message.imageUrl" class="media-container">
                    <a :href="message.imageUrl" target="_blank" class="media-card-link">
                      <img :src="message.imageUrl" class="media-preview" alt="Generated visual attachment" />
                      <div class="media-overlay">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                        <span>查看大图</span>
                      </div>
                    </a>
                  </div>

                  <!-- 文件下载卡片 (如有) -->
                  <div v-if="message.fileUrl" class="file-container">
                    <a :href="message.fileUrl" target="_blank" class="file-download-card">
                      <div class="file-icon">
                        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                      </div>
                      <div class="file-details">
                        <span class="file-name">{{ extractFileName(message.fileUrl) }}</span>
                        <span class="file-action">点击下载报告文件</span>
                      </div>
                      <div class="file-arrow">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
                      </div>
                    </a>
                  </div>
                </div>
              </div>

            </div>
          </div>

        </div>

        <!-- Floating Input Footer Panel -->
        <footer class="input-panel">
          <div class="input-inner">
            
            <!-- Agent Thinking Animation -->
            <div class="agent-status-panel" v-if="disableInput">
              <div class="wave-loader">
                <span class="wave-dot delay-1"></span>
                <span class="wave-dot delay-2"></span>
                <span class="wave-dot delay-3"></span>
              </div>
              <span class="agent-thinking-text">AgentScope is executing web automations...</span>
            </div>

            <!-- Sleek Input Box -->
            <div class="input-box-wrapper" :class="{ 'disabled': disableInput }">
              <input 
                type="text" 
                v-model="newMessage" 
                placeholder="Message AgentScope or request browser automations..." 
                @keyup.enter="sendMessage" 
                :disabled="disableInput" 
              />
              <button 
                @click="sendMessage" 
                :disabled="disableInput || !newMessage.trim()" 
                class="btn-send"
                title="Send Message"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
              </button>
            </div>

            <p class="input-disclaimer">
              AgentScope Orchestrator v1.0. Preserves active session tokens and STOMP channels.
            </p>
          </div>
        </footer>

      </div>
    </main>
  </div>
</template>

<script>
import { Client } from '@stomp/stompjs';

export default {
    data() {
        return {
            disableInput: false,
            isVisible: true,
            messages: [
                { type: 'user', text: 'Hello, server!' },
                { type: 'server', text: 'Hello, user!' },
            ],
            newMessage: '',
            stompClient: null,
            isSidebarCollapsed: false,
            isConnected: false
        };
    },
    methods: {
        sendMessage() {
            if (this.newMessage.trim() && !this.disableInput) {
                var msg = { type: 'user', text: this.newMessage };
                this.messages.push(msg);
                this.newMessage = '';
                this.disableInput = true; // 发送时立即加锁，等待流式输出
                if(this.stompClient && this.stompClient.connected) {
                    this.stompClient.publish({ destination: '/app/enhanced-dialog', body: JSON.stringify(msg) });
                }
                this.scrollToBottom();
            }
        },
        closeDialog() {
            this.isVisible = false;
        },
        handleMessage(playload) {
            const message = JSON.parse(playload.body);
            
            // 支持流式响应处理
            if (message.streamId) {
                let existingMsg = this.messages.find(m => m.streamId === message.streamId);
                
                if (existingMsg) {
                    // 更新流式状态
                    if (message.streaming !== undefined) {
                        existingMsg.streaming = message.streaming;
                    }
                    if (message.streamEnd !== undefined) {
                        existingMsg.streamEnd = message.streamEnd;
                        if (message.streamEnd) {
                            existingMsg.streaming = false;
                            this.disableInput = false; // 解锁输入框
                        }
                    }
                    
                    // 根据不同的事件类型分类合并
                    if (message.eventType === 'REASONING') {
                        if (message.text) {
                            existingMsg.text = (existingMsg.text || '') + message.text;
                        }
                    } else if (message.eventType === 'AGENT_RESULT') {
                        if (message.text) {
                            existingMsg.text = message.text; // 最终回答作为权威结果覆盖
                        }
                    } else if (message.eventType === 'TOOL_RESULT') {
                        if (message.text) {
                            if (!existingMsg.toolResults) {
                                existingMsg.toolResults = [];
                            }
                            existingMsg.toolResults.push({
                                text: message.text,
                                timestamp: this.getFormattedTime()
                            });
                        }
                    } else if (message.eventType === 'ERROR') {
                        existingMsg.isError = true;
                        if (message.text) {
                            existingMsg.text = (existingMsg.text || '') + '\n' + message.text;
                        }
                    } else if (message.eventType === 'SUMMARY') {
                        if (message.text) {
                            existingMsg.summary = (existingMsg.summary || '') + message.text;
                        }
                    }
                    
                    // 更新附件等媒体资源
                    if (message.imageUrl) {
                        existingMsg.imageUrl = message.imageUrl;
                    }
                    if (message.fileUrl) {
                        existingMsg.fileUrl = message.fileUrl;
                    }
                    if (message.openUrl) {
                        existingMsg.openUrl = message.openUrl;
                    }
                } else {
                    // 创建全新的流式消息项
                    const newMsg = {
                        type: 'server',
                        streamId: message.streamId,
                        text: (message.eventType === 'REASONING' || message.eventType === 'AGENT_RESULT') ? (message.text || '') : '',
                        eventType: message.eventType,
                        streaming: message.streaming ?? true,
                        streamEnd: message.streamEnd ?? false,
                        toolResults: message.eventType === 'TOOL_RESULT' && message.text ? [{
                            text: message.text,
                            timestamp: this.getFormattedTime()
                        }] : [],
                        showTools: true, // 默认展开步骤
                        imageUrl: message.imageUrl,
                        fileUrl: message.fileUrl,
                        openUrl: message.openUrl,
                        summary: message.eventType === 'SUMMARY' ? message.text : ''
                    };
                    
                    if (message.eventType === 'ERROR') {
                        newMsg.isError = true;
                        newMsg.text = message.text || '';
                    }
                    
                    this.messages.push(newMsg);
                    
                    if (message.streamEnd) {
                        this.disableInput = false;
                    } else {
                        this.disableInput = true;
                    }
                }
                this.scrollToBottom();
            } else {
                // 兼容原有的非流式一般消息
                if (message.text) {
                    this.messages.push(message);
                    this.scrollToBottom();
                }
                if (message.meta) {
                    if (message.meta.serverStatusHint == 0) {
                        this.disableInput = false;
                    } else if (message.meta.serverStatusHint == 1) {
                        this.disableInput = true;
                    }
                }
            }
        },
        scrollToBottom() {
            this.$nextTick(() => {
                const messagesContainer = this.$refs.messagesContainer;
                if(messagesContainer) {
                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                }
            });
        },
        clearChatHistory() {
            if (confirm("Are you sure you want to wipe the session conversation history?")) {
                this.messages = [];
            }
        },
        prefillPrompt(prompt) {
            this.newMessage = prompt;
        },
        extractFileName(fileUrl) {
            if (!fileUrl) return "downloaded-report";
            try {
                const parts = fileUrl.split('/');
                return decodeURIComponent(parts[parts.length - 1]);
            } catch (e) {
                return "downloaded-attachment";
            }
        },
        getFormattedTime() {
            const now = new Date();
            return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        },
        reconnectWebSocket() {
            console.log("Reactivating STOMP WebSocket client...");
            if (this.stompClient) {
                this.isConnected = false;
                this.stompClient.deactivate().then(() => {
                    this.stompClient.activate();
                });
            }
        }
    },
    mounted() {
        this.scrollToBottom();
    },
    created() {
        console.log("Starting connection to WebSocket Server")
        this.stompClient = new Client({
            brokerURL: 'ws://localhost:18081/bs-dialog-websocket'
        });
        this.stompClient.onConnect = (frame) => {
            console.log('Connected: ' + frame);
            this.isConnected = true;
            this.stompClient.subscribe('/user/queue/dialog', this.handleMessage);
        };
        this.stompClient.onDisconnect = () => {
            console.log('Disconnected from STOMP broker');
            this.isConnected = false;
        };
        this.stompClient.onWebSocketError = (error) => {
            console.error('Error with websocket', error);
            this.isConnected = false;
        };

        this.stompClient.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
            this.isConnected = false;
        };
        this.stompClient.activate();
    }
};
</script>

<style scoped>
/* App Layout Structure */
.app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background: var(--bg-primary);
  color: var(--text-primary);
  overflow: hidden;
  position: relative;
}

/* Sidebar Styling */
.sidebar {
  width: 260px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: transform 0.22s cubic-bezier(0.2, 0.8, 0.2, 1), width 0.22s cubic-bezier(0.2, 0.8, 0.2, 1);
  z-index: 20;
}

.app-layout.sidebar-collapsed .sidebar {
  width: 0;
  transform: translateX(-260px);
  overflow: hidden;
  border-right: none;
}

.sidebar-header {
  padding: 1.25rem 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--border-light);
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.brand-logo {
  width: 22px;
  height: 22px;
  color: var(--text-primary);
}

.brand-name {
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: -0.03em;
}

.text-indigo {
  color: var(--accent-indigo);
}

.collapse-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.collapse-btn:hover {
  background: var(--bg-active);
  color: var(--text-primary);
}

.collapse-btn svg {
  width: 16px;
  height: 16px;
}

/* Connection Status Card */
.status-card {
  margin: 1rem 0.75rem 0.5rem;
  padding: 0.75rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.8rem;
  font-weight: 550;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: var(--accent-amber);
  animation: pulse-amber 1.8s infinite ease-in-out;
}

.status-dot.connected {
  background-color: var(--accent-emerald);
  animation: pulse-dot 1.8s infinite ease-in-out;
}

.status-text {
  color: var(--text-secondary);
}

.status-details {
  font-size: 0.7rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
  font-family: monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Navigation / Sidebar list */
.sidebar-nav {
  flex: 1;
  padding: 0.75rem 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  overflow-y: auto;
}

.nav-section-title {
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-muted);
  letter-spacing: 0.05em;
  padding: 0.75rem 0.5rem 0.25rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.6rem 0.65rem;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  font-weight: 500;
  text-decoration: none;
  font-size: 0.88rem;
  transition: all 0.15s ease;
}

.nav-item:hover {
  background: var(--bg-active);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--bg-active);
  color: var(--text-primary);
  font-weight: 600;
}

.nav-item svg {
  width: 15px;
  height: 15px;
}

.capability-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding: 0.35rem 0.5rem;
}

.cap-tag {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.78rem;
  color: var(--text-muted);
}

.cap-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--text-disabled);
}

/* Sidebar Footer */
.sidebar-footer {
  padding: 0.85rem 1rem;
  border-top: 1px solid var(--border-light);
  background: var(--bg-secondary);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.user-avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--text-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.8rem;
}

.user-info {
  line-height: 1.25;
}

.user-name {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  font-size: 0.7rem;
  color: var(--text-muted);
}

/* Main Content Area */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-primary);
  position: relative;
}

/* Top Navbar */
.top-navbar {
  height: 52px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.25rem;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  z-index: 10;
  flex-shrink: 0;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.expand-sidebar-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.expand-sidebar-btn:hover {
  background: var(--bg-active);
  color: var(--text-primary);
}

.expand-sidebar-btn svg {
  width: 16px;
  height: 16px;
}

.session-info {
  line-height: 1.35;
}

.session-title {
  font-size: 0.9rem;
  font-weight: 600;
}

.session-subtitle {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.72rem;
  font-weight: 500;
  background: #f3f4f6;
  color: var(--text-secondary);
}

.status-badge.connected {
  background: rgba(16, 185, 129, 0.08);
  color: var(--accent-emerald);
}

.badge-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background-color: var(--text-disabled);
}

.status-badge.connected .badge-dot {
  background-color: var(--accent-emerald);
}

.icon-action-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.icon-action-btn:hover {
  background: var(--bg-active);
  color: var(--text-primary);
}

.icon-action-btn svg {
  width: 15px;
  height: 15px;
}

/* Chat Viewport Layout */
.chat-viewport {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 52px);
  position: relative;
  overflow: hidden;
}

/* Scrollable messages box */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 1.25rem;
  display: flex;
  flex-direction: column;
}

/* Chat Flow List (centered reading flow) */
.chat-flow-list {
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Single Message Row */
.message-row {
  display: flex;
  gap: 0.85rem;
  animation: fadeInUp 0.3s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.message-row.user {
  flex-direction: row-reverse;
}

.message-avatar-container {
  flex-shrink: 0;
}

.message-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--bg-active);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  border: 1px solid var(--border-light);
}

.message-avatar.server {
  background: rgba(9, 9, 11, 0.05);
  color: var(--text-primary);
  border-color: var(--border-light);
}

.message-avatar svg {
  width: 14px;
  height: 14px;
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  max-width: 78%;
}

.user .message-body {
  align-items: flex-end;
}

.message-sender-meta {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.72rem;
  color: var(--text-muted);
}

.user .message-sender-meta {
  flex-direction: row-reverse;
}

.sender-name {
  font-weight: 550;
  color: var(--text-secondary);
}

.timestamp {
  font-weight: 400;
}

/* Chat Bubble UI */
.message-bubble {
  padding: 0.75rem 1rem;
  font-size: 0.92rem;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: var(--shadow-sm);
}

.user .message-bubble {
  background: var(--accent-blue);
  color: #ffffff;
  border-radius: 1.1rem 1.1rem 0.2rem 1.1rem;
}

.server .message-bubble {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-light);
  border-radius: 1.1rem 1.1rem 1.1rem 0.2rem;
}

/* Media Cards / Image attachments */
.media-container {
  margin-top: 0.65rem;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-light);
  max-width: 440px;
}

.media-card-link {
  display: block;
  position: relative;
  overflow: hidden;
  background: #000;
}

.media-preview {
  display: block;
  width: 100%;
  height: auto;
  max-height: 250px;
  object-fit: cover;
  transition: transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1), opacity 0.3s ease;
}

.media-card-link:hover .media-preview {
  transform: scale(1.02);
  opacity: 0.92;
}

.media-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0) 100%);
  padding: 0.65rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.media-card-link:hover .media-overlay {
  opacity: 1;
}

.media-overlay svg {
  color: #ffffff;
}

/* Download file card */
.file-container {
  margin-top: 0.5rem;
  max-width: 380px;
  width: 100%;
}

.file-download-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0.85rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  text-decoration: none !important;
  color: var(--text-primary) !important;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
}

.file-download-card:hover {
  background: var(--bg-active);
  border-color: var(--border-medium);
  transform: translateY(-1px);
}

.file-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--bg-secondary);
  color: var(--accent-indigo);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid var(--border-light);
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.file-name {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.file-action {
  font-size: 0.7rem;
  color: var(--text-muted);
}

.file-arrow {
  color: var(--text-muted);
  display: flex;
  align-items: center;
}

.file-arrow svg {
  width: 16px;
  height: 16px;
}

/* Welcome Screen Styling */
.welcome-screen {
  max-width: 800px;
  width: 100%;
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem 0.5rem;
}

.welcome-hero {
  text-align: center;
  margin-bottom: 2.5rem;
  max-width: 550px;
}

.welcome-logo {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.25rem;
  box-shadow: var(--shadow-sm);
}

.welcome-logo svg {
  width: 26px;
  height: 26px;
}

.welcome-hero h1 {
  font-size: 1.85rem;
  font-weight: 700;
  letter-spacing: -0.04em;
  margin-bottom: 0.6rem;
}

.welcome-hero p {
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.45;
}

.welcome-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  width: 100%;
}

.welcome-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  cursor: pointer;
  position: relative;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.welcome-card:hover {
  border-color: var(--border-medium);
  background: var(--bg-secondary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.card-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  border: 1px solid var(--border-light);
}

.card-icon svg {
  width: 18px;
  height: 18px;
}

.welcome-card h3 {
  font-size: 0.92rem;
  font-weight: 600;
  margin-bottom: 0.4rem;
  color: var(--text-primary);
}

.welcome-card p {
  font-size: 0.78rem;
  color: var(--text-muted);
  line-height: 1.35;
  margin-bottom: 1.25rem;
}

.card-arrow {
  position: absolute;
  bottom: 1rem;
  right: 1.25rem;
  font-size: 1rem;
  color: var(--text-disabled);
  transition: transform 0.2s ease, color 0.2s ease;
}

.welcome-card:hover .card-arrow {
  transform: translateX(2px);
  color: var(--text-primary);
}

/* Floating Input Footer Panel */
.input-panel {
  padding: 0 1.25rem 1.25rem;
  background: linear-gradient(to top, var(--bg-primary) 70%, transparent 100%);
  flex-shrink: 0;
  position: relative;
}

.input-inner {
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
}

/* Wave thinking loading layout */
.agent-status-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 0.65rem;
  animation: fadeInUp 0.25s ease-out forwards;
}

.wave-loader {
  display: flex;
  align-items: center;
  gap: 2.5px;
  height: 10px;
}

.wave-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background-color: var(--text-primary);
  animation: wave-bounce 1.2s infinite ease-in-out;
}

.wave-dot.delay-1 {
  animation-delay: 0s;
}

.wave-dot.delay-2 {
  animation-delay: 0.15s;
}

.wave-dot.delay-3 {
  animation-delay: 0.3s;
}

.agent-thinking-text {
  font-size: 0.78rem;
  color: var(--text-muted);
  font-weight: 500;
}

/* Input Area box */
.input-box-wrapper {
  display: flex;
  align-items: center;
  padding: 0.4rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  transition: all 0.2s ease;
}

.input-box-wrapper:focus-within {
  border-color: var(--border-medium);
  box-shadow: var(--shadow-lg), 0 0 0 3px rgba(9, 9, 11, 0.05);
}

.input-box-wrapper.disabled {
  background: var(--bg-secondary);
  box-shadow: var(--shadow-sm);
  cursor: not-allowed;
}

.input-box-wrapper input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  padding: 0.55rem 0.85rem;
  font-size: 0.92rem;
  font-family: inherit;
}

.input-box-wrapper input::placeholder {
  color: var(--text-muted);
}

.input-box-wrapper input:disabled {
  cursor: not-allowed;
  opacity: 0.8;
}

.btn-send {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--text-primary);
  color: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s cubic-bezier(0.2, 0.8, 0.2, 1);
  flex-shrink: 0;
}

.btn-send:hover:not(:disabled) {
  background: var(--accent-blue-hover);
  transform: scale(1.02);
}

.btn-send:disabled {
  background: var(--bg-tertiary);
  color: var(--text-disabled);
  cursor: not-allowed;
}

.btn-send svg {
  width: 14px;
  height: 14px;
}

.input-disclaimer {
  text-align: center;
  font-size: 0.68rem;
  color: var(--text-disabled);
  margin-top: 0.5rem;
}

/* Animations for messages */
.fade-up-enter-active {
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.fade-up-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

/* Streaming Cursor blinking effect */
.streaming-cursor {
  display: inline-block;
  margin-left: 2px;
  color: var(--accent-indigo);
  font-weight: 700;
  animation: cursor-blink 0.8s infinite;
  vertical-align: middle;
}

@keyframes cursor-blink {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

/* Thinking state Skeleton waves */
.thinking-placeholder {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.25rem 0;
}

.thinking-dots {
  display: flex;
  align-items: center;
  gap: 3.5px;
}

.thinking-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--text-muted);
  animation: thinking-bounce 1.4s infinite ease-in-out both;
  opacity: 0.6;
}

.thinking-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.thinking-dot:nth-child(2) {
  animation-delay: -0.16s;
}

.thinking-text {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-weight: 500;
}

@keyframes thinking-bounce {
  0%, 80%, 100% { 
    transform: scale(0.6);
    opacity: 0.5;
  } 
  40% { 
    transform: scale(1.1);
    opacity: 1;
    background-color: var(--accent-indigo);
  }
}

/* Collapsible Tools Execution Panel */
.tools-execution-panel {
  margin-top: 0.75rem;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background: var(--bg-primary);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.tools-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 0.85rem;
  background: var(--bg-tertiary);
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid var(--border-light);
  transition: background 0.15s ease;
}

.tools-header:hover {
  background: var(--bg-active);
}

.tools-title {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.tool-icon {
  color: var(--accent-indigo);
}

.toggle-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: transform 0.22s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.toggle-arrow.expanded {
  transform: rotate(180deg);
}

.tools-list {
  padding: 0.6rem 0.85rem;
  background: #09090b; /* Deep Zinc Black Terminal Background */
  max-height: 250px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tool-item {
  border-left: 2px solid #27272a; /* Zinc 800 line */
  padding-left: 0.6rem;
  margin-bottom: 0.25rem;
}

.tool-meta {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  margin-bottom: 0.2rem;
}

.tool-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--accent-emerald);
}

.tool-time {
  font-size: 0.65rem;
  color: #71717a; /* Zinc-500 */
  font-family: monospace;
}

.tool-code {
  margin: 0;
  font-family: SFMono-Regular, Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 0.75rem;
  color: #e4e4e7; /* zinc-200 */
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.4;
}

/* Responsive queries */
@media (max-width: 900px) {
  .welcome-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  .welcome-hero {
    margin-bottom: 1.5rem;
  }
  .sidebar {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 260px;
  }
  .app-layout.sidebar-collapsed .sidebar {
    width: 0;
    transform: translateX(-260px);
  }
}
</style>
