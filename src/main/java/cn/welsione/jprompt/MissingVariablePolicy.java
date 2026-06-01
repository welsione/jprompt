package cn.welsione.jprompt;

/**
 * 缺失变量处理策略。
 */
public enum MissingVariablePolicy {

    /**
     * 保留原始占位符，例如 {{name}}。
     */
    KEEP_PLACEHOLDER,

    /**
     * 渲染为空字符串。
     */
    EMPTY,

    /**
     * 抛出 TemplateException。
     */
    THROW
}
