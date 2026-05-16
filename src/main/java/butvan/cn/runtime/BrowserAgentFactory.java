package butvan.cn.runtime;


import butvan.cn.agent.prompt.PromptManagement;
import butvan.cn.properties.AgentScopeProperties;
import butvan.cn.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrowserAgentFactory {

    private final Model model;

    private final BrowserToolkitFactory browserToolkitFactory;

    private final PromptManagement promptManagement;

    private final AgentScopeProperties agentScopeProperties;

    public ReActAgent create(MessageSession session) {
        Toolkit toolkit = browserToolkitFactory.createForSession(session);

        String sys_prompt = promptManagement.getPrompt("promptBrowserSystem");

        if (sys_prompt == null || sys_prompt.isBlank()) {
            sys_prompt = """
                    你是 BrowserAgent，专门负责浏览器操作。
                    你可以打开网页、查看页面状态、输入文本、点击元素、滚动页面并提取内容。
                    每次页面变化后，必须重新调用 view_current_page_status。
                    最终返回结构化、可复用的页面信息，不要编造网页内容。
                    """;
        }

        return ReActAgent.builder()
                .name("BrowserAgent")
                .sysPrompt(sys_prompt)
                .model(model)
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .maxIters(agentScopeProperties.getReAct().getMaxIters())
                .build();
    }
}
