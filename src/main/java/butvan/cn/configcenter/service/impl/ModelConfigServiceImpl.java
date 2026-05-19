package butvan.cn.configcenter.service.impl;

import butvan.cn.configcenter.model.AgentModelConfig;
import butvan.cn.configcenter.model.AgentModelConfigView;
import butvan.cn.configcenter.model.UpdateModelConfigRequest;
import butvan.cn.configcenter.service.ModelConfigService;
import butvan.cn.properties.AgentScopeProperties;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ModelConfigServiceImpl implements ModelConfigService {

    private final ObjectMapper objectMapper;

    private final AgentScopeProperties agentScopeProperties;

    @Value("${agent-config.model-file:./data/agent-config/model-config.json}")
    private String modelConfigFile;


    /**
     * 加载当前时间生效的模型配置
     * @return
     */
    @Override
    public AgentModelConfig loadEffectiveConfig() {
        Path path = modelConfigPath();
        if (Files.exists(path)) {
            return readConfig(path);
        }

        return defaultConfigFromApplicationYml();
    }

    /**
     * 获取前端展示的模型配置
     * @return
     */
    @Override
    public AgentModelConfigView getConfigView() {
        AgentModelConfig config = loadEffectiveConfig();
        return toView(config);
    }

    /**
     * 更新模型配置
     * @param request
     * @return
     */
    @Override
    public AgentModelConfigView updateConfig(UpdateModelConfigRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("model config request cannot be null");
        }

        AgentModelConfig old_config = loadEffectiveConfig();

        AgentModelConfig new_config = AgentModelConfig.builder()
                .provider(defaultIfBlank(request.getProvider(), old_config.getProvider()))
                .baseUrl(defaultIfBlank(request.getBaseUrl(), old_config.getBaseUrl()))
                .modelName(defaultIfBlank(request.getModelName(), old_config.getModelName()))
                .apiKey(defaultIfBlank(request.getApiKey(), old_config.getApiKey()))
                .stream(request.getStream() == null ? old_config.getStream() : request.getStream())
                .maxTokens(request.getMaxTokens() == null ? old_config.getMaxTokens() : request.getMaxTokens())
                .updatedAt(System.currentTimeMillis())
                .build();

        validateConfig(new_config);

        saveConfig(new_config);

        return toView(new_config);

    }

    private void saveConfig(AgentModelConfig config) {
        try {
            Path path = modelConfigPath();

            Files.createDirectories(path.getParent());

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(path.toFile(), config);
        } catch (IOException e) {
            throw new IllegalStateException("保存模型配置失败", e);
        }
    }

    /**
     * 校验模型配置
     * @param config
     */
    private void validateConfig(AgentModelConfig config) {
        if (StrUtil.isBlank(config.getProvider())) {
            throw new IllegalArgumentException("provider cannot be blank");
        }

        if (StrUtil.isBlank(config.getBaseUrl())) {
            throw new IllegalArgumentException("baseUrl cannot be blank");
        }

        if (StrUtil.isBlank(config.getModelName())) {
            throw new IllegalArgumentException("modelName cannot be blank");
        }

        if (StrUtil.isBlank(config.getApiKey())) {
            throw new IllegalArgumentException("apiKey cannot be blank");
        }

        if (config.getTemperature() != null
                && (config.getTemperature() < 0 || config.getTemperature() > 2)) {
            throw new IllegalArgumentException("temperature must be between 0 and 2");
        }

        if (config.getMaxTokens() != null && config.getMaxTokens() <= 0) {
            throw new IllegalArgumentException("maxTokens must be greater than 0");
        }
    }

    private AgentModelConfigView toView(AgentModelConfig config) {
        return AgentModelConfigView.builder()
                .provider(config.getProvider())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .apiKeyMasked(maskApiKey(config.getApiKey()))
                .stream(config.getStream())
                .temperature(config.getTemperature())
                .maxTokens(config.getMaxTokens())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    /**
     * 从 application.yml 构建默认模型配置
     * @return
     */
    private AgentModelConfig defaultConfigFromApplicationYml() {
        AgentScopeProperties.ModelProperties model = agentScopeProperties.getModel();

        return AgentModelConfig.builder()
                .provider("dashscope")
                .baseUrl(model.getBaseUrl())
                .modelName(model.getModelName())
                .apiKey(model.getApiKey())
                .stream(model.isStream())
                .temperature(0.2)
                .maxTokens(8192)
                .updatedAt(null)
                .build();
    }

    /**
     * 从 model-config.json 读取模型配置
     * @param path
     * @return
     */
    private AgentModelConfig readConfig(Path path) {
        try {
            return objectMapper.readValue(path.toFile(),AgentModelConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("读取模型配置失败：" + path, e);
        }
    }

    /**
     * 获取模型配置文件路径
     * @return
     */
    private Path modelConfigPath() {
        return Path.of(modelConfigFile);
    }

    /**
     * 如果 value 为空，则返回 defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    /**
     * API KEY 脱敏
     * @param apiKey
     * @return
     */
    private String maskApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey)) {
            return "";
        }

        if (apiKey.length() <= 8) {
            return "****";
        }

        return apiKey.substring(0, 3) + "****" + apiKey.substring(apiKey.length() - 4);
    }

}
