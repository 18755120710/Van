package butvan.cn.runtime;

import butvan.cn.runtime.delegate.BrowserAgentTool;
import butvan.cn.session.MessageSession;
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

    private final BrowserAgentFactory browserAgentFactory;

    public Toolkit createForSession(MessageSession session) {
        Toolkit toolkit = new Toolkit();

        BrowserAgentTool browser_agent_tool = new BrowserAgentTool(
                browserAgentFactory,
                session
        );

        toolkit.registration()
                .tool(browser_agent_tool)
                .apply();

        return toolkit;
    }
}

