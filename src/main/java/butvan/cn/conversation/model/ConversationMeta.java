package butvan.cn.conversation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMeta {

    // 业务会话 id
    private String conversationId;

    // 历史列表标题
    private String title;

    // 是否已生成过只能标题
    private Boolean titleGenerated;

    // 会话创建时间
    private Long createAt;

    // 最后更新时间
    private Long updateAt;

    // 消息数量
    private Integer messageCount;

    private Boolean deleted;
}
