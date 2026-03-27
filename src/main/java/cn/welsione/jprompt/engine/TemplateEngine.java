package cn.welsione.jprompt.engine;

import cn.welsione.jprompt.TemplateException;

/**
 * 模板引擎接口
 */
public interface TemplateEngine {

    /**
     * 渲染模板
     *
     * @param template 模板内容
     * @param data     数据对象
     * @return 渲染后的字符串
     * @throws TemplateException 如果渲染失败
     */
    String render(String template, Object data) throws TemplateException;
}
