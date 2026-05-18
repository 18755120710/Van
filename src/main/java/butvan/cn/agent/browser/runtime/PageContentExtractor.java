package butvan.cn.agent.browser.runtime;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.jsoup.Jsoup;

import java.util.stream.Collectors;

@Component
public class PageContentExtractor {

    private static final int MAX_TEXT_LENGTH = 12000;
    private static final int MAX_LINKS = 20;
    private static final int MAX_IMAGES = 20;
    private static final int MAX_TABLES = 5;

    public String extract(
            String goal,
            String url,
            String title,
            String html,
            String visibleText
    ) {
        Document document = Jsoup.parse(html, url);

        // 去掉无意义的内容，避免脚本，样式污染模型上下文
        document.select("script, style, noscript, svg, canvas").remove();

        String mainText = normalizeText(
                document.body() != null
                        ? document.body().text()
                        : visibleText
        );

        if (mainText.isBlank()) {
            mainText = normalizeText(visibleText);
        }

        String links = extractLinks(document);
        String images = extractImages(document);
        String tables = extractTables(document);

        StringBuilder builder = new StringBuilder();

        builder.append("页面内容提取结果\n");
        builder.append("提取目标：").append(goal).append("\n");
        builder.append("标题：").append(title).append("\n");
        builder.append("URL：").append(url).append("\n\n");

        builder.append("正文文本：\n");
        builder.append(limit(mainText, MAX_TEXT_LENGTH)).append("\n\n");

        if (!links.isBlank()) {
            builder.append("重要链接：\n");
            builder.append(links).append("\n\n");
        }

        if (!images.isBlank()) {
            builder.append("图片信息：\n");
            builder.append(images).append("\n\n");
        }

        if (!tables.isBlank()) {
            builder.append("表格内容：\n");
            builder.append(tables).append("\n\n");
        }

        return builder.toString();

    }

    private String extractLinks (Document document) {
        return document.select("a[href]").stream()
                .limit(MAX_LINKS)
                .map(link -> {
                    String text = normalizeText(link.text());
                    String href = link.absUrl("href");

                    if (text.isBlank()) {
                        text = link.attr("title");
                    }

                    if (text.isBlank() || href.isBlank()) {
                        return "";
                    }

                    return "- " + text + " ->" + href;
                })
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private String extractImages(Document document) {
        return document.select("img[src]").stream()
                .limit(MAX_IMAGES)
                .map(img -> {
                    String alt = normalizeText(img.attr("alt"));
                    String src = img.absUrl("src");

                    if (src.isBlank()) {
                        return "";
                    }

                    if (alt.isBlank()) {
                        alt = "未提供 alt";
                    }

                    return "- " + alt + " -> " + src;
                })
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private String extractTables(Document document) {
        return document.select("table").stream()
                .limit(MAX_TABLES)
                .map(this::tableToText)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining("\n\n"));
    }

    private String tableToText(Element table) {
        StringBuilder builder = new StringBuilder();

        for (Element row : table.select("tr")) {
            String rowText = row.select("th, td").stream()
                    .map(Element::text)
                    .map(this::normalizeText)
                    .filter(text -> !text.isBlank())
                    .collect(Collectors.joining(" | "));

            if (!rowText.isBlank()) {
                builder.append(rowText).append("\n");
            }
        }

        return builder.toString().trim();
    }

    private String limit(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text == null ? "" : text;
        }

        return text.substring(0, maxLength) + "\n\n[内容过长，已截断]";
    }


    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("\\s+", " ").trim();
    }
}
