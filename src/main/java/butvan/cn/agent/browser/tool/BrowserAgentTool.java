package butvan.cn.agent.browser.tool;

import butvan.cn.agent.browser.BrowserAgentRuntime;
import butvan.cn.agent.browser.BrowserAgentSessionRegistry;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BrowserAgentTool {

    private final BrowserAgentSessionRegistry browserAgentSessionRegistry;
    private final MessageSession session;

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
        // 根据当前 session 获取当前 agent
        BrowserAgentRuntime runtime_agent = browserAgentSessionRegistry.getOrCreate(session);
        // 从 runtime 中拿出 agent
        ReActAgent browser_agent = runtime_agent.agent();

        try {
            Msg result = browser_agent.call(
                    Msg.builder()
                            .textContent(task)
                            .build()
            ).block();

            if (result != null && result.getTextContent() != null) {
                return result.getTextContent();
            }

            return "BrowserAgent 执行完成，但没有返回内容";
        } catch (Exception e) {
            return "BrowserAgent 执行失败：" + e.getMessage();
        }

    }
}
