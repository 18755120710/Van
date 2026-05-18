package butvan.cn.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogMessageDTO {

    public static final String TYPE_SERVER = "server";
    public static final String TYPE_USER = "user";

    private String type;
    private String text;
    private String imageUrl;
    private String fileUrl;
    private String openUrl;

    /**
     * 同一次 Agent 回复的唯一 ID。
     *
     * 为什么需要它？
     * 流式输出时，后端会连续发送很多条 WebSocket 消息：
     * chunk1、chunk2、chunk3、最终结果……
     *
     * 前端需要靠 streamId 判断：
     * “这些片段是不是属于同一条 Agent 回复？”
     *
     * 如果没有 streamId，前端只能每收到一个 chunk 就新增一条消息，
     * 页面会变成很多碎片消息。
     */
    private String streamId;

    /**
     * AgentScope stream 事件类型。
     *
     * 常见值包括：
     * REASONING    模型推理/生成过程
     * TOOL_RESULT  工具调用结果，例如 use_browser_agent、create_plan
     * SUMMARY      达到最大轮次时的总结
     * AGENT_RESULT 最终回复
     *
     * 前端可以根据 eventType 决定：
     * - 普通聊天区只展示 REASONING / AGENT_RESULT
     * - 调试面板展示 TOOL_RESULT
     * - 计划面板展示 create_plan / finish_subtask 等工具结果
     */
    private String eventType;

    /**
     * 是否是流式输出过程中的消息。
     *
     * true  表示这条消息是中间片段
     * false 表示不是流式消息，或者已经是普通完整消息
     */
    private Boolean streaming;

    /**
     * 当前这一次 Agent 回复是否已经结束。
     *
     * true 表示这次 stream 已经结束，前端可以：
     * - 停止 loading
     * - 恢复输入框
     * - 把消息状态改成完成
     */
    private Boolean streamEnd;

}
