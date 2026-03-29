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

    /**
     * 注册自定义函数
     *
     * @param name     函数名称
     * @param function 函数实现
     */
    default void registerFunction(String name, TemplateFunction function) {
    }

    /**
     * 模板函数接口
     */
    @FunctionalInterface
    interface TemplateFunction {
        /**
         * 应用函数
         *
         * @param args 函数参数
         * @return 函数结果
         */
        String apply(Object... args);
    }
}
