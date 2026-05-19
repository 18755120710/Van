package butvan.cn.configcenter.service.impl;

import butvan.cn.configcenter.model.PromptContent;
import butvan.cn.configcenter.model.PromptInfo;
import butvan.cn.configcenter.service.PromptConfigService;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    /**
     * 查询所有可管理的 prompt 列表
     * @return
     */
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

    /**
     * 获取某个 prompt 当前实际生效的内容
     * @param key
     * @return
     */
    @Override
    public PromptContent getPromptContent(String key) {
        String safe_key = normalizedKey(key);
        Path custom_path = customPromptPath(safe_key);

        if (Files.exists(custom_path)) {
            return PromptContent.builder()
                    .key(safe_key)
                    .filename(toFileName(key))
                    .content(readCustomPrompt(custom_path))
                    .source("custom")
                    .updatedAt(lastModified(custom_path))
                    .build();
        }

        return PromptContent.builder()
                .key(safe_key)
                .filename(toFileName(safe_key))
                .content(readDefaultPrompt(safe_key))
                .source("default")
                .updatedAt(null)
                .build();
    }

    /**
     * 获取某个 prompt 当前真正生效的文本
     * @param key
     * @return
     */
    @Override
    public String getEffectivePrompt(String key) {
        return getPromptContent(key).getContent();
    }

    /**
     * 保存自定义 prompt
     * @param key
     * @param content
     * @return
     */
    @Override
    public PromptContent savePrompt(String key, String content) {
        String safe_key = normalizedKey(key);

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("prompt content cannot be blank");
        }

        readDefaultPrompt(safe_key);

        try {
            Files.createDirectories(customPromptRoot());

            Path path = customPromptPath(safe_key);
            Files.writeString(path, content, StandardCharsets.UTF_8);

            log.info("custom prompt saved, key={}, path={}", safe_key, path);

            return getPromptContent(safe_key);
        } catch (IOException e) {
            throw new IllegalStateException("保存自定义 Prompt 失败：" + safe_key, e);
        }
    }

    /**
     * 充值 Prompt
     * @param key
     * @return
     */
    @Override
    public PromptContent resetPrompt(String key) {
        String safe_key = normalizedKey(key);

        readDefaultPrompt(safe_key);

        try {
            Files.deleteIfExists(customPromptPath(safe_key));

            log.info("custom prompt reset, key={}",safe_key);

            return getPromptContent(safe_key);
        } catch (IOException e) {
            throw new IllegalStateException("充值 Prompt 失败：" + safe_key, e);
        }
    }

    /**
     * 读取默认 prompt 文件
     * @param key
     * @return
     */
    private String readDefaultPrompt(String key) {
        String safe_key = normalizedKey(key);
        String file_name = toFileName(safe_key);
        String location = "classpath:" + defaultPromptLocale + "/" + file_name;

        try {
            PathMatchingResourcePatternResolver loader = new PathMatchingResourcePatternResolver();
            Resource resource = loader.getResource(location);

            if (!resource.exists()) {
                throw new IllegalStateException("默认 Prompt 不存在：" + safe_key);
            }

            return IoUtil.readUtf8(resource.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("读取默认 prompt 失败：" + safe_key, e);
        }
    }

    /**
     * 读取自定义 prompt 文件
     * @param path
     * @return
     */
    private String readCustomPrompt(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取自定义prompt失败" + path, e);
        }
    }

    /**
     * 把默认 prompt 资源换成列表项
     * @param resource
     * @return
     */
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

    /**
     * 自定义 prompt 根目录
     * @return
     */
    private Path customPromptRoot() {
        return Path.of(customPromptDir);
    }

    /**
     * 自定义 prompt 文件路径
     * @param key
     * @return
     */
    private Path customPromptPath(String key) {
        return customPromptRoot().resolve(toFileName(key));
    }

    /**
     * 把 prompt key 转换为文件名
     * @param key
     * @return
     */
    private String toFileName(String key) {
        return normalizedKey(key) + ".txt";
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

    /**
     * 获取文件最后修改时间
     * @param path
     * @return
     */
    private Long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return null;
        }
    }
}
