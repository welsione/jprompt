package cn.welsione.jprompt;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPromptFactory 功能测试
 */
class JPromptFactoryTest {

    /**
     * 测试用数据类
     */
    @Getter
    @Setter
    public static class TestData {
        private String name;
        private String value;
    }

    @Test
    void testCreateStatic() {
        // 静态类型直接返回内容
        JPromptTemplate staticTemplate = JPromptFactory.INSTANCE.createStatic("prompts/test_static.md");
        assertNotNull(staticTemplate);
        assertNotNull(staticTemplate.get());
    }

    @Test
    void testCreateTemplate() {
        // 模板类型返回原始模板
        JPromptTemplate template = JPromptFactory.INSTANCE.createTemplate("prompts/test_template.md", TestData.class);
        assertNotNull(template);
        assertNotNull(template.get());
    }

    @Test
    void testTemplateBuild() {
        // 模板类型可以 build
        JPromptTemplate template = JPromptFactory.INSTANCE.createTemplate("prompts/test_template.md", TestData.class);

        TestData data = new TestData();
        data.setName("testName");
        data.setValue("testValue");

        String result = template.build(data);
        assertNotNull(result);
    }

    @Test
    void testStaticTemplateGet() {
        // 静态模板 get() 返回内容
        JPromptTemplate staticTemplate = JPromptFactory.INSTANCE.createStatic("prompts/test_static.md");
        String content = staticTemplate.get();
        assertNotNull(content);
    }

    @Test
    void testStaticTemplateBuildThrows() {
        // 静态模板 build() 应该抛出异常
        JPromptTemplate staticTemplate = JPromptFactory.INSTANCE.createStatic("prompts/test_static.md");
        assertThrows(UnsupportedOperationException.class, () -> {
            staticTemplate.build(new Object());
        });
    }
}
