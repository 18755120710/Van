package butvan.cn.conversation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UiMessage {

    // 每条 ui 消息自己的唯一 id
    private String id;

    // 当前消息所属的会话 id
    private String conversationId;

    // 消息类型 user：用户发送消息、server：Agent回复
    private String type;

    // agent 执行 id
    private String traceId;

    // 消息正文
    private String text;

    // 图片地址
    private String imageUrl;

    // 文件地址
    private String fileUrl;

    // 打开的 url
    private String openUrl;

    // 消息创建时间
    private Long createAt;


}
