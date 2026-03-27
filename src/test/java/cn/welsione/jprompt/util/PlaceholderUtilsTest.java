package cn.welsione.jprompt.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

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
}
