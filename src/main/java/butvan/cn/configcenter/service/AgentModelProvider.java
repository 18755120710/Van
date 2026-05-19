package butvan.cn.configcenter.service;

import butvan.cn.configcenter.model.AgentModelConfig;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Agent 模型提供器
 */
@Component
@RequiredArgsConstructor
public class AgentModelProvider {

    private final ModelConfigService modelConfigService;


    /**
     * 根据当前生效配置创建 AgentScopeModel
     * @return
     */
    public Model curentModel() {
        AgentModelConfig config = modelConfigService.loadEffectiveConfig();

        return DashScopeChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .stream(config.getStream())
                .defaultOptions(GenerateOptions.builder()
                        .temperature(config.getTemperature() == null ? 0.2 : config.getTemperature())
                        .maxTokens(config.getMaxTokens() == null ? 8192 : config.getMaxTokens())
                        .build())
                .build();
    }

}
