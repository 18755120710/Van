import type { PromptInfo, PromptContent, AgentModelConfigView, UpdateModelConfigRequest } from '~/types/agentConfig'

export const useAgentConfig = () => {
  const BASE_URL = '/agent-config'

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

  const getModelConfig = async (): Promise<AgentModelConfigView> => {
    const res = await fetch(`${BASE_URL}/model`)
    if (!res.ok) {
      throw new Error(`获取模型配置失败: ${res.statusText}`)
    }
    return await res.json()
  }

  const updateModelConfig = async (payload: UpdateModelConfigRequest): Promise<AgentModelConfigView> => {
    const res = await fetch(`${BASE_URL}/model`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })
    if (!res.ok) {
      throw new Error(`更新模型配置失败: ${res.statusText}`)
    }
    return await res.json()
  }

  return {
    listPrompts,
    getPromptContent,
    savePrompt,
    resetPrompt,
    getModelConfig,
    updateModelConfig
  }
}
