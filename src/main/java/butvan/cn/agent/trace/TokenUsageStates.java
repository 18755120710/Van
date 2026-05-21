package butvan.cn.agent.trace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenUsageStates {

    // 输入 token 数
    private int inputTokens;

    // 输出 token 数
    private int outputTokens;

    // 总 token
    private int totalTokens;

    // 大模型调用次数
    private int modelCallCount;

    // 模型调用耗时总和
    private double totalTime;

    /**
     * 把一次模型调用的 usage 累加到当前统计对象中
     * @param inputTokens
     * @param outputTokens
     * @param totalTokens
     * @param time
     */
    public void add(int inputTokens, int outputTokens, int totalTokens, double time) {

        this.inputTokens += inputTokens;
        this.outputTokens += outputTokens;
        this.totalTokens += totalTokens;
        this.totalTime += time;
        this.modelCallCount += 1;
    }
}
