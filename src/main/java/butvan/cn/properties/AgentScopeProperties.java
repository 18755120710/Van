package butvan.cn.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agentscope")
public class AgentScopeProperties {

    private ModelProperties model = new ModelProperties();

    private ReActProperties reAct = new ReActProperties();


    @Data
    public static class ModelProperties {
        private String apiKey;
        private String baseUrl;
        private String modelName;
        private boolean stream = true;
    }

    @Data
    public static class ReActProperties {
        private int maxIters = 30;
        private int maxActionsPerCall = 5;
    }
}
