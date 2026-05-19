package butvan.cn.domain.meta;

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

    // 会话创建时间
    private Long createAt;

    // 最后更新时间
    private Long updateAt;

    // 消息数量
    private Integer messageCount;
}
