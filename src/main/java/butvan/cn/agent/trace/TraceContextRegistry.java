package butvan.cn.agent.trace;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TraceContextRegistry {

    /**
     * sessionId -> 当前正在执行的 traceId
     *
     * 为什么用 sessionId？
     * 因为你的 BrowserAgentRuntime 是按 sessionId 复用的。
     * 同一个 WebSocket session 同一时间通常只跑一个 Agent 任务。
     */
    private final Map<String, String> currentTraceIds = new ConcurrentHashMap<>();

    /**
     * 设置当前 session 正在执行的 traceId。
     *
     * 每次用户发起新问题时，在 AgentRunService 开始执行前调用。
     */
    public void setCurrentTraceId(String sessionId, String traceId) {
        currentTraceIds.put(sessionId, traceId);
    }

    /**
     * 获取当前 session 对应的 traceId。
     *
     * AgentTraceHook 触发时调用这个方法，
     * 这样即使 BrowserAgentRuntime 被复用，也能拿到最新 traceId。
     */
    public String getCurrentTraceId(String sessionId) {
        return currentTraceIds.get(sessionId);
    }

    /**
     * 当前任务结束后清理。
     *
     * 避免 session 长时间保留旧 traceId。
     */
    public void clear(String sessionId) {
        currentTraceIds.remove(sessionId);
    }
}