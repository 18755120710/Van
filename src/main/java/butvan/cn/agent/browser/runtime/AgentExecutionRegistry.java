package butvan.cn.agent.browser.runtime;

import butvan.cn.agent.browser.BrowserAgentSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * AgentExecutionRegistry 负责管理当前正在运行的 Agent 执行任务。
 *
 * 这个类是“停止功能”的核心注册表。
 *
 * 当前项目里，一个浏览器 WebSocket session 对应一个用户会话。
 * 所以这里用 sessionId 作为 key：
 *
 * sessionId -> AgentExecutionHandle
 *
 * 用户点击停止时：
 * 1. 前端发送 action=stop
 * 2. 后端通过 sessionId 找到 AgentExecutionHandle
 * 3. 调用 stop(sessionId)
 * 4. stop 内部会 interrupt agent、dispose stream、cancel future、close browser runtime
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentExecutionRegistry {

    // 保存当前正在运行的执行任务
    private final Map<String, AgentExecutionHandle> executions = new ConcurrentHashMap<>();

    private final BrowserAgentSessionRegistry browserAgentSessionRegistry;
    /**
     * 注册一次新的 agent 执行
     * @param sessionId
     * @param handle
     */
    public void register(String sessionId, AgentExecutionHandle handle) {
        executions.put(sessionId,handle);
    }

    /**
     * 获取当前 session 的执行句柄
     * @param sessionId
     * @return
     */
    public AgentExecutionHandle get(String sessionId) {
        return executions.get(sessionId);
    }

    /**
     * 判断当前 session 是否已有agent执行
     * @param sessionId
     * @return
     */
    public boolean isRunning(String sessionId) {
        return executions.containsKey(sessionId);
    }

    /**
     * 清理当前 session 执行句柄
     * @param sessionId
     */
    public void remove(String sessionId) {
        executions.remove(sessionId);
    }

    /**
     * 停止当前正在运行的 agent
     * @param sessionId
     * @return true 找到了正在运行的 agent 并发出了停止请求
     */
    public boolean stop(String sessionId) {
        AgentExecutionHandle handle = executions.get(sessionId);

        if (handle == null) {
            return false;
        }

        // 先设置停止标记
        handle.getStopRequested().set(true);

        // 请求 PlannerAgent 停止
        if (handle.getPlannerAgent() != null) {
            try {
                handle.getPlannerAgent().interrupt();
            } catch (Exception e) {
                log.warn("interrupt PlannerAgent failed, sessionId={}",sessionId,e);
            }
        }

        // 请求 BrowserAgent 停止
        if (handle.getBrowserAgentRuntime() != null) {
            try {
                handle.getBrowserAgentRuntime().agent().interrupt();
            } catch (Exception e) {
                log.warn("interrupt BrowserAgent failed, sessionId={}", sessionId, e);
            }

            try {
                handle.getBrowserAgentRuntime().close();
            } catch (Exception e) {
                log.warn("close BrowserAgentRuntime failed, sessionId={}", sessionId, e);
            }

            try {
                browserAgentSessionRegistry.evict(sessionId);
            } catch (Exception e) {
                log.warn("remove BrowserAgentRuntime cache failed, sessionId={}", sessionId, e);
            }
        }

        // 停止 Reactor stream 继续推送事件
        if(handle.getStreamDisposable() != null) {
            try {
                handle.getStreamDisposable().dispose();
            } catch (Exception e) {
                log.warn("dispose agent stream failed, sessionId={}", sessionId, e);
            }
        }

        // 请求中断线程池任务
        if (handle.getFuture() != null) {
            try {
                handle.getFuture().cancel(true);
            } catch (Exception e) {
                log.warn("cancel agent future failed, sessionId={}", sessionId, e);
            }
        }

        return true;
    }
}
