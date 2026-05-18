import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import type { Message, DialogMessageDTO } from '~/types/chat'

// Global singleton state so that WS state persists when switching pages/components
const isConnected = ref(false)
const disableInput = ref(false)
const stopping = ref(false)
const messages = ref<Message[]>([])
const isRightPanelOpen = ref(false)
const activeTraceMsgId = ref<string | null>(null)

let stompClient: Client | null = null

export const useStomp = () => {
  const getFormattedTime = () => {
    const now = new Date()
    const pad = (num: number) => String(num).padStart(2, '0')
    return `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
  }

  const handleMessage = (payload: { body: string }) => {
    let dto: DialogMessageDTO
    try {
      dto = JSON.parse(payload.body)
    } catch (e) {
      console.error('Failed to parse WS payload:', e)
      return
    }

    // A. 处理 eventType === "stopped" 的情况 (覆盖 requirement 8, 16)
    if (dto.eventType === 'stopped') {
      let existingMsg = dto.traceId ? messages.value.find(m => m.type === 'server' && m.traceId === dto.traceId) : null
      
      if (existingMsg) {
        existingMsg.streaming = false
        existingMsg.stopped = true
        existingMsg.toolResults = existingMsg.toolResults || []
        existingMsg.toolResults.push({
          eventType: 'stopped',
          agentName: dto.agentName || 'System',
          text: dto.text || '已停止当前 Agent 执行。',
          timestamp: getFormattedTime()
        })
      } else {
        // 如果不存在 traceId，push 一条 server 消息显示 stopped 文本
        messages.value.push({
          type: 'server',
          traceId: dto.traceId,
          text: dto.text || '已停止当前 Agent 执行。',
          streaming: false
        })
      }
      disableInput.value = false
      stopping.value = false
      return
    }

    // 1. 优先判断 dto.traceId 以支持 Agent 执行过程消息
    if (dto.traceId) {
      let existingMsg = messages.value.find(m => m.type === 'server' && m.traceId === dto.traceId)
      
      if (existingMsg) {
        // B. 如果 existingMsg.stopped === true，忽略后续非 answer/error/stopped 消息 (覆盖 requirement 17)
        if (existingMsg.stopped && dto.eventType !== 'answer' && dto.eventType !== 'error' && dto.eventType !== 'stopped') {
          console.log('Ignoring post-stopped message:', dto.eventType)
          return
        }
      } else {
        existingMsg = {
          type: 'server',
          traceId: dto.traceId,
          text: '',
          streaming: true,
          toolResults: [],
          showTools: true,
          isError: false
        }
        messages.value.push(existingMsg)
      }

      // 更新可能存在的图片、文件、URL
      if (dto.imageUrl) {
        existingMsg.imageUrl = dto.imageUrl
      }
      if (dto.fileUrl) {
        existingMsg.fileUrl = dto.fileUrl
      }
      if (dto.openUrl) {
        existingMsg.openUrl = dto.openUrl
      }

      // 2. 处理 eventType === "answer_delta" 流式响应片段
      if (dto.eventType === 'answer_delta') {
        existingMsg.text = (existingMsg.text || '') + (dto.text || '')
        existingMsg.streaming = true
      }

      // 3. 处理 eventType === "answer" 最终回答信号
      else if (dto.eventType === 'answer') {
        if (dto.text) {
          if (!existingMsg.text) {
            existingMsg.text = dto.text
          }
        }
        existingMsg.streaming = false
        if (dto.done === true) {
          disableInput.value = false
          stopping.value = false
        }
      }

      // 4. 处理 dto.trace === true 且 eventType !== "error" 的执行轨迹消息
      else if (dto.trace === true && dto.eventType !== 'error') {
        existingMsg.toolResults = existingMsg.toolResults || []
        existingMsg.toolResults.push({
          eventType: dto.eventType || '',
          agentName: dto.agentName,
          toolName: dto.toolName,
          text: dto.text || '',
          timestamp: getFormattedTime()
        })
        activeTraceMsgId.value = dto.traceId
        
        // 关键特性：复杂轨迹事件自动拉开右侧思考面板
        isRightPanelOpen.value = true
        
        if (dto.done === true) {
          existingMsg.streaming = false
          disableInput.value = false
          stopping.value = false
        }
      }

      // 5. 处理 eventType === "error" 错误轨迹，且避免重复追加
      else if (dto.eventType === 'error') {
        existingMsg.toolResults = existingMsg.toolResults || []
        const isAlreadyAdded = existingMsg.toolResults.some(
          t => t.eventType === 'error' && t.text === dto.text
        )
        if (!isAlreadyAdded) {
          existingMsg.toolResults.push({
            eventType: dto.eventType,
            agentName: dto.agentName,
            toolName: dto.toolName,
            text: dto.text || '',
            timestamp: getFormattedTime()
          })
        }
        activeTraceMsgId.value = dto.traceId
        isRightPanelOpen.value = true
        existingMsg.isError = true
        existingMsg.streaming = false
        disableInput.value = false
        stopping.value = false
      }

      // 6. 如果 dto.done === true，则更新状态并恢复输入框
      if (dto.done === true) {
        existingMsg.streaming = false
        disableInput.value = false
        stopping.value = false
      }
    } else {
      // 7. 保留旧消息兼容逻辑：如果没有 traceId，但有 text，按原来的方式 push 到 messages
      if (dto.text) {
        messages.value.push({
          type: dto.type || 'server',
          text: dto.text,
          imageUrl: dto.imageUrl,
          fileUrl: dto.fileUrl,
          openUrl: dto.openUrl
        })
      }
      if (dto.meta) {
        if (dto.meta.serverStatusHint === 0) {
          disableInput.value = false
          stopping.value = false
        } else if (dto.meta.serverStatusHint === 1) {
          disableInput.value = true
        }
      }
    }
  }

  const connect = () => {
    if (process.server) return
    if (stompClient) return

    console.log('Starting connection to WebSocket Server')
    stompClient = new Client({
      brokerURL: 'ws://localhost:18081/bs-dialog-websocket',
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    })

    stompClient.onConnect = (frame) => {
      console.log('Connected: ' + frame)
      isConnected.value = true
      if (stompClient) {
        stompClient.subscribe('/user/queue/dialog', handleMessage)
      }
    }

    stompClient.onDisconnect = () => {
      console.log('Disconnected from STOMP broker')
      isConnected.value = false
    }

    stompClient.onWebSocketError = (error) => {
      console.error('Error with websocket', error)
      isConnected.value = false
    }

    stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
      console.error('Additional details: ' + frame.body)
      isConnected.value = false
    }

    stompClient.activate()
  }

  const disconnect = () => {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
      isConnected.value = false
    }
  }

  const sendMessage = (text: string) => {
    if (!stompClient || !isConnected.value) {
      console.warn('STOMP client not connected, cannot send message')
      return
    }

    // Append user message immediately
    messages.value.push({
      type: 'user',
      text
    })

    disableInput.value = true
    stopping.value = false

    const payload = {
      type: 'user',
      action: 'chat',
      text
    }

    stompClient.publish({
      destination: '/app/enhanced-dialog',
      body: JSON.stringify(payload)
    })
  }

  const stopAgent = () => {
    if (!stompClient || !isConnected.value) {
      console.warn('STOMP client not connected, cannot stop agent')
      return
    }

    stopping.value = true

    const payload = {
      type: 'user',
      action: 'stop',
      text: ''
    }

    stompClient.publish({
      destination: '/app/enhanced-dialog',
      body: JSON.stringify(payload)
    })
  }

  const openTracePanel = (traceId: string) => {
    activeTraceMsgId.value = traceId
    isRightPanelOpen.value = true
  }

  const clearMessages = () => {
    messages.value = []
  }

  return {
    isConnected,
    disableInput,
    stopping,
    messages,
    isRightPanelOpen,
    activeTraceMsgId,
    connect,
    disconnect,
    sendMessage,
    stopAgent,
    openTracePanel,
    clearMessages
  }
}
