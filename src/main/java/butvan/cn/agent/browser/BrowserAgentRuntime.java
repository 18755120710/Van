package butvan.cn.agent.browser;

import butvan.cn.agent.browser.runtime.PageSession;
import io.agentscope.core.ReActAgent;

public record BrowserAgentRuntime(
        ReActAgent agent,
        PageSession pageSession
) {
    public void close() {
        pageSession.close();
    }
}
