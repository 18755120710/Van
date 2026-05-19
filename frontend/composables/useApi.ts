import type { ConversationMeta, UiMessage } from '~/types/chat'

export const useApi = () => {
  // 1. 创建具备请求和响应拦截器的基础 $fetch 客户端
  const customFetch = $fetch.create({
    baseURL: '', // 走 Nuxt devProxy 本地开发反向代理，物理性避免跨域
    
    // 请求拦截器
    onRequest({ request, options }) {
      options.headers = options.headers || {}
      console.log(`🚀 [API Request Interceptor] ${options.method || 'GET'} -> ${request}`)
    },
    
    onRequestError({ error }) {
      console.error('❌ [API Request Error Interceptor]', error)
    },
    
    // 响应拦截器
    onResponse({ response }) {
      console.log(`✅ [API Response Interceptor] Status: ${response.status}`)
    },
    
    onResponseError({ response, error }) {
      console.error(`💥 [API Response Error Interceptor] Status: ${response?.status}`, error)
    }
  })

  // 2. 统一将所有请求方法封装在 api 模块命名空间下
  const conversationsApi = {
    // 获取历史会话列表
    list: () => {
      return customFetch<ConversationMeta[]>('/conversations')
    },
    
    // 创建新会话
    create: () => {
      return customFetch<ConversationMeta>('/conversations', { 
        method: 'POST' 
      })
    },
    
    // 查询某个会话的历史消息
    listMessages: (conversationId: string) => {
      return customFetch<UiMessage[]>(`/conversations/${conversationId}/messages`)
    }
  }

  return {
    conversationsApi
  }
}
