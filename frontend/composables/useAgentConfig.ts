import type { PromptInfo, PromptContent } from '~/types/agentConfig'

export const useAgentConfig = () => {
  const BASE_URL = 'http://localhost:18081/agent-config'

  const listPrompts = async (): Promise<PromptInfo[]> => {
    const res = await fetch(`${BASE_URL}/prompts`)
    if (!res.ok) {
      throw new Error(`获取 Prompt 列表失败: ${res.statusText}`)
    }
    return await res.json()
  }

  const getPromptContent = async (key: string): Promise<PromptContent> => {
    const res = await fetch(`${BASE_URL}/prompts/${encodeURIComponent(key)}`)
    if (!res.ok) {
      throw new Error(`获取 Prompt 内容失败: ${res.statusText}`)
    }
    return await res.json()
  }

  const savePrompt = async (key: string, content: string): Promise<PromptContent> => {
    const res = await fetch(`${BASE_URL}/prompts/${encodeURIComponent(key)}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ content })
    })
    if (!res.ok) {
      throw new Error(`保存 Prompt 失败: ${res.statusText}`)
    }
    return await res.json()
  }

  const resetPrompt = async (key: string): Promise<PromptContent> => {
    const res = await fetch(`${BASE_URL}/prompts/${encodeURIComponent(key)}/reset`, {
      method: 'POST'
    })
    if (!res.ok) {
      throw new Error(`重置 Prompt 失败: ${res.statusText}`)
    }
    return await res.json()
  }

  return {
    listPrompts,
    getPromptContent,
    savePrompt,
    resetPrompt
  }
}
