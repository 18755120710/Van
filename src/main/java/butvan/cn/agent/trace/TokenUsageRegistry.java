package butvan.cn.agent.trace;

import io.agentscope.core.model.ChatUsage;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenUsageRegistry {

    /**
     * key: traceId
     * value: 当前 traceId 对应的 token 统计
     */
    private final ConcurrentHashMap<String, TokenUsageStates> usage_map = new ConcurrentHashMap<>();


    /**
     * 记录一次模型调用的 token 消耗
     * @param traceId
     * @param usage
     */
    public void record(String traceId, ChatUsage usage) {
        if (traceId == null || traceId.isBlank() || usage == null) {
            return;
        }

        TokenUsageStates states = usage_map.computeIfAbsent(traceId, id -> TokenUsageStates.builder().build());

        states.add(
                usage.getInputTokens(),
                usage.getOutputTokens(),
                usage.getTotalTokens(),
                usage.getTime()
        );
    }

    public TokenUsageStates get(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return null;
        }

        return usage_map.get(traceId);
    }

    public void clear(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return;
        }
        usage_map.remove(traceId);
    }
}
