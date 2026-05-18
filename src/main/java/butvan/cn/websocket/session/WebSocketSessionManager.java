package butvan.cn.websocket.session;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, MessageSession> sessionMap =
            new ConcurrentHashMap<>();

    public MessageSession getSession(String sessionId) {
        return sessionMap.computeIfAbsent(
                sessionId,
                id -> new WsMessageSession(
                        () -> id,
                        "/queue/dialog",
                        messagingTemplate
                )
        );
    }

    public void clean(String sessionId) {
        sessionMap.remove(sessionId);
    }
}
