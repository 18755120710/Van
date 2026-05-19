package butvan.cn.conversation.service;

import butvan.cn.common.security.SessionIdSanitizer;
import butvan.cn.conversation.model.ConversationMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 会话管理
 */
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ObjectMapper objectMapper;

    @Value("${data.conversation-dir:./data/conversations}")
    private String conversationDir;

    /**
     * 创建一个新会话
     * @return
     */
    public ConversationMeta createConversation() {
        String conversation_id = UUID.randomUUID().toString();

        long now = System.currentTimeMillis();

        ConversationMeta meta = ConversationMeta.builder()
                .conversationId(conversation_id)
                .title("新对话")
                .titleGenerated(false)
                .createAt(now)
                .updateAt(now)
                .messageCount(0)
                .deleted(false)
                .build();

        saveMeta(meta);

        return meta;
    }

    /**
     * 查询历史会话列表
     * @return
     */
    public List<ConversationMeta> listConversation() {
        Path root = Path.of(conversationDir);

        if (!Files.exists(root)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(root)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(this::readMetaQuietly)
                    .filter(meta -> meta != null)
                    .filter(meta -> !Boolean.TRUE.equals(meta.getDeleted()))
                    .sorted(Comparator.comparing(ConversationMeta::getUpdateAt).reversed())
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("读取历史会话列表失败", e);
        }
    }

    /**
     * 更新会话活跃时间和消息数量
     * @param conversationId
     */
    public void touchConversation(String conversationId) {
        ConversationMeta meta = getOrCreate(conversationId);

        meta.setUpdateAt(System.currentTimeMillis());
        int old_count = meta.getMessageCount() == null ? 0 : meta.getMessageCount();
        meta.setMessageCount(old_count + 1);

        saveMeta(meta);
    }

    /**
     * 如果会话标题还是“新对话”，就用用户第一条消息生成标题。
     * @param conversationId
     * @param userText
     */
    public void initTitleIfNecessary(String conversationId, String userText) {
        ConversationMeta meta = getOrCreate(conversationId);

        if (meta.getTitle() != null && !"新对话".equals(meta.getTitle())) {
            return;
        }

        meta.setTitle(generateSimpleTitle(userText));
        meta.setTitleGenerated(false);
        meta.setUpdateAt(System.currentTimeMillis());

        saveMeta(meta);
    }

    /**
     * 根据用户输入生成一个简单的标题
     * @param text
     * @return
     */
    private String generateSimpleTitle(String text) {
        if (text == null || text.isBlank()) {
            return "新对话";
        }

        String normalized = text.trim().replaceAll("\\s+", " ");
        int maxLength = 20;

        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, maxLength) + "...";
    }

    /**
     * 获取指定会话的 meta 信息，如果不存在，就创建一份默认 meta
     * @param conversationId
     * @return
     */
    private ConversationMeta getOrCreate(String conversationId) {

        Path meta_path = metaPath(conversationId);

        if (Files.exists(meta_path)) {
            return readMeta(meta_path);
        }

        long now = System.currentTimeMillis();

        ConversationMeta meta = ConversationMeta.builder()
                .conversationId(conversationId)
                .title("新对话")
                .titleGenerated(false)
                .createAt(now)
                .updateAt(now)
                .messageCount(0)
                .deleted(false)
                .build();

        saveMeta(meta);

        return meta;
    }

    /**
     * 尝试从某个会话目录中读取 meta.json
     * @param conversationDir
     * @return
     */
    private ConversationMeta readMetaQuietly(Path conversationDir) {
        Path metaPath = conversationDir.resolve("meta.json");

        if (!Files.exists(metaPath)) {
            return null;
        }

        return readMeta(metaPath);
    }

    /**
     * 从指定路径读取 meta
     * @param metaPath
     * @return
     */
    private ConversationMeta readMeta(Path metaPath) {
        try {
            return objectMapper.readValue(metaPath.toFile(), ConversationMeta.class);
        } catch (IOException e) {
            throw new IllegalStateException("读取会话元数据失败：" + metaPath, e);
        }
    }

    /**
     * 保存 meta.json
     * @param meta
     */
    private void saveMeta(ConversationMeta meta) {
        try {
            Path dir = conversationDir(meta.getConversationId());
            Files.createDirectories(dir);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(metaPath(meta.getConversationId()).toFile(), meta);
        }catch (IOException e) {
            throw new IllegalStateException("保存会话元数据失败", e);
        }
    }

    /**
     * 根据会话id计算当前会话的业务数据目录
     * @param conversationId
     * @return
     */
    private Path conversationDir(String conversationId) {
        String safe_id = SessionIdSanitizer.requireSafe(conversationId);
        return Path.of(conversationDir).resolve(safe_id);
    }

    /**
     * 根据会话id，计算完整的 meta.json 路径
     * @param conversationId
     * @return
     */
    private Path metaPath(String conversationId) {
        return conversationDir(conversationId).resolve("meta.json");
    }
}
