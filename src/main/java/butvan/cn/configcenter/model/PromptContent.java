package butvan.cn.configcenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt 内容详情。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptContent {

    /**
     * Prompt 的唯一 key。
     */
    private String key;

    /**
     * Prompt 文件名。
     */
    private String filename;

    /**
     * Prompt 当前实际生效的内容。
     */
    private String content;

    /**
     * 当前 prompt 来源。
     */
    private String source;

    /**
     * 最后更新时间。
     */
    private Long updatedAt;
}