package butvan.cn.agent.service;

import butvan.cn.agent.browser.runtime.AgentExecutionHandle;
import butvan.cn.agent.browser.runtime.AgentExecutionRegistry;
import butvan.cn.agent.chat.SimpleChatAgentFactory;
import butvan.cn.agent.planner.PlannerAgentFactory;
import butvan.cn.agent.router.AgentRouterType;
import butvan.cn.agent.router.AgentTaskRouter;
import butvan.cn.agent.trace.TokenUsageRegistry;
import butvan.cn.agent.trace.TokenUsageStates;
import butvan.cn.agent.trace.TraceContextRegistry;
import butvan.cn.common.security.SessionIdSanitizer;
import butvan.cn.conversation.model.UiMessage;
import butvan.cn.conversation.service.ChatHistoryService;
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
import reactor.core.Disposable;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class AgentRunService {

    private final PlannerAgentFactory plannerAgentFactory;
    private final TraceContextRegistry traceContextRegistry;
    private final AgentExecutionRegistry agentExecutionRegistry;
    private final TokenUsageRegistry tokenUsageRegistry;
    private final ChatHistoryService chatHistoryService;
    private final SimpleChatAgentFactory simpleChatAgentFactory;
    private final AgentTaskRouter agentTaskRouter;

    @Value("${memory.session-dir:./memory/sessions}")
    private String memorySessionDir;

    public void run(MessageSession session, String conversationId ,String traceId, AgentExecutionHandle handle) {
        String task = session.readMessage();

        String safe_session_id = SessionIdSanitizer.requireSafe(session.getSessionId());

        // 新一轮任务开始，清理上一轮 stop 标记
        traceContextRegistry.clearStopped(safe_session_id);

        traceContextRegistry.setCurrentTraceId(safe_session_id, traceId);
        // 根据用户输入选择 agent
        AgentRouterType route_type = agentTaskRouter.route(task);
        ReActAgent agent;
        String current_agent_name;
        if (route_type == AgentRouterType.SIMPLE_CHAT) {
            agent = simpleChatAgentFactory.create(session);
            current_agent_name = "SimpleChatAgent";
        } else {
            agent = plannerAgentFactory.create(session,traceId);
            current_agent_name = "PlannerAgent";
        }

        // 保存 planner agent
        handle.setPlannerAgent(agent);

        // 保存并加载会话记忆
        SessionManager memory_session_manager = SessionManager
                .forSessionId(conversationId)
                .withSession(new JsonSession(Path.of(memorySessionDir)))
                .addComponent(agent);
        memory_session_manager.loadIfExists();

        try {

            AtomicBoolean answer_started = new AtomicBoolean(false);
            // 积累流式回答
            StringBuilder answer_buffer = new StringBuilder();

            // 标记本轮 agent 是否正常拿到了最终结果
            AtomicBoolean completed_successfully = new AtomicBoolean(false);
            // 标记 stream 是否发生异常
            AtomicBoolean stream_failed = new AtomicBoolean(false);

            // 配置流失事件类型
            StreamOptions stream_options = StreamOptions.builder()
                    .eventTypes(EventType.REASONING, EventType.TOOL_RESULT, EventType.SUMMARY, EventType.AGENT_RESULT)
                    .incremental(true)
                    .includeReasoningChunk(true)
                    .includeReasoningResult(true)
                    .includeSummaryChunk(true)
                    .includeSummaryResult(true)
                    .build();

            CountDownLatch done_latch = new CountDownLatch(1);

            Disposable disposable = agent.stream(
                    Msg.builder()
                            .textContent(task)
                            .build(),
                    stream_options
            ).doOnNext(event -> {

                // 如果用户已经点击了停止，不再向前端推送任何新事件
                if (handle.getStopRequested().get()) {
                    return;
                }

                Msg event_message = event.getMessage();
                String text = event_message == null ? "" : event_message.getTextContent();
                if (text == null) {
                    text = "";
                }

                EventType type = event.getType();
                if (type == EventType.REASONING) {
                    // 标识模型还在生成回答
                    if (!text.isBlank()) {
                        answer_started.set(true);
                        // 累计流式文本
                        answer_buffer.append(text);
                        session.sendMessage(DialogMessageDTO.builder()
                                .type(DialogMessageDTO.TYPE_SERVER)
                                .traceId(traceId)
                                .eventType("answer_delta")
                                .agentName(current_agent_name)
                                .trace(false)
                                .done(false)
                                .text(text)
                                .build());
                    }
                    return;
                }
                if (type == EventType.AGENT_RESULT) {
                    completed_successfully.set(true);
                    // 标识agent最终结果
                    String final_text = answer_started.get() ? answer_buffer.toString() : text;

                    sendTokenMessage(session,traceId);

                    session.sendMessage(DialogMessageDTO.builder()
                            .type(DialogMessageDTO.TYPE_SERVER)
                            .traceId(traceId)
                            .eventType("answer")
                            .agentName(current_agent_name)
                            .trace(false)
                            .done(true)
                            .text(final_text)
                            .build());

                    // 保存 agent 最终回答
                    if (final_text != null && !final_text.isBlank()) {
                        chatHistoryService.appendMessage(
                                conversationId,
                                UiMessage.builder()
                                        .id(UUID.randomUUID().toString())
                                        .conversationId(conversationId)
                                        .type(DialogMessageDTO.TYPE_SERVER)
                                        .traceId(traceId)
                                        .text(final_text)
                                        .createAt(System.currentTimeMillis())
                                        .build()
                        );
                    }
                    return;
                }
                if (type == EventType.SUMMARY) {
                    // summary 只是过程信息
                    if (!text.isBlank()) {
                        session.sendMessage(DialogMessageDTO.builder()
                                .type(DialogMessageDTO.TYPE_SERVER)
                                .traceId(traceId)
                                .eventType("summary")
                                .agentName(current_agent_name)
                                .trace(true)
                                .done(false)
                                .text(text)
                                .build());
                    }
                    return;
                }
                if (type == EventType.TOOL_RESULT) {
                    // 工具结果
                    return;
                }
            }).doOnError(e -> {
                stream_failed.set(true);

                if (handle.getStopRequested().get()) {
                    return;
                }

                session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .traceId(traceId)
                        .eventType("error")
                        .agentName(current_agent_name)
                        .trace(true)
                        .done(true)
                        .text("Agent 执行失败：" + e.getMessage())
                        .build());
            }).doFinally(signalType -> {
                /**
                 * 无论 stream 正常完成、异常、还是被 dispose，
                 * 都会进入 doFinally。
                 *
                 * 这里释放 latch，让 run() 方法继续往 finally 走。
                 */
                done_latch.countDown();
            }).subscribe();

            // 保存 stream disposable
            handle.setStreamDisposable(disposable);
            done_latch.await();

            // 只在正常完成的时候才保存 agent session
            if (completed_successfully.get()
                    && !stream_failed.get()
                    && !handle.getStopRequested().get()
            ) {
                memory_session_manager.saveSession();
            }
        } catch (InterruptedException e) {
            /**
             * 当前线程被中断，通常来自 future.cancel(true)。
             *
             * 如果用户已经请求停止，不需要再推 error。
             */
            Thread.currentThread().interrupt();

            if (!handle.getStopRequested().get()) {
                session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .traceId(traceId)
                        .eventType("error")
                        .agentName(current_agent_name)
                        .trace(true)
                        .done(true)
                        .text("Agent 执行线程被中断：" + e.getMessage())
                        .build());
            }
        } catch (Exception e) {
            if (!handle.getStopRequested().get()) {
                session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .traceId(traceId)
                        .eventType("error")
                        .agentName(current_agent_name)
                        .trace(true)
                        .done(true)
                        .text("task execution failed: " + e.getMessage())
                        .build());
            }
        } finally {

            tokenUsageRegistry.clear(traceId);

            /**
             * 当前轮执行结束后清理 traceId。
             */
            traceContextRegistry.clear(safe_session_id);

            agentExecutionRegistry.remove(safe_session_id);
        }
    }

    /**
     * 判断当前 session 是否已经有 agent 在运行
     * @param sessionId
     * @return
     */
    public boolean isRunning(String sessionId) {
        return agentExecutionRegistry.isRunning(sessionId);
    }

    /**
     * 停止当前 session 运行的 agent
     * @param session
     */
    public void stop(MessageSession session) {

        String session_id = SessionIdSanitizer.requireSafe(session.getSessionId());

        // 标记当前 session 已停止
        traceContextRegistry.markStopped(session_id);

        // 获取当前执行句柄
        AgentExecutionHandle handle = agentExecutionRegistry.get(session_id);

        String trace_id = handle == null ? null : handle.getTraceId();

        boolean stopped = agentExecutionRegistry.stop(session_id);

        session.sendMessage(DialogMessageDTO.builder()
                .type(DialogMessageDTO.TYPE_SERVER)
                .traceId(trace_id)
                .eventType("stopped")
                .agentName("System")
                .trace(true)
                .done(true)
                .text(stopped ? "已停止当前 Agent 执行。" : "当前没有正在执行的 Agent。")
                .build());

    }

    private void sendTokenMessage(MessageSession session, String traceId) {
        TokenUsageStates stats = tokenUsageRegistry.get(traceId);

        if (stats == null) return;

        String text = """
            本轮大模型消耗：
            输入 Token：%d
            输出 Token：%d
            总 Token：%d
            模型调用次数：%d
            模型总耗时：%.2f 秒
            """.formatted(
                stats.getInputTokens(),
                stats.getOutputTokens(),
                stats.getTotalTokens(),
                stats.getModelCallCount(),
                stats.getTotalTime()
        );

        session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .traceId(traceId)
                        .eventType("token_usage")
                        .agentName("AgentScope")
                        .trace(true)
                        .done(false)
                        .text(text)
                .build());
    }
}
