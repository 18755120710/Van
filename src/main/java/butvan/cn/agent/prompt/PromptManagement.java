package butvan.cn.agent.prompt;

import butvan.cn.configcenter.service.PromptConfigService;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromptManagement {

    @Value("${prompt.locale:/prompt}")
    private String locale;

    private static final Map<String,String> PROMPT_MAP = new ConcurrentHashMap<>();

    private final PromptConfigService promptConfigService;

    /**
     * initialize operations: read all files in the prompt directory and save their contents to PROMPT_MAP
     * @throws IOException
     */
    @PostConstruct
    void init() throws IOException {
        String pattern = StrUtil.format("classpath:{}/*.txt",this.locale);
        PathMatchingResourcePatternResolver loader = new PathMatchingResourcePatternResolver();
        Resource[] resources = loader.getResources(pattern);

        for (Resource resource : resources) {
            PROMPT_MAP.put(resource.getFilename(), IoUtil.readUtf8(resource.getInputStream()));
            log.info("loading prompt ({}) file success", resource.getFilename());
        }

    }


    /**
     * get prompt context by name
     * @param name prompt name
     * @return prompt context
     */
    public String getPrompt(String name) {
        String key = normalizeKey(name);

        try {
            // 优先走 PromptConfigService
            return promptConfigService.getEffectivePrompt(key);
        } catch (Exception e) {
            /*
             * 如果配置中心读取失败，不让 Agent 直接崩掉。
             * 回退到启动时加载的默认 prompt。
             */
            log.warn("read effective prompt failed, fallback to startup cache, key={}", key, e);
            return PROMPT_MAP.get(toFileName(key));
        }
    }

    /**
     * 标准化 prompt key
     * @param name
     * @return
     */
    private String normalizeKey(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("prompt name cannot be blank");
        }

        String trimmed = name.trim();

        return StrUtil.endWith(trimmed,".txt")
                ? trimmed.substring(0, trimmed.length() - 4)
                : trimmed;
    }

    /**
     * 把 prompt key 转换成文件名
     * @param key
     * @return
     */
    private String toFileName(String key) {
        return key + ".txt";
    }
}
