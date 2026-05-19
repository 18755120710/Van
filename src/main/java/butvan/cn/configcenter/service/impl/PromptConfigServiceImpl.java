package butvan.cn.configcenter.service.impl;

import butvan.cn.configcenter.model.PromptContent;
import butvan.cn.configcenter.model.PromptInfo;
import butvan.cn.configcenter.service.PromptConfigService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptConfigServiceImpl implements PromptConfigService {

    // 默认 prompt 的 classpath 目录
     @Value("${prompt.locale:/prompt}")
     private String defaultPromptLocale;

     // 用户自定义 prompt 目录
     @Value("${agent-config.prompt-dir:./agent-config/prompts}")
     private String customPromptDir;

    @Override
    public List<PromptInfo> listPrompt() {
        try {
            PathMatchingResourcePatternResolver loader = new PathMatchingResourcePatternResolver();
            Resource[] resources = loader.getResources(defaultPromptLocale);

            return List.of(resources)
                    .stream()
                    .map(this::toPromptInfo)
                    .sorted(Comparator.comparing(PromptInfo::getKey))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("读取 Prompt 列表失败", e);
        }
    }

    @Override
    public PromptContent getPromptContent(String key) {
        return null;
    }

    @Override
    public String getEffectivePrompt(String key) {
        return "";
    }

    @Override
    public PromptContent savePrompt(String key, String content) {
        return null;
    }

    @Override
    public PromptContent resetPrompt(String key) {
        return null;
    }

    private PromptInfo toPromptInfo(Resource resource) {
        String filename = resource.getFilename();

        if (filename == null) {
            throw new IllegalStateException("Prompt 文件名不能为空");
        }

        String key = removeTxtSuffix(filename);
        Path custom_path = customPromptPath(key);
        boolean custom = Files.exists(custom_path);

        return PromptInfo.builder()
                .key(key)
                .filename(filename)
                .name(key)
                .source(custom ? "custom" : "default")
                .updatedAt(custom ? lastModified(custom_path) : null)
                .build();
    }

    private Path customPromptRoot() {
        return Path.of(customPromptDir);
    }

    private Path customPromptPath(String key) {
        return customPromptRoot().resolve(toFileName(key));
    }

    private String toFileName(String key) {
        return normalizedKey(key);
    }

    /**
     * 标准化 prompt key
     * @param key
     * @return
     */
    private String normalizedKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("prompt key cannot be blank");
        }

        String normalized = removeTxtSuffix(key.trim());

        if (!normalized.matches("^[A-Za-z0-9_-]{1,128}$")) {
            throw new IllegalArgumentException("invalid prompt key: " + key);
        }

        return normalized;
    }

    /**
     * 去掉 .txt 后缀
     * @param filename
     * @return
     */
    private String removeTxtSuffix(String filename) {
        return StrUtil.endWith(filename,".txt")
                ? filename.substring(0,filename.length() - 4)
                : filename;
    }

    private Long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return null;
        }
    }
}
