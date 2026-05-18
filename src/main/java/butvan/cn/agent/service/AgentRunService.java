package butvan.cn.agent.service;

import butvan.cn.agent.planner.PlannerAgentFactory;
import butvan.cn.common.security.SessionIdSanitizer;
import butvan.cn.websocket.dto.DialogMessageDTO;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.UUID;

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

        // 保存并加载会话记忆
        SessionManager memory_session_manager = SessionManager
                .forSessionId(safe_session_id)
                .withSession(new JsonSession(Path.of(memorySessionDir)))
                .addComponent(agent);
        memory_session_manager.loadIfExists();

        // 给本次agent回复，生成一个唯一的streamId
        String stream_id = UUID.randomUUID().toString();

        try {
            // 配置流失事件类型
            StreamOptions stream_options = StreamOptions.builder()
                    /**
                     * 指定你想监听哪些事件。
                     *
                     * REASONING:
                     *   模型推理/生成过程。普通聊天流式文本主要看这个。
                     *
                     * TOOL_RESULT:
                     *   工具调用结果。比如：
                     *   - use_browser_agent 的返回
                     *   - create_plan 的返回
                     *   - finish_subtask 的返回
                     *
                     * SUMMARY:
                     *   当 agent 达到最大迭代次数时，可能会生成总结。
                     *
                     * AGENT_RESULT:
                     *   最终回复。注意文档里说默认不一定包含 AGENT_RESULT，
                     *   所以这里显式加上。
                     */
                    .eventTypes(EventType.REASONING, EventType.TOOL_RESULT, EventType.SUMMARY, EventType.AGENT_RESULT)
                    .incremental(true)
                    .includeReasoningChunk(true)
                    .includeReasoningResult(true)
                    .includeSummaryChunk(true)
                    .includeSummaryChunk(true)
                    .build();

            agent.stream(
                    Msg.builder()
                            .textContent(task)
                            .build(),
                    stream_options
            ).doOnNext(event -> {
                // event 表示 AgentScope 推出来的一次流式事件
                // 可能是 REASONING、TOOL_RESULT、SUMMARY、AGENT_RESULT 等

                Msg event_message = event.getMessage();
                String text = event_message == null ? "" : event_message.getTextContent();

                if (text == null) {
                    text = "";
                }

                // 判断是不是最终结果事件
                boolean is_final_result = event.getType() == EventType.AGENT_RESULT;

                session.sendMessage(DialogMessageDTO.builder()
                                .type(DialogMessageDTO.TYPE_SERVER)
                                .text(text)
                                .streamId(stream_id)
                                .eventType(event.getType().name())
                                .streaming(!is_final_result)
                                .streamEnd(is_final_result)
                        .build());

            }).doOnError(e -> {
                // stream 过程中如果发生异常，推送错误消息给前端
                session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .streamId(stream_id)
                        .eventType("ERROR")
                        .streaming(false)
                        .streamEnd(true)
                        .text("Agent 执行失败：" + e.getMessage())
                        .build());
            }).blockLast();

            // agent 执行完成之后，保存 session 状态
            memory_session_manager.saveSession();
        } catch (Exception e) {
            memory_session_manager.saveSession();
            session.sendMessage(DialogMessageDTO.builder()
                    .type(DialogMessageDTO.TYPE_SERVER)
                    .streamId(stream_id)
                    .eventType("ERROR")
                    .streaming(false)
                    .streamEnd(true)
                    .text("task execution failed: " + e.getMessage())
                    .build());

        }
    }
}
