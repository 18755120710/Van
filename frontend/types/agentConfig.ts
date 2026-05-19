export interface PromptInfo {
  key: string
  filename: string
  name: string
  source: 'default' | 'custom'
  updatedAt?: number | null
}

export interface PromptContent {
  key: string
  filename: string
  content: string
  source: 'default' | 'custom'
  updatedAt?: number | null
}

export interface AgentModelConfigView {
  provider: string
  baseUrl: string
  modelName: string
  apiKeyMasked: string
  stream: boolean
  temperature: number
  maxTokens: number
  updatedAt?: number | null
}

export interface UpdateModelConfigRequest {
  provider: string
  baseUrl: string
  modelName: string
  apiKey?: string | null
  stream: boolean
  temperature: number
  maxTokens: number
}
