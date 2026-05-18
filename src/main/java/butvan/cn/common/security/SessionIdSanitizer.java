package butvan.cn.common.security;

import java.util.regex.Pattern;

public final class SessionIdSanitizer {

    private static final Pattern SAFE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,128}$"); // 只允许安全字符

    public static String requireSafe(String sessionId) { // 校验并返回安全 sessionId
        if (sessionId == null) { // 如果 sessionId 为空
            throw new IllegalArgumentException("sessionId cannot be null"); // 抛出异常
        }

        if (!SAFE_PATTERN.matcher(sessionId).matches()) { // 如果 sessionId 不符合规则
            throw new IllegalArgumentException("Invalid sessionId: " + sessionId); // 阻止继续写文件
        }

        return sessionId; // 校验通过后返回原值
    }

    private SessionIdSanitizer() { // 私有构造方法
    }
}