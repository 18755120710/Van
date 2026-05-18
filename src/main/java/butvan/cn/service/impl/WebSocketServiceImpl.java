package butvan.cn.service.impl;

import butvan.cn.agent.service.AgentRunService;
import butvan.cn.service.WebSocketService;
import butvan.cn.websocket.dto.DialogMessageDTO;
import butvan.cn.websocket.session.WebSocketSessionManager;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService
{

    private final WebSocketSessionManager sessionManager;

    private final AgentRunService agentRunService;

    private final TaskExecutor agentTaskExecutor;// 注入 agent 专用线程数


    @Override
    public void enhancedDialog(DialogMessageDTO message, SimpMessageHeaderAccessor headerAccessor) {
        if (StrUtil.isEmpty(message.getText())) {
            log.info("receive message empty!!!");
            return;
        }

        // 获取会话id
        var sessionId = headerAccessor.getSessionId();
        // 根据会话id获取对话对象
        var wsSession = this.sessionManager.getSession(sessionId);
        // 将消息对象写入到会话中
        wsSession.receiveMessage(message);

        // 异步调用大模型，并将回复信息发送给客户端
        agentTaskExecutor.execute(() -> {
            agentRunService.run(wsSession);
        });

    }
}
