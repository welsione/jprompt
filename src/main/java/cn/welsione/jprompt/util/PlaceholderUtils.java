package cn.welsione.jprompt.util;

import cn.welsione.jprompt.MissingVariablePolicy;
import cn.welsione.jprompt.engine.TemplateEngine;

import java.util.Collections;
import java.util.Map;

/**
 * 占位符工具类
 * 支持：变量插值、条件判断、循环迭代、函数调用、表达式运算、默认值
 */
public final class PlaceholderUtils {

    private PlaceholderUtils() {
    }

    /**
     * 渲染模板
     *
     * @param template     模板内容
     * @param placeholders 占位符映射
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> placeholders) {
        return render(template, placeholders, Collections.emptyMap());
    }

    /**
     * 渲染模板（带函数注册）
     *
     * @param template     模板内容
     * @param placeholders 占位符映射
     * @param functions    函数映射
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> placeholders,
                                Map<String, TemplateEngine.TemplateFunction> functions) {
        return render(template, placeholders, functions, MissingVariablePolicy.KEEP_PLACEHOLDER);
    }

    /**
     * 渲染模板（带函数注册和缺失变量策略）
     *
     * @param template              模板内容
     * @param placeholders          占位符映射
     * @param functions             函数映射
     * @param missingVariablePolicy 缺失变量策略
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> placeholders,
                                Map<String, TemplateEngine.TemplateFunction> functions,
                                MissingVariablePolicy missingVariablePolicy) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        RenderContext context = new RenderContext(
                placeholders == null ? Collections.emptyMap() : placeholders,
                functions == null ? Collections.emptyMap() : functions,
                missingVariablePolicy == null ? MissingVariablePolicy.KEEP_PLACEHOLDER : missingVariablePolicy
        );
        return new TemplateParser(template).parse().render(context);
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
