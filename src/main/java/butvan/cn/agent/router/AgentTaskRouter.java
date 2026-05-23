package butvan.cn.agent.router;

import org.springframework.stereotype.Component;

/**
 * 任务路由器
 */
@Component
public class AgentTaskRouter {

    public AgentRouterType route(String task) {
        if (task == null || task.isBlank()) {
            return AgentRouterType.SIMPLE_CHAT;
        }

        String text = task.trim().toLowerCase();

        // 明确需要实时信息或网页能力的关键词，走 plan
        if (containsAny(text,
                "搜索", "查一下", "查询", "浏览", "网页", "网站",
                "打开", "点击", "滚动", "提取", "链接", "新闻",
                "天气", "价格", "当前", "最新", "实时",
                "search", "browse", "website", "click", "latest"
                )) {
            return AgentRouterType.PLANNER_TASK;
        }

        // 默认走 simple
        return AgentRouterType.SIMPLE_CHAT;
    }


    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
