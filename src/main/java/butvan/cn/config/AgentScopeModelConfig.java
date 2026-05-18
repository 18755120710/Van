package butvan.cn.config;

import butvan.cn.properties.AgentScopeProperties;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentScopeModelConfig {

    @Bean
    public Model mainModel(AgentScopeProperties agentScopeProperties) {
        return DashScopeChatModel.builder()
                .apiKey(agentScopeProperties.getModel().getApiKey())
                .modelName(agentScopeProperties.getModel().getModelName())
                .stream(agentScopeProperties.getModel().isStream())
                .defaultOptions(GenerateOptions.builder()
                        .temperature(0.2)
                        .maxTokens(8192)
                        .build())
                .build();
    }
}
