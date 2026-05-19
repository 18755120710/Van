package butvan.cn.configcenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模型配置展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentModelConfigView {

    /**
     * 模型供应商。
     */
    private String provider;

    /**
     * 模型 API Base URL。
     */
    private String baseUrl;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 脱敏后的 API Key。
     *
     * 例如：
     * sk-****abcd
     */
    private String apiKeyMasked;

    /**
     * 是否开启流式输出。
     */
    private Boolean stream;

    /**
     * 温度参数。
     */
    private Double temperature;

    /**
     * 最大输出 token 数。
     */
    private Integer maxTokens;

    /**
     * 最后更新时间。
     */
    private Long updatedAt;
}