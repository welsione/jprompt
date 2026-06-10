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
     * 仅保留变量名，例如 name。
     *
     * <p>适用于 LLM 场景：模型看到裸变量名而非花括号标记，
     * 可将其理解为"此处应由模型填入"的语义提示。
     */
    KEEP_RAW,

    /**
     * 渲染为空字符串。
     */
    EMPTY,

    /**
     * 抛出 TemplateException。
     */
    THROW
}
