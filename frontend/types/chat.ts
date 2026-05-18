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
