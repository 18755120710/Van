package butvan.cn.runtime.delegate;

import butvan.cn.runtime.BrowserAgentFactory;
import butvan.cn.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

public class BrowserAgentTool {

    private final BrowserAgentFactory browserAgentFactory;

    private final MessageSession session;

    public BrowserAgentTool(
            BrowserAgentFactory browserAgentFactory,
            MessageSession session
    ) {
        this.browserAgentFactory = browserAgentFactory;
        this.session = session;
    }

    @Tool(
            name = "use_browser_agent",
            description = "将网页搜索、页面浏览、点击、滚动、内容提取等任务委派给 BrowserAgent 执行。"
    )
    public String useBrowserAgent(
            @ToolParam(
                    name = "task",
                    description = "要交给 BrowserAgent 执行的具体浏览器任务"
            )
            String task) {

        ReActAgent browser_agent = browserAgentFactory.create(session);

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
