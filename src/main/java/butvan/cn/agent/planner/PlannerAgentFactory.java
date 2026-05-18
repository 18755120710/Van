package butvan.cn.agent.planner;

import butvan.cn.agent.prompt.PromptManagement;
import butvan.cn.agent.trace.AgentTraceHook;
import butvan.cn.agent.trace.TraceContextRegistry;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.plan.PlanNotebook;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * PlannerAgentFactory 负责创建总规划 Agent。
 *
 * PlannerAgent 的职责：
 * 1. 理解用户总任务
 * 2. 拆分任务步骤
 * 3. 判断是否需要调用 BrowserAgent、ContentAgent、AmapAgent
 * 4. 汇总各专家 Agent 的结果
 *
 * PlannerAgent 不应该直接操作网页页面元素。
 */
@Component
@RequiredArgsConstructor
public class PlannerAgentFactory {

    private final Model model;
    private final PlannerToolkitFactory plannerToolkitFactory;
    private final PromptManagement promptManagement;
    private final TraceContextRegistry traceContextRegistry;

    public ReActAgent create(MessageSession messageSession, String traceId) {
        Toolkit toolkit = plannerToolkitFactory.createForSession(messageSession,traceId);

        PlanNotebook plan_note_book = PlanNotebook.builder()
                .maxSubtasks(12)
                .needUserConfirm(false)
                .build();
        String sys_prompt = promptManagement.getPrompt("promptPlanningSystem");

        if (sys_prompt == null || sys_prompt.isBlank()) {
            sys_prompt = """
                    你是 PlannerAgent，负责理解用户任务、拆分计划并委派给专家 Agent。
                    涉及网页浏览、搜索、点击、页面提取的任务，必须调用 use_browser_agent。
                    不要自己编造网页内容，必须基于专家 Agent 返回的信息回答。
                    """;
        }

        return ReActAgent.builder()
                .name("PlannerAgent")
                .sysPrompt(sys_prompt)
                .model(model)
                .memory(new InMemoryMemory())
                .toolkit(toolkit)
                .planNotebook(plan_note_book)
                .hook(new AgentTraceHook(messageSession, traceContextRegistry))
                .maxIters(30)
                .build();
    }
}
