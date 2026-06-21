package cn.welsione.jprompt;

import cn.welsione.jprompt.engine.TemplateEngine;

/**
 * 提示词模板类
 *
 * 泛型参数 T 指定数据类型，编译时进行类型检查
 */
public class JPrompt<T> {

    private final String content;
    private final Class<T> dataClass;
    private final boolean isTemplate;
    private final TemplateEngine engine;

    JPrompt(String content, Class<T> dataClass, boolean isTemplate, TemplateEngine engine) {
        this.content = content;
        this.dataClass = dataClass;
        this.isTemplate = isTemplate;
        this.engine = engine;
    }

    /**
     * 获取提示词内容
     *
     * @return 提示词内容
     */
    public String get() {
        return content;
    }

    /**
     * 构建提示词（仅模板类型有效）
     *
     * @param data 模板数据，必须是泛型参数 T 指定的类型
     * @return 渲染后的提示词
     * @apiNote 受泛型擦除限制，当 T 为带泛型参数的类型（如 {@code Map<String, Object>}）
     *          时，运行时仅检查原始类型（如 {@code Map.class}），子类实例也能通过检查。
     *          对于简单 POJO 类型不受影响。
     */
    public String build(T data) {
        if (!isTemplate) {
            return content;
        }
        if (data != null && !dataClass.isInstance(data)) {
            throw new TemplateException("模板数据类型不匹配，期望: " + dataClass.getName()
                    + "，实际: " + data.getClass().getName());
        }
        return engine.render(content, data);
    }

    /**
     * 创建静态提示词（不可变）
     */
    public static JPrompt<Void> get(String path) {
        return JPromptFactory.getInstance().get(path);
    }

    /**
     * 创建模板提示词（可变）
     */
    public static <T> JPrompt<T> template(String path, Class<T> dataClass) {
        return JPromptFactory.getInstance().template(path, dataClass);
    }

    /**
     * 从字符串内容创建模板提示词（可变），不经过文件加载与路径缓存。
     *
     * <p>用于运行时从数据库等非 classpath 来源读取模板文本的场景。
     * 与 {@link #template(String, Class)} 的区别仅在于内容来源：前者从 classpath 文件加载，
     * 本方法直接使用传入的 {@code content}，渲染行为完全一致。
     *
     * @param content   模板文本（含 {@code {{}}} 占位符）
     * @param dataClass 数据类型
     * @param <T>       数据类型
     * @return JPrompt<T> 实例
     */
    public static <T> JPrompt<T> templateInline(String content, Class<T> dataClass) {
        return JPromptFactory.getInstance().templateInline(content, dataClass);
    }
}
