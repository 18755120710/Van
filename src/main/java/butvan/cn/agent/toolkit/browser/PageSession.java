package butvan.cn.agent.toolkit.browser;

import butvan.cn.session.MessageSession;
import cn.hutool.core.io.resource.ClassPathResource;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PageSession {

    private final PlaywrightManager playwrightManager;
    private final MessageSession messageSession;

    private final PageContentExtractor pageContentExtractor;

    private BrowserContext context;
    private Page currentPage;

    /**
     *  保存最近一次页面标注结果
     *  模型只能通过这些 index 操作页面，避免直接操作任意 selector
     */
    private final Map<Integer, BrowserElement> elementMap = new ConcurrentHashMap<>();


    public String goToUrl(String url) {

        ensureContext();

        currentPage = context.newPage();
        currentPage.navigate(normalizeUrl(url));

        waitPageReady(currentPage);

        return "已打开页面：\n" +
                "title: " + safeTitle(currentPage) + "\n" +
                "url:" + currentPage.url() + "\n" +
                "下一步请调用 view_current_page_status 查看可可交互元素";

    }

    public String viewCurrentPageStatus() {
        Page page = requiredCurrentPage();

        Object result = page.evaluate(loadBuildDomTreeScript());

        if (!(result instanceof Map<?,?> rawMap)) {
                return "页面状态读取失败：buildDomTree.js 没有返回对象";
        }

        elementMap.clear();

        Object elements_obj = rawMap.get("elements");
        if (elements_obj instanceof List<?> elements) {
            for (Object item : elements) {
                if (!(item instanceof Map<?,?> itemMap)) {
                    continue;
                }

                int index = toInt(itemMap.get("index"));
                String selector = toString(itemMap.get("selector"));
                String tag = toString(itemMap.get("tag"));
                String text = toString(itemMap.get("text"));

                if (index > 0 && selector != null) {
                    elementMap.put(index,new BrowserElement(index,selector,tag,text));
                }
            }
        }

        return formatPageStatus(rawMap);
    }

    public String inputText(int index, String text) {
        Page page = requiredCurrentPage();
        BrowserElement element = requiredElement(index);

        Locator locator = page.locator(element.getSelector()).first();

        // 输入先前清空原内容，适用于搜索框，textarea 等场景
        locator.fill("");
        locator.fill(text);

        return "已在元素 [" + index + "] 输入文本" + text;
    }

    public String clickElement(int index) {
        Page page = requiredCurrentPage();
        BrowserElement element = requiredElement(index);

        int page_count_before = context.pages().size();

        Locator locator = page.locator(element.getSelector()).first();
        locator.click();

        waitPageReady(page);

        // 有些点击会打开新 tab ，这里 自动切换到最新的tab
        List<Page> pages = context.pages();
        if (pages.size() > page_count_before) {
            currentPage = pages.get(pages.size() - 1);

            waitPageReady(currentPage);

            return "已点击元素 [" + index + "]，并切换到最新的标签页："
                    + currentPage.url()
                    + "\n下一步请调用 view_current_page_status 重新查看当前页面。";
        }

        currentPage = page;

        return "已点击元素 [" + index + "]。"
                + "\n当前页面：" + currentPage.url()
                + "\n下一步请调用 view_current_page_status 重新查看页面。";
    }

    public String scrollDown(int amount) {
        Page page = requiredCurrentPage();

        int pixels = amount > 0 ? amount : 800;

        page.mouse().wheel(0,pixels);

        return "已向下滚动 " + pixels + " 像素，请调用 view_current_page_status 查看新位置。";
    }

    public String scrollUp(int amount) {
        Page page = requiredCurrentPage();

        int pixels = amount > 0 ? amount : 800;
        page.mouse().wheel(0, -pixels);

        return "已向上滚动 " + pixels + " 像素，请调用 view_current_page_status 查看新位置。";
    }

    public String extractContent(String goal) {
        Page page = requiredCurrentPage();

        String html = page.content();
        String visible_text = page.locator("body").innerText();

        return pageContentExtractor.extract(
                goal,
                page.url(),
                safeTitle(page),
                html,
                visible_text
        );

    }

    public String switchTab(int pageId) {
        ensureContext();

        List<Page> pages = context.pages();

        if (pageId < 1 || pageId > pages.size()) {
            return "切换失败：pageId 超出范围。当前共有 " + pages.size() + " 个标签页。";
        }

        currentPage = pages.get(pageId - 1);
        currentPage.bringToFront();

        return "已切换到标签页 [" + pageId + "]：\n"
                + "title: " + safeTitle(currentPage) + "\n"
                + "url: " + currentPage.url();
    }


    private BrowserElement requiredElement(int index) {
        BrowserElement element = elementMap.get(index);

        if (element == null) {
            throw new IllegalArgumentException(
                    "未找到元素 index=" + index + "。页面变化后请调用 view_current_page_status。"
            );
        }

        return element;
    }

    private Page requiredCurrentPage() {
        if (currentPage == null) {
            throw new IllegalStateException("当前没有打开的页面，请调用 go_to_url");
        }

        return currentPage;
    }

    private String loadBuildDomTreeScript() {
        try {
            ClassPathResource resource = new ClassPathResource("js/buildDomTree.js");
            try (var inputStream = resource.getStream()) {
                return new String(inputStream.readAllBytes(),StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("reading buildDomTree.js failed",e);
        }
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    private String toString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String formatPageStatus(Map<?, ?> rawMap) {
        StringBuilder builder = new StringBuilder();

        builder.append("当前页面状态:\n");
        builder.append("title: ").append(toString(rawMap.get("title"))).append("\n");
        builder.append("url: ").append(toString(rawMap.get("url"))).append("\n");
        builder.append("scrollY: ").append(toString(rawMap.get("scrollY"))).append("\n");
        builder.append("viewportHeight: ").append(toString(rawMap.get("viewportHeight"))).append("\n");
        builder.append("pageHeight: ").append(toString(rawMap.get("pageHeight"))).append("\n\n");

        builder.append("可交互元素：\n");

        for (BrowserElement element : elementMap.values()) {
            builder.append("[")
                    .append(element.getIndex())
                    .append("]")
                    .append(element.getTag())
                    .append(" - ")
                    .append(element.getText())
                    .append("\n");
        }

        if (elementMap.isEmpty()) {
            builder.append("没有发现可交互元素。\n");
        }

        return builder.toString();
    }

    private void ensureContext() {
        if (context == null) {
            Browser browser = playwrightManager.newBrowser();

            this.context = browser.newContext(
                    new Browser.NewContextOptions()
                            // 设置常见的浏览器尺寸
                            .setViewportSize(1440,900)
            );
        }
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is not allow blank!");
        }

        String trimmed = url.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        return "https://" + trimmed;

    }

    private void waitPageReady(Page page) {
        try {
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        } catch (Exception e) {
            // 某些页面会长时间加载资源，基础版不会因为等待失败中断整个任务
        }
    }

    private String safeTitle(Page page) {
        try {
            return page.title();
        } catch (Exception e) {
            return "unknow page title!";
        }
    }

}
