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
        // 1. 创建当前 session 的页面会话
        PageSession pageSession = new PageSession(
                playwrightManager,
                session,
                pageContentExtractor
        );

        // 2. 创建浏览器工具对象，工具内部会调用 pageSession
        BrowserToolkit browserToolkit = new BrowserToolkit(pageSession);

        // 3. 创建 AgentScope Toolkit
        Toolkit toolkit = new Toolkit();

        // 4. 把 BrowserToolkit 里的 @Tool 方法注册进去
        toolkit.registration()
                .tool(browserToolkit)
                .apply();

        // 5. 同时返回 toolkit 和 pageSession
        return new BrowserToolkitRuntime(toolkit, pageSession);
    }

    public record BrowserToolkitRuntime(
            Toolkit toolkit,        // AgentScope 工具集合
            PageSession pageSession // 当前浏览器页面会话
    ) {
    }
}
