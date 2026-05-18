package butvan.cn.agent.browser.runtime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrowserElement {

    /**
     * 页面元素编号，模型后续会用这个 index 点击或输入
     */
    private int index;

    /**
     * 使用 buildDomTree.js 注入的稳定选择器
     */
    private String selector;

    /**
     * 元素标签
     */
    private String tag;

    /**
     * 元素展示的文本
     */
    private String text;
}
