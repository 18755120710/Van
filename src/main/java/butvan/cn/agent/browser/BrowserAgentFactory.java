package butvan.cn.agent.browser;


import butvan.cn.agent.prompt.PromptManagement;
import butvan.cn.agent.trace.AgentTraceHook;
import butvan.cn.agent.trace.TokenUsageRegistry;
import butvan.cn.agent.trace.TraceContextRegistry;
import butvan.cn.configcenter.service.AgentModelProvider;
import butvan.cn.properties.AgentScopeProperties;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrowserAgentFactory {

    //private final Model model;
    private final AgentModelProvider agentModelProvider;
    private final BrowserToolkitFactory browserToolkitFactory;

    private final PromptManagement promptManagement;

    private final AgentScopeProperties agentScopeProperties;

    private final TraceContextRegistry traceContextRegistry;
    private final TokenUsageRegistry tokenUsageRegistry;

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
                .model(agentModelProvider.curentModel())
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .maxIters(agentScopeProperties.getReAct().getMaxIters())
                .build();
    }

    public BrowserAgentRuntime createRuntime(MessageSession session, String traceId) {

        BrowserToolkitFactory.BrowserToolkitRuntime toolkitRuntime = browserToolkitFactory.createRuntimeForSession(session); // 创建工具和 PageSession
        String sys_prompt = browserSystemPrompt();

        ReActAgent agent = ReActAgent.builder() // 开始构造 BrowserAgent
                .name("BrowserAgent") // Agent 名称
                .sysPrompt(sys_prompt) // BrowserAgent 系统提示词
                .model(agentModelProvider.curentModel()) // 使用 Spring 注入的大模型
                .memory(new InMemoryMemory()) // 使用短期记忆
                .toolkit(toolkitRuntime.toolkit()) // 使用带浏览器工具的 Toolkit
                .hook(new AgentTraceHook(session,traceContextRegistry,tokenUsageRegistry))
                .maxIters(agentScopeProperties.getReAct().getMaxIters()) // 最大推理轮数
                .build(); // 完成构造

        return new BrowserAgentRuntime(agent, toolkitRuntime.pageSession()); // 返回 agent + pageSession
    }

    private String browserSystemPrompt() {
        String sysPrompt = promptManagement.getPrompt("promptBrowserSystem");

        if (sysPrompt == null || sysPrompt.isBlank()) {
            sysPrompt = """
                你是 BrowserAgent，专门负责浏览器操作。
                你可以打开网页、查看页面状态、输入文本、点击元素、滚动页面并提取内容。
                每次页面变化后，必须重新调用 view_current_page_status。
                最终返回结构化、可复用的页面信息，不要编造网页内容。
                """;
        }

        return sysPrompt;
    }
}
