package butvan.cn.controller;

import butvan.cn.configcenter.model.PromptContent;
import butvan.cn.configcenter.model.PromptInfo;
import butvan.cn.configcenter.model.UpdatePromptRequest;
import butvan.cn.configcenter.service.PromptConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 配置中心接口
 */
@RestController
@RequestMapping("/agent-config")
@RequiredArgsConstructor
public class AgentConfigController {

    private final PromptConfigService promptConfigService;

    /**
     * 查询所有可管理的 prompt 列表。
     * @return prompt 列表
     */
    @GetMapping("/prompts")
    public List<PromptInfo> listPrompts() {
        return promptConfigService.listPrompt();
    }

    /**
     * 查询某个 prompt 的当前生效内容。
     * @param key prompt key，例如 promptPlanningSystem
     * @return prompt 内容详情
     */
    @GetMapping("/prompts/{key}")
    public PromptContent getPromptContent(@PathVariable String key) {
        return promptConfigService.getPromptContent(key);
    }

    /**
     * 保存某个 prompt 的自定义内容。
     * @param key prompt key
     * @param request prompt 更新请求
     * @return 保存后的 prompt 内容详情
     */
    @PutMapping("/prompts/{key}")
    public PromptContent updatePrompt(
            @PathVariable String key,
            @RequestBody UpdatePromptRequest request
    ) {
        return promptConfigService.savePrompt(key, request.getContent());
    }

    /**
     * 重置某个 prompt。
     * @param key prompt key
     * @return 重置后的 prompt 内容详情
     */
    @PostMapping("/prompts/{key}/reset")
    public PromptContent resetPrompt(@PathVariable String key) {
        return promptConfigService.resetPrompt(key);
    }
}
