package butvan.cn.agent.planner;

import butvan.cn.agent.browser.BrowserAgentFactory;
import butvan.cn.agent.browser.BrowserPageSessionRegistry;
import butvan.cn.agent.browser.runtime.AgentExecutionRegistry;
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

    private final AgentExecutionRegistry agentExecutionRegistry;
    private final BrowserPageSessionRegistry browserPageSessionRegistry;
    private final BrowserAgentFactory browserAgentFactory;

    public Toolkit createForSession(MessageSession session, String traceId) {
        Toolkit toolkit = new Toolkit();

        BrowserAgentTool browser_agent_tool = new BrowserAgentTool(
                browserPageSessionRegistry,
                browserAgentFactory,
                session,
                traceId,
                agentExecutionRegistry
        );

        toolkit.registration()
                .tool(browser_agent_tool)
                .apply();

        return toolkit;
    }
}
