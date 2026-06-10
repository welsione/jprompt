package cn.welsione.jprompt.util;

import java.util.Collection;

/**
 * 模板工具类
 */
public final class TemplateUtils {

    private TemplateUtils() {
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
     * 安全转换为字符串（用于直接字符串拼接）
     * null 值返回空字符串
     */
    public static String toStringOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }
}
