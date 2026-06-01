package cn.welsione.jprompt.loader;

import cn.welsione.jprompt.TemplateException;

/**
 * 模板加载器。
 */
@FunctionalInterface
public interface TemplateLoader {

    /**
     * 加载模板内容。
     *
     * @param path 模板路径
     * @return 模板内容
     * @throws TemplateException 加载失败时抛出
     */
    String load(String path) throws TemplateException;
}
