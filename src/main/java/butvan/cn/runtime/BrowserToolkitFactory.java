package butvan.cn.runtime;

import butvan.cn.agent.toolkit.browser.BrowserToolkit;
import butvan.cn.agent.toolkit.browser.PageContentExtractor;
import butvan.cn.agent.toolkit.browser.PageSession;
import butvan.cn.agent.toolkit.browser.PlaywrightManager;
import butvan.cn.session.MessageSession;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrowserToolkitFactory {

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
}
