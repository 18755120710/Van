package butvan.cn.agent.trace;

import butvan.cn.websocket.dto.DialogMessageDTO;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.hook.*;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolUseBlock;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AgentTraceHook implements Hook {

    private final MessageSession session;

    /**
     * 当前用户问题对应的一次执行 ID
     *
     * 同一轮执行中的规划、工具调用、最终回答
     * 都使用同一个 trace_id
     */
    private final TraceContextRegistry traceContextRegistry;

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {

        /**
         * 工具调用前触发。
         *
         * 这里适合告诉前端：
         * - PlannerAgent 准备创建计划
         * - PlannerAgent 准备调用 BrowserAgent
         * - BrowserAgent 准备打开网页
         * - BrowserAgent 准备提取页面内容
         */
        if (event instanceof PreActingEvent e) {
            handlePreActing(e);
            return Mono.just(event);
        }

        if (event instanceof PostActingEvent e) {
            handlePostActing(e);
            return Mono.just(event);
        }

        if (event instanceof ErrorEvent e) {
            sendTrace(
                    "error",
                    e.getAgent().getName(),
                    null,
                    "Agent 执行出错：" + e.getError().getMessage(),
                    true
            );
            return Mono.just(event);
        }

        return Mono.just(event);
    }

    private void handlePreActing(PreActingEvent event) {
        ToolUseBlock tool_use = event.getToolUse();

        String agent_name = event.getAgent().getName();
        String tool_name = tool_use.getName();

        if ("create_plan".equals(tool_name)) {
            String plan_input = String.valueOf(tool_use.getInput());


            sendTrace(
                    "status",
                    agent_name,
                    tool_name,
                    "正在创建计划：\n" + plan_input,
                    false
            );
            return;
        }

        sendTrace(
                "tool_call",
                agent_name,
                tool_name,
                agent_name + "正在调用工具：" + tool_name,
                false
        );
    }

    private void handlePostActing(PostActingEvent event) {
        ToolUseBlock toolUse = event.getToolUse();
        ToolResultBlock toolResult = event.getToolResult();

        String agentName = event.getAgent().getName();
        String toolName = toolUse.getName();

        /**
         * 把工具返回结果转成文本。
         *
         * 工具结果可能包含多个 ContentBlock，
         * 这里先只提取 TextBlock。
         */
        String resultText = extractText(toolResult);

        /**
         * create_plan 执行完成后，
         * resultText 通常会包含规划出来的步骤。
         *
         * 这就是你想在前端看到的：
         * “AgentScope 规划完步骤后，展示具体步骤”。
         */
        if ("create_plan".equals(toolName)) {
            sendTrace(
                    "plan",
                    agentName,
                    toolName,
                    "计划创建结果：\n" + resultText,
                    false
            );
            return;
        }

        /**
         * finish_subtask 表示某个子任务完成。
         *
         * 如果你想做类似“步骤 1 已完成”的 UI，
         * 可以重点处理这个事件。
         */
        if ("finish_subtask".equals(toolName)) {
            sendTrace(
                    "plan",
                    agentName,
                    toolName,
                    "子任务完成：\n" + resultText,
                    false
            );
            return;
        }

        /**
         * use_browser_agent 完成后，
         * 表示 BrowserAgent 已经返回执行结果。
         */
        if ("use_browser_agent".equals(toolName)) {
            sendTrace(
                    "agent_call",
                    agentName,
                    toolName,
                    "BrowserAgent 执行完成",
                    false
            );
            return;
        }

        /**
         * 其他工具结果建议不要全部塞到聊天正文。
         *
         * 可以先截断展示，避免页面被网页正文、搜索结果撑爆。
         */
        sendTrace(
                "tool_result",
                agentName,
                toolName,
                "工具完成：" + toolName + "\n" + summarizeToolResult(toolName,resultText),
                false
        );
    }

    /**
     * 根据工具类型，把原始工具结果压缩成适合前端展示的摘要。
     *
     * 注意：
     * 原始完整内容仍然可以保留在后端日志或 JSONL trace 中。
     * 前端不应该展示几千字的页面 dump。
     */
    private String summarizeToolResult(String toolName, String resultText) {
        if (resultText == null || resultText.isBlank()) {
            return "";
        }

        if ("view_current_page_status".equals(toolName)) {
            return summarizePageStatus(resultText);
        }

        if ("extract_content".equals(toolName)) {
            return summarizeExtractContent(resultText);
        }

        if ("go_to_url".equals(toolName)) {
            return abbreviate(resultText, 300);
        }

        if ("input_text".equals(toolName) || "click_element".equals(toolName)) {
            return abbreviate(resultText, 200);
        }

        return abbreviate(resultText, 500);
    }

    private String summarizePageStatus(String text) {
        String title = extractLineStartingWith(text, "title:");
        String url = extractLineStartingWith(text, "url:");
        long elementCount = text.lines()
                .filter(line -> line.matches("\\[\\d+\\].*"))
                .count();

        return """
            页面状态：
            %s
            %s
            可交互元素数量：%d
            """.formatted(title, url, elementCount);
    }

    private String summarizeExtractContent(String text) {
        String title = extractLineStartingWith(text, "标题：");
        String url = extractLineStartingWith(text, "URL：");

        return """
            页面内容提取：
            %s
            %s

            摘要：
            %s
            """.formatted(
                title,
                url,
                abbreviate(text, 800)
        );
    }

    private String extractLineStartingWith(String text, String prefix) {
        return text.lines()
                .filter(line -> line.startsWith(prefix))
                .findFirst()
                .orElse(prefix + "未知");
    }

    private String extractText(ToolResultBlock toolResult) {
        if (toolResult == null || toolResult.getOutput() == null) {
            return "";
        }

        return toolResult.getOutput()
                .stream()
                .filter(block -> block instanceof TextBlock)
                .map(block -> ((TextBlock) block).getText())
                .collect(Collectors.joining("\n"));
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "\n...";
    }

    private void sendTrace(
            String eventType,
            String agentName,
            String toolName,
            String text,
            boolean done
    ) {
        String trace_id = traceContextRegistry.getCurrentTraceId(session.getSessionId());
        if (trace_id == null) {
            trace_id = "unknown-" + session.getSessionId();
        }
        session.sendMessage(DialogMessageDTO.builder()
                        .type(DialogMessageDTO.TYPE_SERVER)
                        .traceId(trace_id)
                        .eventType(eventType)// 过程的消息类型
                        .agentName(agentName)// 谁产生的事件
                        .toolName(toolName)// 调用的是哪个工具
                        .text(text)// 展示内容
                        .trace(true)// true：表示是执行过程，不是最终回答
                        .done(done)// 表示整轮执行结束或异常结束
                .build());
    }
}
