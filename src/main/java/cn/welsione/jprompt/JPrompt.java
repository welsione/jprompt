package cn.welsione.jprompt;

/**
 * 提示词模板类
 *
 * 泛型参数 T 指定数据类型，编译时进行类型检查
 */
public class JPrompt<T> {

    private final String content;
    private final Class<T> dataClass;
    private final boolean isTemplate;

    JPrompt(String content, Class<T> dataClass, boolean isTemplate) {
        this.content = content;
        this.dataClass = dataClass;
        this.isTemplate = isTemplate;
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
     */
    public String build(T data) {
        if (!isTemplate) {
            return content;
        }
        return JPromptFactory.INSTANCE.getEngine().render(content, data);
    }

    /**
     * 创建静态提示词（不可变）
     */
    public static JPrompt<Void> get(String path) {
        return JPromptFactory.INSTANCE.get(path);
    }

    /**
     * 创建模板提示词（可变）
     */
    public static <T> JPrompt<T> template(String path, Class<T> dataClass) {
        return JPromptFactory.INSTANCE.template(path, dataClass);
    }
}
