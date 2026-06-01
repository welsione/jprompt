package cn.welsione.jprompt;

import cn.welsione.jprompt.engine.ReflectiveTemplateEngine;
import cn.welsione.jprompt.engine.TemplateEngine;
import cn.welsione.jprompt.loader.ClasspathTemplateLoader;
import cn.welsione.jprompt.loader.TemplateLoader;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPrompt 工厂类。
 */
public final class JPromptFactory {

    public static final JPromptFactory INSTANCE = JPromptFactory.builder().build();

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    private final TemplateEngine engine;
    private final TemplateLoader loader;
    private final boolean cacheEnabled;

    private JPromptFactory(Builder builder) {
        this.engine = builder.engine;
        this.loader = builder.loader;
        this.cacheEnabled = builder.cacheEnabled;
    }

    /**
     * 创建工厂构建器。
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建静态提示词（不可变）
     *
     * @param path 提示词文件路径
     * @return JPrompt<Void> 实例
     */
    public JPrompt<Void> get(String path) {
        String content = loadTemplate(path);
        return new JPrompt<>(content, Void.class, false, engine);
    }

    /**
     * 创建模板提示词（可变）
     *
     * @param path      模板路径
     * @param dataClass 数据类型
     * @param <T>       数据类型
     * @return JPrompt<T> 实例
     */
    public <T> JPrompt<T> template(String path, Class<T> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass must not be null");
        String content = loadTemplate(path);
        return new JPrompt<>(content, dataClass, true, engine);
    }

    /**
     * 获取模板引擎。
     */
    public TemplateEngine getEngine() {
        return engine;
    }

    /**
     * 清空模板缓存。
     */
    public void clearCache() {
        templateCache.clear();
    }

    /**
     * 移除指定模板缓存。
     */
    public void evictCache(String path) {
        templateCache.remove(path);
    }

    private String loadTemplate(String path) {
        Objects.requireNonNull(path, "path must not be null");
        if (!cacheEnabled) {
            return loader.load(path);
        }
        return templateCache.computeIfAbsent(path, loader::load);
    }

    public static final class Builder {

        private TemplateEngine engine = new ReflectiveTemplateEngine();
        private TemplateLoader loader = new ClasspathTemplateLoader();
        private boolean cacheEnabled = true;

        private Builder() {
        }

        public Builder engine(TemplateEngine engine) {
            this.engine = Objects.requireNonNull(engine, "engine must not be null");
            return this;
        }

        public Builder loader(TemplateLoader loader) {
            this.loader = Objects.requireNonNull(loader, "loader must not be null");
            return this;
        }

        public Builder cacheEnabled(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
            return this;
        }

        public JPromptFactory build() {
            return new JPromptFactory(this);
        }
    }
}
