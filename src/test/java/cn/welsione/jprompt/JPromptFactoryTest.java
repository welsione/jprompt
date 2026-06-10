package cn.welsione.jprompt;

import cn.welsione.jprompt.loader.FileSystemTemplateLoader;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPromptFactory 功能测试
 */
class JPromptFactoryTest {

    @TempDir
    Path tempDir;

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
        JPrompt<Void> staticTemplate = JPromptFactory.getInstance().get("prompts/test_static.md");
        assertNotNull(staticTemplate);
        assertNotNull(staticTemplate.get());
    }

    @Test
    void testCreateTemplate() {
        // 模板类型返回原始模板
        JPrompt<TestData> template = JPromptFactory.getInstance().template("prompts/test_template.md", TestData.class);
        assertNotNull(template);
        assertNotNull(template.get());
    }

    @Test
    void testTemplateBuild() {
        // 模板类型可以 build
        JPrompt<TestData> template = JPromptFactory.getInstance().template("prompts/test_template.md", TestData.class);

        TestData data = new TestData();
        data.setName("testName");
        data.setValue("testValue");

        String result = template.build(data);
        assertNotNull(result);
    }

    @Test
    void testStaticTemplateGet() {
        // 静态模板 get() 返回内容
        JPrompt<Void> staticTemplate = JPromptFactory.getInstance().get("prompts/test_static.md");
        String content = staticTemplate.get();
        assertNotNull(content);
    }

    @Test
    void testStaticTemplateBuildThrows() {
        // 静态模板 build() 应该直接返回内容（不支持 build）
        JPrompt<Void> staticTemplate = JPromptFactory.getInstance().get("prompts/test_static.md");
        String result = staticTemplate.build(null);
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testTemplateBuildRejectsWrongRawType() {
        JPrompt rawTemplate = JPromptFactory.getInstance().template("prompts/test_template.md", TestData.class);

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

    @Test
    void testFileSystemLoader() throws IOException {
        Path promptsDir = Files.createDirectories(tempDir.resolve("prompts"));
        Files.writeString(promptsDir.resolve("hello.md"), "Hello, {{name}}");

        JPromptFactory factory = JPromptFactory.builder()
                .loader(new FileSystemTemplateLoader(tempDir))
                .build();
        TestData data = new TestData();
        data.setName("Alice");

        assertEquals("Hello, Alice", factory.template("prompts/hello.md", TestData.class).build(data));
    }

    @Test
    void testFileSystemLoaderRejectsPathTraversal() {
        FileSystemTemplateLoader loader = new FileSystemTemplateLoader(tempDir);

        assertThrows(TemplateException.class, () -> loader.load("../outside.md"));
    }

    @Test
    void testMissingVariablePolicyFromFactory() {
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "Hello, {{name}}!")
                .missingVariablePolicy(MissingVariablePolicy.EMPTY)
                .build();

        assertEquals("Hello, !", factory.template("missing", TestData.class).build(new TestData()));
    }

    @Test
    void testMissingVariablePolicyKeepRaw() {
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "Hello, {{name}}!")
                .missingVariablePolicy(MissingVariablePolicy.KEEP_RAW)
                .build();

        assertEquals("Hello, name!", factory.template("raw", TestData.class).build(new TestData()));
    }

    @Test
    void testCacheTtlExpired() throws InterruptedException {
        AtomicInteger loadCount = new AtomicInteger();
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "load-" + loadCount.incrementAndGet())
                .cacheTtlMillis(50)
                .build();

        assertEquals("load-1", factory.get("ttl-template").get());
        // 缓存未过期，应命中
        assertEquals("load-1", factory.get("ttl-template").get());
        assertEquals(1, loadCount.get());

        // 等待缓存过期
        Thread.sleep(60);
        assertEquals("load-2", factory.get("ttl-template").get());
        assertEquals(2, loadCount.get());
    }

    @Test
    void testCacheTtlNotExpired() {
        AtomicInteger loadCount = new AtomicInteger();
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "load-" + loadCount.incrementAndGet())
                .cacheTtlMillis(60000)
                .build();

        assertEquals("load-1", factory.get("ttl-long").get());
        assertEquals("load-1", factory.get("ttl-long").get());
        assertEquals(1, loadCount.get());
    }

    @Test
    void testCacheTtlZeroMeansNoExpiry() {
        AtomicInteger loadCount = new AtomicInteger();
        JPromptFactory factory = JPromptFactory.builder()
                .loader(path -> "load-" + loadCount.incrementAndGet())
                .cacheTtlMillis(0)
                .build();

        assertEquals("load-1", factory.get("no-ttl").get());
        assertEquals("load-1", factory.get("no-ttl").get());
        assertEquals(1, loadCount.get());
    }

    @Test
    void testCacheTtlNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                JPromptFactory.builder().cacheTtlMillis(-1));
    }
}
