package cn.welsione.jprompt;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

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
        JPrompt<Void> staticTemplate = JPromptFactory.INSTANCE.get("prompts/test_static.md");
        assertNotNull(staticTemplate);
        assertNotNull(staticTemplate.get());
    }

    @Test
    void testCreateTemplate() {
        // 模板类型返回原始模板
        JPrompt<TestData> template = JPromptFactory.INSTANCE.template("prompts/test_template.md", TestData.class);
        assertNotNull(template);
        assertNotNull(template.get());
    }

    @Test
    void testTemplateBuild() {
        // 模板类型可以 build
        JPrompt<TestData> template = JPromptFactory.INSTANCE.template("prompts/test_template.md", TestData.class);

        TestData data = new TestData();
        data.setName("testName");
        data.setValue("testValue");

        String result = template.build(data);
        assertNotNull(result);
    }

    @Test
    void testStaticTemplateGet() {
        // 静态模板 get() 返回内容
        JPrompt<Void> staticTemplate = JPromptFactory.INSTANCE.get("prompts/test_static.md");
        String content = staticTemplate.get();
        assertNotNull(content);
    }

    @Test
    void testStaticTemplateBuildThrows() {
        // 静态模板 build() 应该直接返回内容（不支持 build）
        JPrompt<Void> staticTemplate = JPromptFactory.INSTANCE.get("prompts/test_static.md");
        String result = staticTemplate.build(null);
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testTemplateBuildRejectsWrongRawType() {
        JPrompt rawTemplate = JPromptFactory.INSTANCE.template("prompts/test_template.md", TestData.class);

        assertThrows(TemplateException.class, () -> rawTemplate.build(new Object()));
    }

    @Test
    void testCustomLoader() {
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "Hello, {{name}} from " + path)
                .build();

        JPrompt<TestData> template = factory.template("memory-template", TestData.class);
        TestData data = new TestData();
        data.setName("Alice");

        assertEquals("Hello, Alice from memory-template", template.build(data));
    }

    @Test
    void testTemplateCacheEnabledByDefault() {
        AtomicInteger loadCount = new AtomicInteger();
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "load-" + loadCount.incrementAndGet())
                .build();

        assertEquals("load-1", factory.get("cached").get());
        assertEquals("load-1", factory.get("cached").get());
        assertEquals(1, loadCount.get());
    }

    @Test
    void testTemplateCacheCanBeDisabled() {
        AtomicInteger loadCount = new AtomicInteger();
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "load-" + loadCount.incrementAndGet())
                .cacheEnabled(false)
                .build();

        assertEquals("load-1", factory.get("uncached").get());
        assertEquals("load-2", factory.get("uncached").get());
        assertEquals(2, loadCount.get());
    }

    @Test
    void testClearCacheReloadsTemplate() {
        AtomicInteger loadCount = new AtomicInteger();
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "load-" + loadCount.incrementAndGet())
                .build();

        assertEquals("load-1", factory.get("reloadable").get());
        factory.clearCache();
        assertEquals("load-2", factory.get("reloadable").get());
    }
}
