package butvan.cn.agent.browser;

import butvan.cn.websocket.session.MessageSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BrowserAgentSessionRegistry {

    private final BrowserAgentFactory browserAgentFactory;
    private final Map<String, BrowserAgentRuntime> agents = new ConcurrentHashMap<>();

    public BrowserAgentRuntime getOrCreate(MessageSession session) {
        return agents.computeIfAbsent(
                session.getSessionId(),
                id -> browserAgentFactory.createRuntime(session)
        );
    }

    public void remove(String sessionId) {
        BrowserAgentRuntime runtime = agents.remove(sessionId);

        if (runtime != null) {
            runtime.close();
        }
    }
}
