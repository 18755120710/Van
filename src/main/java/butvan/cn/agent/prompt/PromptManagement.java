package butvan.cn.agent.prompt;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.PostConstruct;
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
public class PromptManagement {

    @Value("${prompt.locale:/prompt}")
    private String locale;

    private static final Map<String,String> PROMPT_MAP = new ConcurrentHashMap<>();

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
        String key = StrUtil.endWith(name,".txt") ? name : name + ".txt";

        return PROMPT_MAP.get(key);
    }
}
