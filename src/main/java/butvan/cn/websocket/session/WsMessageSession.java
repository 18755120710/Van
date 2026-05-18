package butvan.cn.websocket.session;

import butvan.cn.websocket.dto.DialogMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

@Slf4j
public class WsMessageSession implements MessageSession {

    private final SimpMessagingTemplate messagingTemplate;

    private final BlockingQueue<DialogMessageDTO> buffer =
            new ArrayBlockingQueue<>(1024);

    private final Supplier<String> sessionIdProvider;

    private final String destination;

    public WsMessageSession(
            Supplier<String> sessionIdProvider,
            String destination,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.sessionIdProvider = sessionIdProvider;
        this.destination = destination;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void receiveMessage(DialogMessageDTO messageDTO) {
        try {
            buffer.put(messageDTO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("写入会话消息失败", e);
        }
    }

    @Override
    public String readMessage() {
        try {
            return buffer.take().getText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("读取会话消息失败", e);
        }
    }

    @Override
    public void sendMessage(String text) {
        sendMessage(DialogMessageDTO.builder()
                .type(DialogMessageDTO.TYPE_SERVER)
                .text(text)
                .build());
    }

    @Override
    public void sendMessage(DialogMessageDTO messageDTO) {
        messageDTO.setType(DialogMessageDTO.TYPE_SERVER);
        sendToClient(messageDTO);
    }

    @Override
    public String getSessionId() {
        return sessionIdProvider.get();
    }

    private void sendToClient(DialogMessageDTO messageDTO) {
        try {
            String sessionId = sessionIdProvider.get();

            SimpMessageHeaderAccessor headerAccessor =
                    SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

            headerAccessor.setSessionId(sessionId);
            headerAccessor.setLeaveMutable(true);

            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    destination,
                    messageDTO,
                    headerAccessor.getMessageHeaders()
            );
        } catch (Exception e) {
            log.error("发送 WebSocket 消息失败", e);
        }
    }
}
