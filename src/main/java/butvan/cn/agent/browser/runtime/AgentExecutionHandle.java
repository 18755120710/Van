package butvan.cn.agent.browser.runtime;

import butvan.cn.agent.browser.BrowserAgentRuntime;
import io.agentscope.core.ReActAgent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.Disposable;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AgentExecutionHandle 表示“一次正在运行的 Agent 执行任务”。
 *
 * 为什么需要这个类？
 *
 * 用户点击“停止”时，后端需要知道：
 * 1. 当前 session 正在运行哪个 PlannerAgent
 * 2. 当前是否有 BrowserAgent 正在执行浏览器任务
 * 3. 当前 Reactor stream 是否还在向前端推送
 * 4. 当前线程池任务是否还在运行
 * 5. 用户是否已经请求停止
 *
 * 这些信息如果散落在 AgentRunService、BrowserAgentTool、Registry 里，
 * 后续会非常难管理。
 *
 * 所以我们用 AgentExecutionHandle 把“一次执行”的可停止资源集中保存起来。
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AgentExecutionHandle {


    // 用户问题对应一次执行ID
    private final String traceId;
    // 正在运行的 planner agent
    private ReActAgent plannerAgent;
    // 正在运行或者复用的 browser agent runtime
    private BrowserAgentRuntime browserAgentRuntime;
    // reactor stream 订阅句柄
    private Disposable streamDisposable;
    // 线程池任务句柄
    private Future<?> future;
    // 是否已经停止请求
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);

}
