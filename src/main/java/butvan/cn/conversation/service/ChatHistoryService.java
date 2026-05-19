package butvan.cn.conversation.service;

import butvan.cn.common.security.SessionIdSanitizer;
import butvan.cn.conversation.model.UiMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ObjectMapper objectMapper;

    private final ConversationService conversationService;

    @Value("${data.conversation-dir:./data/conversations}")
    private String conversationDir;

    /**
     * 追加一条 前端 UI 消息
     * @param conversationId
     */
    public void appendMessage(String conversationId, UiMessage uiMessage) {
        String safe_id = SessionIdSanitizer.requireSafe(conversationId);

        try {
            Path dir = conversationDir(safe_id);
            Files.createDirectories(dir);

            Path message_path = messagePath(conversationId);
            String json_line = objectMapper.writeValueAsString(uiMessage);
            try (BufferedWriter writer = Files.newBufferedWriter(
                    message_path,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            )){
                writer.write(json_line);
                writer.newLine();
            }

            // 消息保存成功后，更新会话数据
            conversationService.touchConversation(safe_id);
        } catch (IOException e) {
            throw new IllegalStateException("保存 UI 消息历史失败", e);
        }
    }

    /**
     * 读取某个会话的所有 ui 消息
     * @param conversationId
     * @return
     */
    public List<UiMessage> listMessage(String conversationId) {
        String safe_id = SessionIdSanitizer.requireSafe(conversationId);
        Path message_path = messagePath(safe_id);

        if (!Files.exists(message_path)) {
            return List.of();
        }

        try {
            return Files.lines(message_path,StandardCharsets.UTF_8)
                    .filter(line -> line != null && !line.isBlank())
                    // 将每一行 json 反序列化成 UiMessage
                    .map(this::readMessageLine)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("读取 UI 消息历史失败", e);
        }
    }

    /**
     * 把 ui_message.jsonl 中的一行 json 转换成 UiMessage
     * @param line
     * @return
     */
    private UiMessage readMessageLine(String line) {
        try {
            return objectMapper.readValue(line, UiMessage.class);
        } catch (IOException e) {
            throw new IllegalStateException("解析 UI 消息历史失败：" + line, e);
        }
    }

    /**
     * 根据会话id，计算 ui_message.jsonl 的完整路径
     * @param conversationId
     * @return
     */
    private Path conversationDir(String conversationId) {
        return Path.of(conversationDir).resolve(conversationId);
    }

    /**
     * 根据会话id计算 ui_message.jsonl 的完整路径
     * @param conversationId
     * @return
     */
    private Path messagePath(String conversationId) {
        return conversationDir(conversationId).resolve("ui_messages.jsonl");
    }
}
