package cn.welsione.jprompt.loader;

import cn.welsione.jprompt.TemplateException;

import java.util.Objects;

/**
 * 组合模板加载器，按优先级尝试 primary → fallback 两个加载器。
 *
 * <p>primary 加载成功则直接返回；primary 失败时尝试 fallback；
 * 两者均失败时抛出 {@link TemplateException}，消息包含路径和两个失败原因。</p>
 *
 * <p>典型用途：开发环境 classpath 优先、生产环境数据库优先的双源提示词加载。</p>
 */
public class CompositeTemplateLoader implements TemplateLoader {

    private final TemplateLoader primary;
    private final TemplateLoader fallback;

    /**
     * 构造组合加载器。
     *
     * @param primary   优先尝试的加载器
     * @param fallback  primary 失败时的兜底加载器
     */
    public CompositeTemplateLoader(TemplateLoader primary, TemplateLoader fallback) {
        this.primary = Objects.requireNonNull(primary, "primary must not be null");
        this.fallback = Objects.requireNonNull(fallback, "fallback must not be null");
    }

    @Override
    public String load(String path) throws TemplateException {
        Objects.requireNonNull(path, "path must not be null");
        TemplateException primaryError;
        try {
            return primary.load(path);
        } catch (TemplateException ex) {
            primaryError = ex;
        }
        try {
            return fallback.load(path);
        } catch (TemplateException fallbackError) {
            throw new TemplateException(
                    "Composite loader failed for path '" + path
                            + "': primary=[" + primaryError.getMessage()
                            + "], fallback=[" + fallbackError.getMessage() + "]");
        }
    }
}
