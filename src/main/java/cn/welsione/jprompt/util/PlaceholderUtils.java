package cn.welsione.jprompt.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 占位符工具类
 */
public final class PlaceholderUtils {

    // 双花括号占位符正则表达式：{{key}} 或 {{key.subkey}}
    private static final Pattern DOUBLE_BRACE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    // 单花括号占位符正则表达式：{key} 或 {key.subkey}
    private static final Pattern SINGLE_BRACE_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    private PlaceholderUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 渲染模板中的占位符，支持两种格式：
     * 1. {{key}} - 双花括号格式（推荐）
     * 2. {key} - 单花括号格式（兼容现有模板）
     *
     * @param template    模板内容
     * @param placeholders 占位符映射
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> placeholders) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (placeholders == null || placeholders.isEmpty()) {
            return template;
        }

        // 先处理双花括号格式（精确匹配）
        String result = renderWithPattern(template, placeholders, DOUBLE_BRACE_PATTERN, "{{", "}}");
        // 再处理单花括号格式（兼容现有模板）
        result = renderWithPattern(result, placeholders, SINGLE_BRACE_PATTERN, "{", "}");

        return result;
    }

    private static String renderWithPattern(String template, Map<String, Object> placeholders,
                                            Pattern pattern, String openBrace, String closeBrace) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            Object value = resolvePlaceholder(placeholder, placeholders);
            String replacement = value != null ? escapeReplacement(value.toString()) : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 解析占位符，支持嵌套路径如 user.name
     */
    private static Object resolvePlaceholder(String placeholder, Map<String, Object> placeholders) {
        String[] parts = placeholder.split("\\.");

        Object current = placeholders;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * 转义 replacement 字符串中的特殊字符
     */
    private static String escapeReplacement(String value) {
        // Matcher.appendReplacement 需要对 $ 和 \ 进行转义
        return value.replace("\\", "\\\\").replace("$", "\\$");
    }

    /**
     * 检查模板中是否包含某个占位符（双花括号格式）
     */
    public static boolean containsPlaceholder(String template, String placeholder) {
        if (template == null || placeholder == null) {
            return false;
        }
        return template.contains("{{" + placeholder + "}}");
    }
}
