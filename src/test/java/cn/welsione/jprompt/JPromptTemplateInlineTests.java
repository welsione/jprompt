package cn.welsione.jprompt;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPrompt.templateInline 从字符串创建模板的测试，验证与 template(path) 渲染行为一致。
 */
class JPromptTemplateInlineTests {

    /**
     * 测试用数据类
     */
    @Getter
    @Setter
    public static class TestData {
        private String name;
        private List<String> keywords;
        private boolean active;
    }

    @Test
    void rendersVariablePlaceholder() {
        JPrompt<TestData> prompt = JPrompt.templateInline("Hello, {{name}}!", TestData.class);
        TestData data = new TestData();
        data.setName("Ascoder");

        assertEquals("Hello, Ascoder!", prompt.build(data));
    }

    @Test
    void rendersEachList() {
        JPrompt<TestData> prompt = JPrompt.templateInline(
                "{{#each keywords as k}}- {{k}}\n{{/each}}", TestData.class);
        TestData data = new TestData();
        data.setKeywords(List.of("a", "b"));

        assertEquals("- a\n- b\n", prompt.build(data));
    }

    @Test
    void rendersIfCondition() {
        JPrompt<TestData> prompt = JPrompt.templateInline(
                "{{#if active}}在线{{/if}}", TestData.class);
        TestData data = new TestData();
        data.setActive(true);

        assertEquals("在线", prompt.build(data));
    }

    @Test
    void emptyContentRendersEmpty() {
        JPrompt<TestData> prompt = JPrompt.templateInline("", TestData.class);
        assertEquals("", prompt.build(new TestData()));
    }

    @Test
    void inlineRenderEqualsFileTemplateRender() {
        // 同一段模板文本，templateInline 与从文件加载的 template 渲染结果一致
        String templateText = "Task: {{name}}\nKeywords:\n{{#each keywords as k}}- {{k}}\n{{/each}}";
        JPrompt<TestData> inlinePrompt = JPrompt.templateInline(templateText, TestData.class);
        JPrompt<TestData> filePrompt = JPromptFactory.getInstance().template(
                "prompts/test_inline_compare.md", TestData.class);

        TestData data = new TestData();
        data.setName("demo");
        data.setKeywords(List.of("x", "y"));

        assertEquals(filePrompt.build(data), inlinePrompt.build(data));
    }

    @Test
    void nullContentThrows() {
        assertThrows(NullPointerException.class,
                () -> JPrompt.templateInline(null, TestData.class));
    }

    @Test
    void nullDataClassThrows() {
        assertThrows(NullPointerException.class,
                () -> JPrompt.templateInline("{{name}}", null));
    }
}
