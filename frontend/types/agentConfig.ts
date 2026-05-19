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
