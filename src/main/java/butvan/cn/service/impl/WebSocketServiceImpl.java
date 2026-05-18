package butvan.cn.service.impl;

import butvan.cn.agent.browser.runtime.AgentExecutionHandle;
import butvan.cn.agent.browser.runtime.AgentExecutionRegistry;
import butvan.cn.agent.service.AgentRunService;
import butvan.cn.common.security.SessionIdSanitizer;
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


    @Override
    public void enhancedDialog(DialogMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {

        String session_id = SessionIdSanitizer.requireSafe(headerAccessor.getSessionId());

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

        ws_session.receiveMessage(message);

        String trace_id = UUID.randomUUID().toString();
        AgentExecutionHandle handle = new AgentExecutionHandle(trace_id);
        agentExecutionRegistry.register(session_id,handle);

        Future<?> future = agentTaskExecutor.submit(() -> {
            agentRunService.run(ws_session, trace_id, handle);
        });
        handle.setFuture(future);
    }
}
