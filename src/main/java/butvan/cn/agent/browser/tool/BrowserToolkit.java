package butvan.cn.agent.browser.tool;

import butvan.cn.agent.browser.runtime.PageSession;


import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BrowserToolkit {

    private final PageSession pageSession;

    @Tool(name = "go_to_url", description = "打开指定网页 URL。打开后需要调用 view_current_page_status 查看页面状态。")
    public String goToUrl(
            @ToolParam(name = "url", description = "完整网页地址，例如 https://www.baidu.com")
            String url
    ) {
        return pageSession.goToUrl(url);
    }

    @Tool(name = "view_current_page_status", description = "查看当前页面 URL、标题、滚动位置和可交互元素列表。")
    public String viewCurrentPageStatus() {
        return pageSession.viewCurrentPageStatus();
    }

    @Tool(name = "input_text", description = "向指定编号的输入框输入文本。输入前必须先调用 view_current_page_status 获取元素编号。")
    public String inputText(
            @ToolParam(name = "index", description = "页面元素编号")
            int index,
            @ToolParam(name = "text", description = "要输入的文本")
            String text
    ) {
        return pageSession.inputText(index, text);
    }

    @Tool(name = "click_element", description = "点击指定编号的页面元素。点击前必须先调用 view_current_page_status 获取元素编号。")
    public String clickElement(
            @ToolParam(name = "index", description = "页面元素编号")
            int index
    ) {
        return pageSession.clickElement(index);
    }

    @Tool(name = "scroll_down", description = "向下滚动当前页面。")
    public String scrollDown(
            @ToolParam(name = "amount", description = "滚动像素，默认可传 800", required = false)
            int amount
    ) {
        return pageSession.scrollDown(amount);
    }

    @Tool(name = "scroll_up", description = "向上滚动当前页面。")
    public String scrollUp(
            @ToolParam(name = "amount", description = "滚动像素，默认可传 800", required = false)
            int amount
    ) {
        return pageSession.scrollUp(amount);
    }

    @Tool(name = "extract_content", description = "提取当前页面正文文本，用于根据目标分析网页内容。")
    public String extractContent(
            @ToolParam(name = "goal", description = "提取目标，例如：提取搜索结果前 10 条")
            String goal
    ) {
        return pageSession.extractContent(goal);
    }

    @Tool(name = "switch_tab", description = "切换浏览器标签页，pageId 从 1 开始。")
    public String switchTab(
            @ToolParam(name = "page_id", description = "标签页编号，从 1 开始")
            int pageId
    ) {
        return pageSession.switchTab(pageId);
    }
}
