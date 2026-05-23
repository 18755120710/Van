package butvan.cn.agent.chat;

import butvan.cn.agent.trace.AgentTraceHook;
import butvan.cn.agent.trace.TokenUsageRegistry;
import butvan.cn.agent.trace.TraceContextRegistry;
import butvan.cn.configcenter.service.AgentModelProvider;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * create simple agent
 */
@Component
@RequiredArgsConstructor
public class SimpleChatAgentFactory {

    private final AgentModelProvider agentModelProvider;
    private final TraceContextRegistry traceContextRegistry;
    private final TokenUsageRegistry tokenUsageRegistry;

    public ReActAgent create(MessageSession session) {
        String prompt = """
                你是一个简洁、友好的中文助手。
                直接回答用户问题。
                如果用户需要搜索、浏览网页、获取实时信息或执行复杂任务，请说明需要切换到规划能力处理。
                """;

        return ReActAgent.builder()
                .name("SimpleChatAgent")
                .sysPrompt(prompt)
                .model(agentModelProvider.curentModel())
                .memory(new InMemoryMemory())
                .maxIters(3)
                .hook(new AgentTraceHook(session, traceContextRegistry, tokenUsageRegistry))
                .build();
    }
}
