package cn.welsione.jprompt.engine;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReflectiveTemplateEngine 反射模板引擎测试
 */
class ReflectiveTemplateEngineTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        templateEngine = new ReflectiveTemplateEngine();
    }

    @Test
    void testRenderWithDoubleBracePlaceholder() {
        String template = "Hello, {{name}}!";
        TestData data = new TestData();
        data.setName("World");

        String result = templateEngine.render(template, data);

        assertEquals("Hello, World!", result);
    }

    @Test
    void testRenderWithSingleBracePlaceholder() {
        String template = "Hello, {name}!";
        TestData data = new TestData();
        data.setName("World");

        String result = templateEngine.render(template, data);

        assertEquals("Hello, World!", result);
    }

    @Test
    void testRenderWithMultipleFields() {
        String template = "{{greeting}}, {{name}}!";
        TestData data = new TestData();
        data.setGreeting("Hello");
        data.setName("Alice");

        String result = templateEngine.render(template, data);

        assertEquals("Hello, Alice!", result);
    }

    @Test
    void testRenderWithNullData() {
        String template = "Hello, {{name}}!";

        String result = templateEngine.render(template, null);

        assertEquals("Hello, {{name}}!", result);
    }

    @Test
    void testRenderWithNullTemplate() {
        TestData data = new TestData();
        data.setName("World");

        String result = templateEngine.render(null, data);

        assertNull(result);
    }

    @Test
    void testRenderWithEmptyTemplate() {
        TestData data = new TestData();
        data.setName("World");

        String result = templateEngine.render("", data);

        assertEquals("", result);
    }

    @Test
    void testRenderWithMissingField() {
        String template = "Hello, {{name}}! Your hobby is {{hobby}}.";
        TestData data = new TestData();
        data.setName("Alice");
        // hobby not set

        String result = templateEngine.render(template, data);

        assertEquals("Hello, Alice! Your hobby is .", result);
    }

    @Test
    void testRenderWithBooleanField() {
        String template = "Passed: {{passed}}";
        TestData data = new TestData();
        data.setPassed(true);

        String result = templateEngine.render(template, data);

        assertEquals("Passed: true", result);
    }

    /**
     * 测试用数据类（仅使用字符串字段避免 Jackson 序列化问题）
     */
    @Getter
    @Setter
    public static class TestData {
        private String name;
        private String greeting;
        private String age;
        private boolean passed;
    }
}
