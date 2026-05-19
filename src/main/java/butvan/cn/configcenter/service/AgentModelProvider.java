package butvan.cn.configcenter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Agent 模型提供器
 */
@Component
@RequiredArgsConstructor
public class AgentModelProvider {

    private final ModelConfigService modelConfigService;

}
