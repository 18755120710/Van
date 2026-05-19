export interface ToolResult {
  eventType: string;
  agentName?: string;
  toolName?: string;
  text: string;
  timestamp: string;
}

export interface Message {
  type: 'user' | 'server';
  traceId?: string;
  text: string;
  imageUrl?: string;
  fileUrl?: string;
  openUrl?: string;
  streaming?: boolean;
  toolResults?: ToolResult[];
  showTools?: boolean;
  isError?: boolean;
  stopped?: boolean;
}

export interface DialogMessageDTO {
  type: 'user' | 'server';
  text?: string;
  imageUrl?: string;
  fileUrl?: string;
  openUrl?: string;
  traceId?: string;
  eventType?: string;
  agentName?: string;
  toolName?: string;
  trace?: boolean;
  done?: boolean;
  meta?: {
    serverStatusHint?: number;
  };
}

export interface ConversationMeta {
  conversationId: string;
  title: string;
  titleGenerated: boolean;
  createAt: number;
  updateAt: number;
  messageCount: number;
  deleted: boolean;
}

export interface UiMessage {
  id: string;
  conversationId: string;
  type: 'user' | 'server';
  traceId?: string;
  text?: string;
  imageUrl?: string;
  fileUrl?: string;
  openUrl?: string;
  createAt: number;
}
