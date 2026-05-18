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

    public BrowserAgentRuntime getOrCreate(MessageSession session,String traceId) {
        return agents.computeIfAbsent(
                session.getSessionId(),
                id -> browserAgentFactory.createRuntime(session,traceId)
        );
    }

    public void remove(String sessionId) {
        BrowserAgentRuntime runtime = agents.remove(sessionId);

        if (runtime != null) {
            runtime.close();
        }
    }

    /**
     * 只从缓存中移除 BrowserAgentRuntime，不主动 close。
     *
     * 使用场景：
     * AgentExecutionRegistry.stop(...) 已经手动关闭了 runtime，
     * 这里只需要删除缓存，避免下次复用。
     */
    public void evict(String sessionId) {
        agents.remove(sessionId);
    }
}
