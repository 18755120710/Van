import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import type { Message, DialogMessageDTO, ConversationMeta, UiMessage } from '~/types/chat'

// Global singleton state so that WS state persists when switching pages/components
const isConnected = ref(false)
const disableInput = ref(false)
const stopping = ref(false)
const messages = ref<Message[]>([])
const isRightPanelOpen = ref(false)
const activeTraceMsgId = ref<string | null>(null)
const activeConversationId = ref<string | null>(null)
const conversations = ref<ConversationMeta[]>([])

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

      // 使用 100% 确定的 Message 引用，保证 TypeScript 严格模式类型安全
      const msg: Message = existingMsg

      // 更新可能存在的图片、文件、URL
      if (dto.imageUrl) {
        msg.imageUrl = dto.imageUrl
      }
      if (dto.fileUrl) {
        msg.fileUrl = dto.fileUrl
      }
      if (dto.openUrl) {
        msg.openUrl = dto.openUrl
      }

      // 2. 处理 eventType === "answer_delta" 流式响应片段
      if (dto.eventType === 'answer_delta') {
        msg.text = (msg.text || '') + (dto.text || '')
        msg.streaming = true
      }

      // 3. 处理 eventType === "answer" 最终回答信号
      else if (dto.eventType === 'answer') {
        if (dto.text) {
          if (!msg.text) {
            msg.text = dto.text
          }
        }
        msg.streaming = false
        if (dto.done === true) {
          disableInput.value = false
          stopping.value = false
        }
      }

      // 4. 处理 dto.trace === true 且 eventType !== "error" 的执行轨迹消息
      else if (dto.trace === true && dto.eventType !== 'error') {
        msg.toolResults = msg.toolResults || []
        msg.toolResults.push({
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
          msg.streaming = false
          disableInput.value = false
          stopping.value = false
        }
      }

      // 5. 处理 eventType === "error" 错误轨迹，且避免重复追加
      else if (dto.eventType === 'error') {
        msg.toolResults = msg.toolResults || []
        const isAlreadyAdded = msg.toolResults.some(
          t => t.eventType === 'error' && t.text === dto.text
        )
        if (!isAlreadyAdded) {
          msg.toolResults.push({
            eventType: dto.eventType,
            agentName: dto.agentName,
            toolName: dto.toolName,
            text: dto.text || '',
            timestamp: getFormattedTime()
          })
        }
        activeTraceMsgId.value = dto.traceId
        isRightPanelOpen.value = true
        msg.isError = true
        msg.streaming = false
        disableInput.value = false
        stopping.value = false
      }

      // 6. 如果 dto.done === true，则更新状态并恢复输入框
      if (dto.done === true) {
        msg.streaming = false
        disableInput.value = false
        stopping.value = false
      }
    } else {
      // 7. 保留旧消息兼容逻辑：如果没有 traceId，但有 text，按原来的方式 push 到 messages
      if (dto.text) {
        messages.value.push({
          type: dto.type || 'server',
          text: dto.text || '',
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
    if (import.meta.server) return
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

  const BASE_URL = ''

  const loadConversations = async () => {
    try {
      const res = await fetch(`${BASE_URL}/conversations`)
      if (res.ok) {
        conversations.value = await res.json()
      }
    } catch (e) {
      console.error('Failed to load conversations:', e)
    }
  }

  const loadMessages = async (conversationId: string) => {
    try {
      const res = await fetch(`${BASE_URL}/conversations/${conversationId}/messages`)
      if (res.ok) {
        const data: UiMessage[] = await res.json()
        messages.value = data.map((msg: UiMessage) => ({
          type: msg.type,
          text: msg.text || '',
          traceId: msg.traceId,
          imageUrl: msg.imageUrl,
          fileUrl: msg.fileUrl,
          openUrl: msg.openUrl,
          streaming: false
        }))
      }
    } catch (e) {
      console.error(`Failed to load messages for conversation ${conversationId}:`, e)
    }
  }

  const initSession = async () => {
    if (import.meta.server) return
    let storedId = localStorage.getItem('activeConversationId')

    // Filter out potential invalid string representations of null or undefined
    if (storedId === 'null' || storedId === 'undefined') {
      storedId = null
    }

    if (storedId) {
      activeConversationId.value = storedId
    } else {
      try {
        const res = await fetch(`${BASE_URL}/conversations`, {
          method: 'POST'
        })
        if (res.ok) {
          const data = await res.json()
          storedId = data.conversationId
          if (storedId) {
            activeConversationId.value = storedId
            localStorage.setItem('activeConversationId', storedId)
          }
        }
      } catch (e) {
        console.error('Failed to create new conversation:', e)
      }
    }

    // Refresh conversation list
    await loadConversations()

    // Fetch messages for active conversation
    if (activeConversationId.value) {
      await loadMessages(activeConversationId.value)
    }
  }

  const createNewConversation = async () => {
    try {
      const res = await fetch(`${BASE_URL}/conversations`, {
        method: 'POST'
      })
      if (res.ok) {
        const data = await res.json()
        const newId = data.conversationId
        if (newId) {
          activeConversationId.value = newId
          localStorage.setItem('activeConversationId', newId)
          messages.value = []
          await loadConversations()
        }
      }
    } catch (e) {
      console.error('Failed to create new conversation:', e)
    }
  }

  const switchConversation = async (conversationId: string) => {
    activeConversationId.value = conversationId
    localStorage.setItem('activeConversationId', conversationId)
    await loadMessages(conversationId)
  }

  const sendMessage = async (text: string) => {
    if (!stompClient || !isConnected.value) {
      console.warn('STOMP client not connected, cannot send message')
      return
    }

    // Double check that activeConversationId is not null. If it is null, create it immediately.
    if (!activeConversationId.value) {
      console.warn('activeConversationId is null in sendMessage, attempting to create one now...')
      try {
        const res = await fetch(`${BASE_URL}/conversations`, {
          method: 'POST'
        })
        if (res.ok) {
          const data = await res.json()
          const newId = data.conversationId
          if (newId) {
            activeConversationId.value = newId
            localStorage.setItem('activeConversationId', newId)
            await loadConversations()
          }
        }
      } catch (e) {
        console.error('Failed to auto-create conversation in sendMessage:', e)
      }
    }

    // If still null, report error and return to prevent sending null payload
    if (!activeConversationId.value) {
      console.error('Aborting sendMessage: activeConversationId is still null.')
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
      conversationId: activeConversationId.value,
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
      conversationId: activeConversationId.value,
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
    activeConversationId,
    conversations,
    connect,
    disconnect,
    sendMessage,
    stopAgent,
    openTracePanel,
    clearMessages,
    loadConversations,
    loadMessages,
    initSession,
    createNewConversation,
    switchConversation
  }
}
