package butvan.cn.configcenter.model;

import lombok.Data;

/**
 * 更新模型配置请求。
 */
@Data
public class UpdateModelConfigRequest {

    /**
     * 模型供应商。
     *
     * 第一版可以传 dashscope。
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
     * 新的 API Key。
     *
     * 如果为空，后端保留旧 key。
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
}