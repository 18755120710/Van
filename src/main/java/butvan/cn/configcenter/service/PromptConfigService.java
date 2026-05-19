package butvan.cn.configcenter.service;

import butvan.cn.configcenter.model.PromptContent;
import butvan.cn.configcenter.model.PromptInfo;

import java.util.List;

/**
 * Prompt 服务配置
 */
public interface PromptConfigService {

    /**
     * 查询所有可管理的 prompt 列表
     * @return
     */
    List<PromptInfo> listPrompt();

    /**
     * 查询某个 Prompt 当前生效的完整内容
     * @param key
     * @return
     */
    PromptContent getPromptContent(String key);

    /**
     * 获取某个 prompt 当前真正生效的文本
     * @param key
     * @return
     */
    String getEffectivePrompt(String key);

    /**
     * 保存自定义 prompt
     * @param key
     * @param content
     * @return
     */
    PromptContent savePrompt(String key, String content);

    /**
     * 重置 prompt
     * @param key
     * @return
     */
    PromptContent resetPrompt(String key);
}
