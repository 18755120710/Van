package butvan.cn.configcenter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 模型配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentModelConfig {

    /**
     * 模型供应商。
     *
     * 当前项目使用 DashScope 兼容 OpenAI 风格接口。
     * 第一版可以固定为 dashscope。
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
     * 模型 API Key 原文。
     */
    private String apiKey;

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
     * 最后更新时间，毫秒时间戳。
     */
    private Long updatedAt;
}