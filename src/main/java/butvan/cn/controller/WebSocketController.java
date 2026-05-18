package butvan.cn.controller;

import butvan.cn.service.WebSocketService;
import butvan.cn.websocket.dto.DialogMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketService webSocketService;


    /**
     * 处理发送到 “/enhanced-dialog” 路径的消息
     * @param message
     * @param headerAccessor
     */
    @MessageMapping("/enhanced-dialog")
    public void enhancedDialog(
            @Payload DialogMessageDTO message,
            SimpMessageHeaderAccessor headerAccessor
            ) {
        log.info("enhanced-dialog:{}",message);
        webSocketService.enhancedDialog(message,headerAccessor);
    }
}
