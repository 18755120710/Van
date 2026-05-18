package butvan.cn.websocket.session;

import butvan.cn.websocket.dto.DialogMessageDTO;

public interface MessageSession {

    /**
     * 接收前端消息，写入会话缓冲区
     */
    void receiveMessage(DialogMessageDTO messageDTO);

    /**
     * 阻塞读取用户消息文本
     */
    String readMessage();

    /**
     * 发送普通文本消息到前端
     */
    void sendMessage(String text);

    /**
     * 发送结构化消息到前端，例如 fileUrl / openUrl
     */
    void sendMessage(DialogMessageDTO messageDTO);

    /**
     * 获取 会话id
     * @return sessionId
     */
    String getSessionId();
}
