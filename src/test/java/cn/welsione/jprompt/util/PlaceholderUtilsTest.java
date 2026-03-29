package cn.welsione.jprompt.util;

import cn.welsione.jprompt.engine.TemplateEngine;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlaceholderUtils 占位符工具测试
 */
class PlaceholderUtilsTest {

    @Test
    void testDoubleBracePlaceholder() {
        String template = "Hello, {{name}}!";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", "World");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Hello, World!", result);
    }

    @Test
    void testSingleBracePlaceholder() {
        String template = "Hello, {name}!";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", "World");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Hello, World!", result);
    }

    @Test
    void testMultiplePlaceholders() {
        String template = "{{greeting}}, {{name}}! Today is {{day}}.";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("greeting", "Hello");
        placeholders.put("name", "World");
        placeholders.put("day", "Monday");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Hello, World! Today is Monday.", result);
    }

    @Test
    void testNestedPlaceholder() {
        String template = "User: {{user.name}}, Age: {{user.age}}";
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Alice");
        user.put("age", "25");

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("user", user);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("User: Alice, Age: 25", result);
    }

    @Test
    void testMissingPlaceholderKeepsOriginal() {
        // 当占位符在模板中但映射中没有对应值时，保留原占位符
        String template = "Hello, {{name}}!";
        Map<String, Object> placeholders = new HashMap<>();
        // 不添加 name 键

        String result = PlaceholderUtils.render(template, placeholders);

        // 映射为空时直接返回原模板
        assertEquals(template, result);
    }

    @Test
    void testNullTemplate() {
        String result = PlaceholderUtils.render(null, new HashMap<>());
        assertNull(result);
    }

    @Test
    void testEmptyTemplate() {
        String result = PlaceholderUtils.render("", new HashMap<>());
        assertEquals("", result);
    }

    @Test
    void testNullPlaceholders() {
        String template = "Hello, {{name}}!";
        String result = PlaceholderUtils.render(template, null);
        assertEquals(template, result);
    }

    @Test
    void testEmptyPlaceholdersMap() {
        // 空映射时直接返回原模板
        String template = "Hello, {{name}}!";
        Map<String, Object> placeholders = new HashMap<>();

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals(template, result);
    }

    @Test
    void testContainsPlaceholder() {
        assertTrue(PlaceholderUtils.containsPlaceholder("Hello, {{name}}!", "name"));
        assertFalse(PlaceholderUtils.containsPlaceholder("Hello, {name}!", "name"));
        assertFalse(PlaceholderUtils.containsPlaceholder("Hello, {{name}}!", "other"));
    }

    @Test
    void testMixedPlaceholderFormats() {
        String template = "First: {{name}}, Second: {age}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", "Alice");
        placeholders.put("age", "30");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("First: Alice, Second: 30", result);
    }

    @Test
    void testSpecialCharactersInValue() {
        String template = "Path: {{path}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("path", "C:\\Users\\Test");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Path: C:\\Users\\Test", result);
    }

    @Test
    void testDollarSignInValue() {
        String template = "Price: {{price}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("price", "$100");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Price: $100", result);
    }

    @Test
    void testBooleanValue() {
        String template = "Active: {{active}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("active", true);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Active: true", result);
    }

    // ========== 增强功能测试 ==========

    @Test
    void testDefaultValue() {
        String template = "Title: {{title!\"无标题\"}}";
        Map<String, Object> placeholders = new HashMap<>();
        // title 未设置

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Title: 无标题", result);
    }

    @Test
    void testDefaultValueWhenValueExists() {
        String template = "Title: {{title!\"无标题\"}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("title", "我的小说");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("Title: 我的小说", result);
    }

    @Test
    void testIfBlockConditionTrue() {
        String template = "{{#if hasPower}}战力体系{{/if}}已解锁";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("hasPower", true);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("战力体系已解锁", result);
    }

    @Test
    void testIfBlockConditionFalse() {
        String template = "{{#if hasPower}}战力体系{{/if}}已解锁";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("hasPower", false);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("已解锁", result);
    }

    @Test
    void testUnlessBlockConditionFalse() {
        String template = "{{#unless isEmpty}}有内容{{/unless}}查看";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("isEmpty", false);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("有内容查看", result);
    }

    @Test
    void testUnlessBlockConditionTrue() {
        String template = "{{#unless isEmpty}}有内容{{/unless}}查看";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("isEmpty", true);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("查看", result);
    }

    @Test
    void testEachBlock() {
        String template = "角色：{{#each chars}}{{name}}{{/each}}";
        Map<String, Object> char1 = new HashMap<>();
        char1.put("name", "张三");
        Map<String, Object> char2 = new HashMap<>();
        char2.put("name", "李四");

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("chars", Arrays.asList(char1, char2));

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("角色：张三李四", result);
    }

    @Test
    void testEachBlockWithAlias() {
        String template = "{{#each chars as c}}{{c.name}}{{/each}}";
        Map<String, Object> char1 = new HashMap<>();
        char1.put("name", "张三");
        Map<String, Object> char2 = new HashMap<>();
        char2.put("name", "李四");

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("chars", Arrays.asList(char1, char2));

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("张三李四", result);
    }

    @Test
    void testEqBlockEqual() {
        String template = "{{#eq status \"active\"}}在线{{/eq}}状态";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("status", "active");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("在线状态", result);
    }

    @Test
    void testEqBlockNotEqual() {
        String template = "{{#eq status \"active\"}}在线{{/eq}}状态";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("status", "offline");

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("状态", result);
    }

    @Test
    void testExpressionAddition() {
        String template = "序号：{{index + 1}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("index", 5);

        String result = PlaceholderUtils.render(template, placeholders);

        assertEquals("序号：6.0", result);
    }

    @Test
    void testFunctionUpperCase() {
        Map<String, TemplateEngine.TemplateFunction> functions = new HashMap<>();
        functions.put("upperCase", args -> args.length > 0 && args[0] != null
                ? args[0].toString().toUpperCase() : "");

        String template = "Name: {{upperCase name}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", "alice");

        String result = PlaceholderUtils.render(template, placeholders, functions);

        assertEquals("Name: ALICE", result);
    }

    @Test
    void testFunctionLowerCase() {
        Map<String, TemplateEngine.TemplateFunction> functions = new HashMap<>();
        functions.put("lowerCase", args -> args.length > 0 && args[0] != null
                ? args[0].toString().toLowerCase() : "");

        String template = "Name: {{lowerCase name}}";
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", "BOB");

        String result = PlaceholderUtils.render(template, placeholders, functions);

        assertEquals("Name: bob", result);
    }

    @Test
    void testFunctionWithStringArgument() {
        Map<String, TemplateEngine.TemplateFunction> functions = new HashMap<>();
        functions.put("greet", args -> args.length > 0 ? "Hello, " + args[0] : "");

        String template = "{{greet \"World\"}}";
        Map<String, Object> placeholders = new HashMap<>();

        String result = PlaceholderUtils.render(template, placeholders, functions);

        assertEquals("Hello, World", result);
    }
}
