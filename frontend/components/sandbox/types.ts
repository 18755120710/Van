import type { ToolResult } from '~/types/chat'

export type SandboxRole = 'pm' | 'planner' | 'browser' | 'qa' | 'ops'

export type SandboxPhase =
  | 'idle'
  | 'planning'
  | 'handoff'
  | 'working'
  | 'reporting'
  | 'summary'
  | 'answer'
  | 'usage'
  | 'error'
  | 'stopped'

export interface SandboxDialogue {
  role: SandboxRole
  text: string
}

export interface MinecraftSandboxState {
  phase: SandboxPhase
  activeRole: SandboxRole
  dialogue: SandboxDialogue
  storyText: string
  latestEvent?: ToolResult
  activeToolName?: string
}

export interface ScreenPoint {
  x: number
  y: number
  visible: boolean
}

export interface RoleAnchorMap {
  pm: ScreenPoint
  planner: ScreenPoint
  browser: ScreenPoint
  qa: ScreenPoint
  ops: ScreenPoint
}
