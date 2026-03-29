package cn.welsione.jprompt.engine;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        // TestData 没有 hobby 字段，所以 {{hobby}} 应该被保留
        String template = "Hello, {{name}}! Your hobby is {{hobby}}.";
        TestData data = new TestData();
        data.setName("Alice");
        // hobby not set

        String result = templateEngine.render(template, data);

        // 缺失的占位符应保留原样
        assertEquals("Hello, Alice! Your hobby is {{hobby}}.", result);
    }

    @Test
    void testRenderWithBooleanField() {
        String template = "Passed: {{passed}}";
        TestData data = new TestData();
        data.setPassed(true);

        String result = templateEngine.render(template, data);

        assertEquals("Passed: true", result);
    }

    // ========== 增强功能测试 ==========

    @Test
    void testDefaultValue() {
        String template = "Title: {{title!\"无标题\"}}";
        TestData data = new TestData();
        // title not set

        String result = templateEngine.render(template, data);

        assertEquals("Title: 无标题", result);
    }

    @Test
    void testDefaultValueWhenValueExists() {
        // 使用 name 字段，因为 TestData 没有 title 字段
        String template = "Title: {{name!\"无标题\"}}";
        TestData data = new TestData();
        data.setName("我的小说");

        String result = templateEngine.render(template, data);

        assertEquals("Title: 我的小说", result);
    }

    @Test
    void testIfBlockConditionTrue() {
        String template = "{{#if hasPower}}战力体系{{/if}}已解锁";
        Map<String, Object> data = new HashMap<>();
        data.put("hasPower", true);

        String result = templateEngine.render(template, data);

        assertEquals("战力体系已解锁", result);
    }

    @Test
    void testIfBlockConditionFalse() {
        String template = "{{#if hasPower}}战力体系{{/if}}已解锁";
        Map<String, Object> data = new HashMap<>();
        data.put("hasPower", false);

        String result = templateEngine.render(template, data);

        assertEquals("已解锁", result);
    }

    @Test
    void testUnlessBlockConditionFalse() {
        String template = "{{#unless isEmpty}}有内容{{/unless}}查看";
        Map<String, Object> data = new HashMap<>();
        data.put("isEmpty", false);

        String result = templateEngine.render(template, data);

        assertEquals("有内容查看", result);
    }

    @Test
    void testEachBlock() {
        String template = "角色：{{#each chars}}{{name}}{{/each}}";
        Map<String, Object> char1 = new HashMap<>();
        char1.put("name", "张三");
        Map<String, Object> char2 = new HashMap<>();
        char2.put("name", "李四");

        Map<String, Object> data = new HashMap<>();
        data.put("chars", Arrays.asList(char1, char2));

        String result = templateEngine.render(template, data);

        assertEquals("角色：张三李四", result);
    }

    @Test
    void testEachBlockWithAlias() {
        String template = "{{#each chars as c}}{{c.name}}{{/each}}";
        Map<String, Object> char1 = new HashMap<>();
        char1.put("name", "张三");
        Map<String, Object> char2 = new HashMap<>();
        char2.put("name", "李四");

        Map<String, Object> data = new HashMap<>();
        data.put("chars", Arrays.asList(char1, char2));

        String result = templateEngine.render(template, data);

        assertEquals("张三李四", result);
    }

    @Test
    void testEqBlockEqual() {
        String template = "{{#eq status \"active\"}}在线{{/eq}}状态";
        Map<String, Object> data = new HashMap<>();
        data.put("status", "active");

        String result = templateEngine.render(template, data);

        assertEquals("在线状态", result);
    }

    @Test
    void testEqBlockNotEqual() {
        String template = "{{#eq status \"active\"}}在线{{/eq}}状态";
        Map<String, Object> data = new HashMap<>();
        data.put("status", "offline");

        String result = templateEngine.render(template, data);

        assertEquals("状态", result);
    }

    @Test
    void testExpressionAddition() {
        String template = "序号：{{index + 1}}";
        Map<String, Object> data = new HashMap<>();
        data.put("index", 5);

        String result = templateEngine.render(template, data);

        assertEquals("序号：6.0", result);
    }

    @Test
    void testBuiltInFunctionUpperCase() {
        String template = "Name: {{upperCase name}}";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "alice");

        String result = templateEngine.render(template, data);

        assertEquals("Name: ALICE", result);
    }

    @Test
    void testBuiltInFunctionLowerCase() {
        String template = "Name: {{lowerCase name}}";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "BOB");

        String result = templateEngine.render(template, data);

        assertEquals("Name: bob", result);
    }

    @Test
    void testBuiltInFunctionCapitalize() {
        String template = "{{capitalize name}}";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "alice");

        String result = templateEngine.render(template, data);

        assertEquals("Alice", result);
    }

    @Test
    void testBuiltInFunctionLength() {
        String template = "Length: {{length name}}";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "hello");

        String result = templateEngine.render(template, data);

        assertEquals("Length: 5", result);
    }

    @Test
    void testBuiltInFunctionLengthWithList() {
        String template = "Count: {{length items}}";
        Map<String, Object> data = new HashMap<>();
        data.put("items", Arrays.asList("a", "b", "c"));

        String result = templateEngine.render(template, data);

        assertEquals("Count: 3", result);
    }

    @Test
    void testCustomFunction() {
        TemplateEngine engine = new ReflectiveTemplateEngine();
        engine.registerFunction("greet", args -> args.length > 0 ? "Hello, " + args[0] : "");

        String template = "{{greet name}}";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "World");

        String result = engine.render(template, data);

        assertEquals("Hello, World", result);
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
