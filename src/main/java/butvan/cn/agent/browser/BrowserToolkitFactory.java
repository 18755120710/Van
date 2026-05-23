package butvan.cn.agent.browser;

import butvan.cn.agent.browser.runtime.PageContentExtractor;
import butvan.cn.agent.browser.runtime.PageSession;
import butvan.cn.agent.browser.runtime.PlaywrightManager;
import butvan.cn.agent.browser.tool.BrowserToolkit;
import butvan.cn.websocket.session.MessageSession;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class    BrowserToolkitFactory {

    private final PlaywrightManager playwrightManager;
    private final PageContentExtractor pageContentExtractor;

    public Toolkit createForSession(MessageSession session) {
        Toolkit toolkit = new Toolkit();

        PageSession pageSession = new PageSession(
                playwrightManager,
                session,
                pageContentExtractor
        );

        BrowserToolkit browserToolkit = new BrowserToolkit(pageSession);

        toolkit.registration()
                .tool(browserToolkit)
                .apply();

        return toolkit;
    }

    public BrowserToolkitRuntime createRuntimeForSession(MessageSession session) {
        PageSession page_session = createPageSession(session);
        Toolkit toolkit = createToolkit(page_session);

        return new BrowserToolkitRuntime(toolkit,page_session);
    }

    public PageSession createPageSession(MessageSession session) {
        return new PageSession(
                playwrightManager,
                session,
                pageContentExtractor
        );
    }

    public Toolkit createToolkit(PageSession session) {
        Toolkit toolkit = new Toolkit();

        toolkit.registration()
                .tool(new BrowserToolkit(session))
                .apply();

        return toolkit;
    }

    public record BrowserToolkitRuntime(
            Toolkit toolkit,        // AgentScope 工具集合
            PageSession pageSession // 当前浏览器页面会话
    ) {
    }
}
