package cn.welsione.jprompt.util;

import java.util.Collection;

/**
 * 模板工具类
 */
public final class TemplateUtils {

    private static final int MAX_NESTING_DEPTH = 10;

    private TemplateUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 判断值是否为真
     *
     * @param value 待判断的值
     * @return 是否为真
     */
    public static boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }
        if (value instanceof Collection) {
            return !((Collection<?>) value).isEmpty();
        }
        return true;
    }

    /**
     * 转义 replacement 字符串中的特殊字符（用于 appendReplacement）
     */
    public static String escapeReplacement(String value) {
        return value.replace("\\", "\\\\").replace("$", "\\$");
    }

    /**
     * 安全转换为字符串（用于直接字符串拼接）
     * null 值返回空字符串
     */
    public static String toStringOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * 获取最大嵌套深度
     */
    public static int getMaxNestingDepth() {
        return MAX_NESTING_DEPTH;
    }
}
