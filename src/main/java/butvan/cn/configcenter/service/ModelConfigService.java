package butvan.cn.configcenter.service;

import butvan.cn.configcenter.model.AgentModelConfig;
import butvan.cn.configcenter.model.AgentModelConfigView;
import butvan.cn.configcenter.model.UpdateModelConfigRequest;

/**
 * 模型服务配置接口
 */
public interface ModelConfigService {

    /**
     * 加载当前实际生效的模型配置
     * @return
     */
    AgentModelConfig loadEffectiveConfig();

    /**
     * 获取当前给前端展示的模型配置
     * @return
     */
    AgentModelConfigView getConfigView();

    /**
     * 更新模型配置
     * @param request
     * @return
     */
    AgentModelConfigView updateConfig(UpdateModelConfigRequest request);
}
