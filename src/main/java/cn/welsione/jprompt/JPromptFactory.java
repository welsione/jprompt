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
 *
 * <p>默认实例通过 {@link #getInstance()} 懒加载获取，使用 classpath 加载器、缓存开启、保留缺失占位符策略。
 * 可通过 {@link #configure()} 在首次使用前替换全局默认配置。
 * 如需完全独立的配置，请使用 {@link #builder()} 创建新实例。
 */
public final class JPromptFactory {

    private static volatile JPromptFactory instance;
    private static final Object INSTANCE_LOCK = new Object();

    private final Map<String, CacheEntry> templateCache = new ConcurrentHashMap<>();
    private final TemplateEngine engine;
    private final TemplateLoader loader;
    private final boolean cacheEnabled;
    private final long cacheTtlMillis;

    private JPromptFactory(Builder builder) {
        this.engine = builder.engine != null
                ? builder.engine
                : new ReflectiveTemplateEngine(builder.missingVariablePolicy);
        this.loader = builder.loader;
        this.cacheEnabled = builder.cacheEnabled;
        this.cacheTtlMillis = builder.cacheTtlMillis;
    }

    /**
     * 获取全局默认实例（懒加载）。
     */
    public static JPromptFactory getInstance() {
        if (instance == null) {
            synchronized (INSTANCE_LOCK) {
                if (instance == null) {
                    instance = builder().build();
                }
            }
        }
        return instance;
    }

    /**
     * 创建全局配置构建器，用于替换默认实例。
     *
     * <p>必须在首次调用 {@link #getINSTANCE()} 之前调用，否则抛出异常。
     *
     * @return Builder 实例
     * @throws IllegalStateException 如果全局实例已初始化
     */
    public static Builder configure() {
        if (instance != null) {
            throw new IllegalStateException("全局实例已初始化，无法重新配置。请使用 JPromptFactory.builder() 创建独立实例。");
        }
        return new Builder(true);
    }

    /**
     * 创建工厂构建器。
     */
    public static Builder builder() {
        return new Builder(false);
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
        if (cacheTtlMillis > 0) {
            CacheEntry entry = templateCache.computeIfAbsent(path, p -> new CacheEntry(loader.load(p)));
            if (entry.isExpired(cacheTtlMillis)) {
                String content = loader.load(path);
                templateCache.put(path, new CacheEntry(content));
                return content;
            }
            return entry.content;
        }
        CacheEntry entry = templateCache.computeIfAbsent(path, p -> new CacheEntry(loader.load(p)));
        return entry.content;
    }

    private static final class CacheEntry {
        final String content;
        final long timestamp;

        CacheEntry(String content) {
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - timestamp > ttlMillis;
        }
    }

    public static final class Builder {

        private TemplateEngine engine;
        private TemplateLoader loader = new ClasspathTemplateLoader();
        private boolean cacheEnabled = true;
        private long cacheTtlMillis;
        private MissingVariablePolicy missingVariablePolicy = MissingVariablePolicy.KEEP_PLACEHOLDER;
        private final boolean global;

        private Builder(boolean global) {
            this.global = global;
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

        /**
         * 设置缓存 TTL（毫秒）。超过此时间的缓存条目将重新加载。
         *
         * @param ttlMillis 缓存存活时间，0 表示永不过期（默认）
         * @return this
         */
        public Builder cacheTtlMillis(long ttlMillis) {
            if (ttlMillis < 0) {
                throw new IllegalArgumentException("cacheTtlMillis must not be negative");
            }
            this.cacheTtlMillis = ttlMillis;
            return this;
        }

        public Builder missingVariablePolicy(MissingVariablePolicy missingVariablePolicy) {
            this.missingVariablePolicy = Objects.requireNonNull(
                    missingVariablePolicy, "missingVariablePolicy must not be null");
            return this;
        }

        public JPromptFactory build() {
            JPromptFactory factory = new JPromptFactory(this);
            if (global) {
                synchronized (INSTANCE_LOCK) {
                    if (instance != null) {
                        throw new IllegalStateException("全局实例已初始化，无法重新配置。");
                    }
                    instance = factory;
                }
            }
            return factory;
        }
    }
}
