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
     * 转义 replacement 字符串中的特殊字符
     */
    public static String escapeReplacement(String value) {
        return value.replace("\\", "\\\\").replace("$", "\\$");
    }

    /**
     * 获取最大嵌套深度
     */
    public static int getMaxNestingDepth() {
        return MAX_NESTING_DEPTH;
    }
}
