package cn.welsione.jprompt;

import cn.welsione.jprompt.engine.ReflectiveTemplateEngine;
import cn.welsione.jprompt.engine.TemplateEngine;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPrompt 模板使用集成测试
 */
class PromptTemplateIntegrationTest {

    /**
     * 测试用数据类
     */
    @Getter
    @Setter
    public static class TestTaskData {
        private String taskName;
        private String description;
        private String priority;
    }

    @Test
    void testDeclarativeTemplateUsage() {
        TemplateEngine engine = new ReflectiveTemplateEngine();

        String templateContent = "Task: {taskName}\nDescription: {description}\nPriority: {priority}";

        TestTaskData data = new TestTaskData();
        data.setTaskName("Write Report");
        data.setDescription("Complete the quarterly report");
        data.setPriority("High");

        String result = engine.render(templateContent, data);

        assertTrue(result.contains("Task: Write Report"));
        assertTrue(result.contains("Description: Complete the quarterly report"));
        assertTrue(result.contains("Priority: High"));
    }

    @Test
    void testTemplateBuildMethod() {
        TemplateEngine engine = new ReflectiveTemplateEngine();

        String template = "Task: {{taskName}}, Priority: {{priority}}";

        TestTaskData data = new TestTaskData();
        data.setTaskName("Review PR");
        data.setPriority("Medium");

        String result = engine.render(template, data);

        assertEquals("Task: Review PR, Priority: Medium", result);
    }

    @Test
    void testSpecialCharactersInData() {
        TemplateEngine engine = new ReflectiveTemplateEngine();

        String template = "Task: {taskName}, Desc: {description}";
        TestTaskData data = new TestTaskData();
        data.setTaskName("Special");
        data.setDescription("Content with dollar $100");

        String result = engine.render(template, data);

        assertTrue(result.contains("Task: Special"));
        assertTrue(result.contains("Desc: Content with dollar $100"));
    }

    @Test
    void testJPromptFactoryCreateStatic() {
        JPrompt<Void> template = JPromptFactory.INSTANCE.get("prompts/test_static.md");
        assertNotNull(template.get());
        assertNotNull(template);
    }

    @Test
    void testJPromptFactoryCreateTemplate() {
        JPrompt<TestTaskData> template = JPromptFactory.INSTANCE.template("prompts/test_template.md", TestTaskData.class);
        assertNotNull(template);

        TestTaskData data = new TestTaskData();
        data.setTaskName("TestTask");
        data.setDescription("TestDesc");
        data.setPriority("High");

        String result = template.build(data);
        assertNotNull(result);
        // 模板使用 {{taskName}} 占位符
        assertTrue(result.contains("TestTask"));
    }
}
