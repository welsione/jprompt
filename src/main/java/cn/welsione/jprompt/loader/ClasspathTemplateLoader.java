package cn.welsione.jprompt.loader;

import cn.welsione.jprompt.TemplateException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 从 classpath 加载模板。
 */
public class ClasspathTemplateLoader implements TemplateLoader {

    private final ClassLoader classLoader;

    public ClasspathTemplateLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClasspathTemplateLoader(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : ClasspathTemplateLoader.class.getClassLoader();
    }

    @Override
    public String load(String path) throws TemplateException {
        try (InputStream is = classLoader.getResourceAsStream(path)) {
            if (is == null) {
                throw new TemplateException("找不到提示词模板文件: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TemplateException("加载提示词模板失败: " + path, e);
        }
    }
}
