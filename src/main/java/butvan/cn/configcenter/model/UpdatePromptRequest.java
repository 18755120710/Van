package butvan.cn.configcenter.model;

import lombok.Data;

/**
 * 更新 Prompt 的请求体。
 */
@Data
public class UpdatePromptRequest {

    /**
     * 用户在前端编辑器中填写的新 prompt 内容。
     */
    private String content;
}