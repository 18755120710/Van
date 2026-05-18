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
     * 本次用户提问对应的一次 Agent 执行 ID。
     *
     * 一次用户问题会产生多条过程消息：
     * - 规划开始
     * - 规划步骤
     * - 调用 BrowserAgent
     * - BrowserAgent 打开网页
     * - BrowserAgent 提取内容
     * - 最终回答
     *
     * 这些消息都使用同一个 traceId，前端可以把它们归为同一轮执行。
     */
    private String traceId;

    /**
     * 前端展示类型。
     *
     * 建议值：
     * status      状态提示，例如“正在规划步骤”
     * plan        规划步骤
     * agent_call  调用了哪个子 Agent
     * tool_call   调用了哪个工具
     * tool_result 工具返回结果
     * answer      最终回答
     * error       错误信息
     */
    private String eventType;

    /**
     * 当前事件来自哪个 Agent。
     *
     * 例如：
     * PlannerAgent
     * BrowserAgent
     */
    private String agentName;

    /**
     * 当前调用的工具名。
     *
     * 例如：
     * create_plan
     * use_browser_agent
     * go_to_url
     * extract_content
     */
    private String toolName;

    /**
     * 是否是中间过程消息。
     *
     * true 表示这条消息只是执行过程，
     * false 表示这条消息可以当作正式回答。
     */
    private Boolean trace;

    /**
     * 当前这一轮 Agent 执行是否结束。
     *
     * 前端可以根据它恢复输入框。
     */
    private Boolean done;

    /**
     * 客户端动作类型。
     *
     * chat:
     *   普通用户提问。
     *
     * stop:
     *   停止当前正在运行的 Agent。
     *
     * 为什么需要这个字段？
     *
     * 因为 stop 指令可能没有 text。
     * 如果只靠 text 判断，后端会把空文本 stop 消息过滤掉。
     */
    private String action;


}
