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

    private TokenBudgetProperties tokenBudget = new TokenBudgetProperties();


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
        private int browserMaxIters = 8;
    }

    @Data
    public static class TokenBudgetProperties {
        private int SimpleChatMaxInput = 800;
        private int PlannerWarningInput = 3000;
        private int PlannerHardLimitInput = 8000;
    }
}
