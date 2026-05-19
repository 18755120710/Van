package butvan.cn.controller;

import butvan.cn.conversation.model.ConversationMeta;
import butvan.cn.conversation.model.UiMessage;
import butvan.cn.conversation.service.ChatHistoryService;
import butvan.cn.conversation.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    private final ChatHistoryService chatHistoryService;

    /**
     * 创建一个新的业务会话
     * @return
     */
    @PostMapping
    public ConversationMeta createConversation() {
        return conversationService.createConversation();
    }

    /**
     * 查询历史会话列表
     * @return
     */
    @GetMapping
    public List<ConversationMeta> listConversation() {
        return conversationService.listConversation();
    }

    /**
     * 查询某个会话的 ui 聊天消息
     * @param conversationId
     * @return
     */
    @GetMapping("/{conversationId}/messages")
    public List<UiMessage> listMessages(@PathVariable String conversationId) {
        return chatHistoryService.listMessage(conversationId);
    }
}
