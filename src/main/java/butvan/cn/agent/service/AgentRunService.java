package butvan.cn.agent.service;

import butvan.cn.agent.planner.PlannerAgentFactory;
import butvan.cn.common.security.SessionIdSanitizer;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class AgentRunService {

    private final PlannerAgentFactory plannerAgentFactory;

    @Value("${memory.session-dir:./memory/sessions}")
    private String memorySessionDir;

    public void run(MessageSession session) {
        String task = session.readMessage();

        ReActAgent agent = plannerAgentFactory.create(session);
        // 校验 sessionId
        String safe_session_id = SessionIdSanitizer.requireSafe(session.getSessionId());

        SessionManager memory_session_manager = SessionManager
                .forSessionId(safe_session_id)
                .withSession(new JsonSession(Path.of(memorySessionDir)))
                .addComponent(agent);

        memory_session_manager.loadIfExists();

        try {
            Msg result = agent.call(
                    Msg.builder()
                            .textContent(task)
                            .build()
            ).block();

            memory_session_manager.saveSession();

            if (result != null) {
                session.sendMessage(result.getTextContent());
            } else {
                session.sendMessage("task execution finished, but LLM returned no content!");
            }
        } catch (Exception e) {
            memory_session_manager.saveSession();
            session.sendMessage("task execution field:" + e.getMessage());
        }
    }
}
