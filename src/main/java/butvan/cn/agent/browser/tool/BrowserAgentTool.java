package butvan.cn.agent.browser.tool;

import butvan.cn.agent.browser.BrowserAgentFactory;
import butvan.cn.agent.browser.BrowserPageSessionRegistry;
import butvan.cn.agent.browser.runtime.AgentExecutionHandle;
import butvan.cn.agent.browser.runtime.AgentExecutionRegistry;
import butvan.cn.agent.browser.runtime.PageSession;
import butvan.cn.websocket.dto.DialogMessageDTO;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BrowserAgentTool {

    private final BrowserPageSessionRegistry browserPageSessionRegistry;
    private final BrowserAgentFactory browserAgentFactory;
    private final MessageSession session;
    private final String traceId;
    private final AgentExecutionRegistry agentExecutionRegistry;

    @Tool(
            name = "use_browser_agent",
            description = "将网页搜索、页面浏览、点击、滚动、内容提取等任务委派给 BrowserAgent 执行。"
    )
    public String useBrowserAgent(
            @ToolParam(
                    name = "task",
                    description = "要交给 BrowserAgent 执行的具体浏览器任务"
            )
            String task)
    {

        if (isStopRequested()) {
            return "BrowserAgent 执行已被用户停止。";
        }

        /**
         * BrowserAgent 调用前，先通知前端。
         *
         * 这样用户能立刻看到：
         * “PlannerAgent 正在委派 BrowserAgent”
         *
         * 这条消息属于过程消息，所以 trace=true。
         */
        session.sendMessage(DialogMessageDTO.builder()
                .type(DialogMessageDTO.TYPE_SERVER)
                .traceId(traceId)
                .eventType("agent_call")
                .agentName("PlannerAgent")
                .toolName("use_browser_agent")
                .trace(true)
                .done(false)
                .text("PlannerAgent 正在委派 BrowserAgent：" + task)
                .build());

        try {

            // 获取当前 session 对应的 pageSession
            PageSession page_session = browserPageSessionRegistry.getOrCreate(session);
            // 每次委派任务都创建一个新的 browserAgent
            ReActAgent browser_agent = browserAgentFactory.createWithPageSession(session, page_session);

            // 把 browser agent runtime 注册到当前执行句柄中
            AgentExecutionHandle handle = agentExecutionRegistry.get(session.getSessionId());
            if (handle != null) {
                handle.setBrowserAgent(browser_agent);
            }

            Msg result = browser_agent.call(
                    Msg.builder()
                            .textContent(task)
                            .build()
            ).block();

            if (isStopRequested()) {
                return "BrowserAgent 执行已被用户停止。";
            }

            /**
             * BrowserAgent 执行完成后，通知前端。
             */
            session.sendMessage(DialogMessageDTO.builder()
                    .type(DialogMessageDTO.TYPE_SERVER)
                    .traceId(traceId)
                    .eventType("agent_call")
                    .agentName("BrowserAgent")
                    .toolName("use_browser_agent")
                    .trace(true)
                    .done(false)
                    .text("BrowserAgent 执行完成，结果已返回 PlannerAgent。")
                    .build());

            if (result != null && result.getTextContent() != null) {
                return result.getTextContent();
            }

            return "BrowserAgent 执行完成，但没有返回内容";
        } catch (Exception e) {

            if (!isStopRequested()) {
                /**
                 * 异常时也要通知前端。
                 *
                 * 否则用户可能只看到一直 loading。
                 */
                session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .traceId(traceId)
                        .eventType("error")
                        .agentName("BrowserAgent")
                        .toolName("use_browser_agent")
                        .trace(true)
                        .done(false)
                        .text("BrowserAgent 执行失败：" + e.getMessage())
                        .build());
            }

            return "BrowserAgent 执行失败：" + e.getMessage();
        } finally {
            AgentExecutionHandle handle = agentExecutionRegistry.get(session.getSessionId());
            if (handle != null) {
                handle.setBrowserAgent(null);
            }
        }
    }

    /**
     * 判断当前执行是否已经被用户请求停止。
     */
    private boolean isStopRequested() {
        AgentExecutionHandle handle = agentExecutionRegistry.get(session.getSessionId());
        return handle != null && handle.getStopRequested().get();
    }
}
