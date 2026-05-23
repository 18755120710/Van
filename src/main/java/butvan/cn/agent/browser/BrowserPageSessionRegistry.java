package butvan.cn.agent.browser;

import butvan.cn.agent.browser.runtime.PageSession;
import butvan.cn.websocket.session.MessageSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按照 sessionId 复用浏览器状态
 *
 * 可以保留：
 * - 当前打开的页面
 * - 浏览器上下文
 * - cookie
 * - tab 状态
 *
 * 但不会复用 browserAgent memory
 */
@Component
@RequiredArgsConstructor
public class BrowserPageSessionRegistry {

    private final BrowserToolkitFactory browserToolkitFactory;

    private final Map<String, PageSession> page_sessions = new ConcurrentHashMap<>();

    public PageSession getOrCreate(MessageSession session) {
        return page_sessions.computeIfAbsent(session.getSessionId(),
                id -> browserToolkitFactory.createPageSession(session));
    }

    public void remove(String sessionId) {
        PageSession page_session = page_sessions.remove(sessionId);

        if (page_session != null) {
            page_session.close();
        }
    }

    public void evict(String sessionId) {
        page_sessions.remove(sessionId);
    }

}
