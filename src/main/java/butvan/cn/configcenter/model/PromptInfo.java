package butvan.cn.configcenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt 列表项。
 *
 * 这个类用于返回给前端“有哪些 prompt 可以编辑”。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptInfo {

    /**
     * Prompt 的唯一 key。
     */
    private String key;

    /**
     * Prompt 文件名。
     */
    private String filename;

    /**
     * Prompt 展示名称。
     */
    private String name;

    /**
     * 当前 prompt 来源。
     */
    private String source;

    /**
     * Prompt 最后更新时间。
     */
    private Long updatedAt;
}