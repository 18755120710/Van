package butvan.cn.agent.planner;

import butvan.cn.agent.browser.BrowserAgentSessionRegistry;
import butvan.cn.agent.browser.tool.BrowserAgentTool;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PlannerToolkitFactory 只负责创建 PlannerAgent 的工具集合。
 *
 * PlannerAgent 的工具应该是“委派专家 Agent 的工具”。
 */
@Component
@RequiredArgsConstructor
public class PlannerToolkitFactory {

    private final BrowserAgentSessionRegistry browserAgentSessionRegistry;

    public Toolkit createForSession(MessageSession session) {
        Toolkit toolkit = new Toolkit();

        BrowserAgentTool browser_agent_tool = new BrowserAgentTool(
                browserAgentSessionRegistry,
                session
        );

        toolkit.registration()
                .tool(browser_agent_tool)
                .apply();

        return toolkit;
    }
}
