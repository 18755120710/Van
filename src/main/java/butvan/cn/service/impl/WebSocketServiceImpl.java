package butvan.cn.service.impl;

import butvan.cn.agent.browser.runtime.AgentExecutionHandle;
import butvan.cn.agent.browser.runtime.AgentExecutionRegistry;
import butvan.cn.agent.service.AgentRunService;
import butvan.cn.common.security.SessionIdSanitizer;
import butvan.cn.conversation.model.UiMessage;
import butvan.cn.conversation.service.ChatHistoryService;
import butvan.cn.conversation.service.ConversationService;
import butvan.cn.service.WebSocketService;
import butvan.cn.websocket.dto.DialogMessageDTO;
import butvan.cn.websocket.session.MessageSession;
import butvan.cn.websocket.session.WebSocketSessionManager;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService
{

    private final WebSocketSessionManager sessionManager;

    private final AgentRunService agentRunService;

    private final ThreadPoolTaskExecutor agentTaskExecutor;// 注入 agent 专用线程数

    private final AgentExecutionRegistry agentExecutionRegistry;

    private final ChatHistoryService chatHistoryService;

    private final ConversationService conversationService;

    @Override
    public void enhancedDialog(DialogMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {

        String session_id = SessionIdSanitizer.requireSafe(headerAccessor.getSessionId());

        // 业务会话 id
        String conversation_id = message.getConversationId();

        MessageSession ws_session = this.sessionManager.getSession(session_id);

        if ("stop".equalsIgnoreCase(message.getAction())) {
            agentRunService.stop(ws_session);
            return;
        }

        if (StrUtil.isEmpty(message.getText())) {
            log.warn("receive message empty!");
            return;
        }

        if (agentExecutionRegistry.isRunning(session_id)) {
            ws_session.sendMessage(DialogMessageDTO.builder()
                    .type(DialogMessageDTO.TYPE_SERVER)
                    .eventType("error")
                    .agentName("System")
                    .trace(true)
                    .done(false)
                    .text("当前 Agent 正在执行，请先停止或等待完成。")
                    .build());
            return;
        }

        // 用户第一次发送消息的时候，初始化设置标题
        conversationService.initTitleIfNecessary(conversation_id,message.getText());

        // 保存用户消息到 ui 历史
        chatHistoryService.appendMessage(conversation_id, UiMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .conversationId(conversation_id)
                        .type(DialogMessageDTO.TYPE_USER)
                        .text(message.getText())
                        .createAt(System.currentTimeMillis())
                .build());

        ws_session.receiveMessage(message);

        String trace_id = UUID.randomUUID().toString();
        AgentExecutionHandle handle = new AgentExecutionHandle(trace_id);
        agentExecutionRegistry.register(session_id,handle);

        Future<?> future = agentTaskExecutor.submit(() -> {
            agentRunService.run(ws_session, conversation_id ,trace_id, handle);
        });
        handle.setFuture(future);
    }
}
