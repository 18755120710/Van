import type { ToolResult } from '~/types/chat'
import type { MinecraftSandboxState, SandboxRole } from './types'

const clip = (value: string, max = 92) => {
  if (!value) return ''
  const normalized = value.replace(/\s+/g, ' ').trim()
  return normalized.length > max ? `${normalized.slice(0, max)}...` : normalized
}

const roleName: Record<SandboxRole, string> = {
  pm: '产品经理 PM',
  planner: '后端开发工程师 Planner',
  browser: '前端开发工程师 Browser',
  qa: '测试工程师 QA',
  ops: '运维工程师 Ops'
}

export const getSandboxRoleName = (role: SandboxRole) => roleName[role]

export const mapToolResultsToMinecraftState = (toolResults: ToolResult[]): MinecraftSandboxState => {
  const last = toolResults[toolResults.length - 1]

  if (!last) {
    return {
      phase: 'idle',
      activeRole: 'pm',
      dialogue: {
        role: 'pm',
        text: '等待新的探索需求。'
      },
      storyText: 'MC 协作办公室进入待机状态，PM 工位、Planner 工位和 Browser 双屏工位已准备就绪。'
    }
  }

  const eventType = last.eventType
  const text = last.text || ''
  const toolName = last.toolName || '未知工具'

  if (eventType === 'stopped') {
    return {
      phase: 'stopped',
      activeRole: 'ops',
      dialogue: {
        role: 'ops',
        text: '执行已停止，全部角色回到工位。'
      },
      storyText: 'Ops 控制台拉下停止闸，办公室灯光降为待机亮度，所有 Agent 暂停动作。',
      latestEvent: last
    }
  }

  if (eventType === 'plan' || eventType === 'status') {
    return {
      phase: 'planning',
      activeRole: 'planner',
      dialogue: {
        role: 'planner',
        text: clip(text || '正在把需求拆成可执行步骤。')
      },
      storyText: 'Planner 在白板前拆解任务，方块便利贴逐格亮起，形成新的执行路线。',
      latestEvent: last,
      activeToolName: toolName
    }
  }

  if (eventType === 'agent_call') {
    const browserFinished = /完成|返回|result/i.test(text)

    return {
      phase: browserFinished ? 'reporting' : 'handoff',
      activeRole: browserFinished ? 'browser' : 'planner',
      dialogue: {
        role: browserFinished ? 'browser' : 'planner',
        text: clip(text || (browserFinished ? '浏览器执行结果已返回。' : '把任务交给 Browser 工位执行。'))
      },
      storyText: browserFinished
        ? 'Browser 带着执行结果离开双屏工位，走向 Planner 桌边汇报。'
        : 'Planner 起身穿过方块走道，把子任务交到 Browser 双屏工位。',
      latestEvent: last,
      activeToolName: toolName
    }
  }

  if (eventType === 'tool_call') {
    return {
      phase: 'working',
      activeRole: 'browser',
      dialogue: {
        role: 'browser',
        text: `开始调用 ${toolName}。`
      },
      storyText: `Browser 坐回双屏电脑前，键盘和屏幕按像素节奏闪烁，正在执行工具 ${toolName}。`,
      latestEvent: last,
      activeToolName: toolName
    }
  }

  if (eventType === 'tool_result') {
    return {
      phase: 'reporting',
      activeRole: 'browser',
      dialogue: {
        role: 'browser',
        text: clip(text || '工具结果已经整理完成。')
      },
      storyText: 'Browser 把采集到的页面结果带回 Planner 工位，准备进入汇总判断。',
      latestEvent: last,
      activeToolName: toolName
    }
  }

  if (eventType === 'summary') {
    return {
      phase: 'summary',
      activeRole: 'planner',
      dialogue: {
        role: 'planner',
        text: clip(text || '正在汇总执行过程。')
      },
      storyText: 'Planner 回到主控桌，白板与笔记本同时亮起，开始整理最终摘要。',
      latestEvent: last,
      activeToolName: toolName
    }
  }

  if (eventType === 'answer') {
    return {
      phase: 'answer',
      activeRole: 'planner',
      dialogue: {
        role: 'planner',
        text: '最终报告已准备好。'
      },
      storyText: 'Planner 走向 PM 工位提交最终报告，本轮协作闭环完成。',
      latestEvent: last
    }
  }

  if (eventType === 'token_usage') {
    return {
      phase: 'usage',
      activeRole: 'ops',
      dialogue: {
        role: 'ops',
        text: clip(text || 'Token 消耗统计已记录。')
      },
      storyText: 'Ops 机柜和路由器亮起绿色状态灯，记录本轮模型调用消耗。',
      latestEvent: last
    }
  }

  if (eventType === 'error') {
    return {
      phase: 'error',
      activeRole: 'qa',
      dialogue: {
        role: 'qa',
        text: clip(text || '发现执行异常，正在定位。')
      },
      storyText: `QA 工位亮起红色故障提示，正在检查 ${toolName} 的异常输出。`,
      latestEvent: last,
      activeToolName: toolName
    }
  }

  return {
    phase: 'idle',
    activeRole: 'planner',
    dialogue: {
      role: 'planner',
      text: clip(text || '收到新的执行事件。')
    },
    storyText: 'MC 协作办公室记录到新的执行轨迹，等待下一步动作。',
    latestEvent: last,
    activeToolName: toolName
  }
}
