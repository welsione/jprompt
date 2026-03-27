package cn.welsione.jprompt;

import cn.welsione.jprompt.engine.ReflectiveTemplateEngine;
import cn.welsione.jprompt.engine.TemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPrompt 工厂类
 */
public enum JPromptFactory {

    INSTANCE;

    private final Map<String, String> templateCache = new ConcurrentHashMap<>();
    private final TemplateEngine engine = new ReflectiveTemplateEngine();

    /**
     * 创建静态提示词（不可变）
     *
     * @param path 提示词文件路径
     * @return JPrompt<Void> 实例
     */
    public JPrompt<Void> get(String path) {
        String content = loadTemplate(path);
        return new JPrompt<>(content, Void.class, false);
    }

    /**
     * 创建模板提示词（可变）
     *
     * @param path       模板路径
     * @param dataClass 数据类型
     * @param <T>       数据类型
     * @return JPrompt<T> 实例
     */
    public <T> JPrompt<T> template(String path, Class<T> dataClass) {
        String content = loadTemplate(path);
        return new JPrompt<>(content, dataClass, true);
    }

    /**
     * 获取模板引擎
     */
    public TemplateEngine getEngine() {
        return engine;
    }

    /**
     * 加载模板文件
     */
    private String loadTemplate(String path) {
        return templateCache.computeIfAbsent(path, p -> {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(p)) {
                if (is == null) {
                    throw new TemplateException("找不到提示词模板文件: " + p);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new TemplateException("加载提示词模板失败: " + p, e);
            }
        });
    }
}
