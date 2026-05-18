<template>
  <div class="message-row" :class="[message.type]">
    <!-- Avatar Container -->
    <div class="message-avatar-container">
      <div class="message-avatar" v-if="message.type === 'user'">U</div>
      <div class="message-avatar server" v-else>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
        </svg>
      </div>
    </div>

    <!-- Message Details -->
    <div class="message-body">
      <div class="message-sender-meta">
        <span class="sender-name">{{ message.type === 'user' ? 'Developer' : 'AgentScope' }}</span>
        <span class="timestamp">{{ timeStr }}</span>
      </div>

      <div class="message-bubble" :class="{ 'error-bubble': message.isError }">
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
        <div 
          v-else 
          class="message-text" 
          v-html="renderedHtml" 
          @click="handleBlockClick"
        ></div>

        <!-- 精致的侧边栏日志唤起按钮 -->
        <div v-if="message.toolResults && message.toolResults.length > 0" class="view-trace-action">
          <button 
            class="btn-view-trace" 
            :class="{ 'active': activeTraceMsgId === message.traceId && isRightPanelOpen }" 
            @click="triggerTracePanel"
            type="button"
          >
            <svg class="tool-icon" xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="16 18 22 12 16 6" />
              <polyline points="8 6 2 12 8 18" />
            </svg>
            <span>
              {{ activeTraceMsgId === message.traceId && isRightPanelOpen ? '正在查看执行步骤' : '查看执行步骤' }} 
              ({{ message.toolResults.length }})
            </span>
          </button>
        </div>
        
        <!-- 丰富的媒体图片展示 (如有) -->
        <div v-if="message.imageUrl" class="media-container">
          <a :href="message.imageUrl" target="_blank" class="media-card-link">
            <img :src="message.imageUrl" class="media-preview" alt="Generated visual attachment" />
            <div class="media-overlay">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <span>查看大图</span>
            </div>
          </a>
        </div>

        <!-- 文件下载卡片 (如有) -->
        <div v-if="message.fileUrl" class="file-container">
          <a :href="message.fileUrl" target="_blank" class="file-download-card">
            <div class="file-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </div>
            <div class="file-details">
              <span class="file-name">{{ extractFileName(message.fileUrl) }}</span>
              <span class="file-action">点击下载报告文件</span>
            </div>
            <div class="file-arrow">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6" />
              </svg>
            </div>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { Message } from '~/types/chat'
import { useMarkdown } from '~/composables/useMarkdown'
import { useStomp } from '~/composables/useStomp'

// Props definition
const props = defineProps<{
  message: Message
}>()

// Load custom Markdown parser & Stomp hooks
const { render, handleBlockClick } = useMarkdown()
const { activeTraceMsgId, isRightPanelOpen, openTracePanel } = useStomp()

// Formatted time cached permanent on mount
const timeStr = ref('')
onMounted(() => {
  const now = new Date()
  timeStr.value = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
})

// Compiled dynamic Markdown rendering
const renderedHtml = computed(() => {
  if (!props.message.text) return ''
  let html = render(props.message.text)
  
  if (props.message.streaming) {
    const closingParagraph = '</p>\n'
    if (html.endsWith(closingParagraph)) {
      html = html.substring(0, html.length - closingParagraph.length) + 
             '<span class="streaming-cursor">▌</span></p>\n'
    } else if (html.endsWith('</p>')) {
      html = html.substring(0, html.length - 4) + 
             '<span class="streaming-cursor">▌</span></p>'
    } else {
      html += '<span class="streaming-cursor">▌</span>'
    }
  }
  return html
})

// File name parser helper
const extractFileName = (fileUrl: string | undefined) => {
  if (!fileUrl) return 'downloaded-report'
  try {
    const parts = fileUrl.split('/')
    return decodeURIComponent(parts[parts.length - 1])
  } catch (e) {
    return 'downloaded-attachment'
  }
}

// Emits trace panel expansion trigger
const triggerTracePanel = () => {
  if (props.message.traceId) {
    openTracePanel(props.message.traceId)
  }
}
</script>
