package cn.welsione.jprompt.engine;

import cn.welsione.jprompt.TemplateException;
import cn.welsione.jprompt.util.PlaceholderUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于反射的模板引擎实现
 */
@Slf4j
public class ReflectiveTemplateEngine implements TemplateEngine {

    private final ObjectMapper objectMapper;

    public ReflectiveTemplateEngine() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @Override
    public String render(String template, Object data) throws TemplateException {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (data == null) {
            return template;
        }

        try {
            // 使用 JSON 序列化方式构建占位符映射
            Map<String, Object> placeholders = buildJsonPlaceholderMap(data);

            // 渲染模板
            return PlaceholderUtils.render(template, placeholders);
        } catch (Exception e) {
            log.error("模板渲染失败: {}", e.getMessage(), e);
            throw new TemplateException("模板渲染失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将对象序列化为 Map，用于 JSON 风格的占位符替换
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildJsonPlaceholderMap(Object data) throws Exception {
        Map<String, Object> map = objectMapper.convertValue(data, Map.class);
        Map<String, Object> result = new HashMap<>();
        flattenMap("", map, result);
        return result;
    }

    /**
     * 将嵌套的 Map 展平为扁平的占位符映射
     */
    private void flattenMap(String prefix, Map<String, Object> map, Map<String, Object> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flattenMap(key, (Map<String, Object>) value, result);
            } else {
                result.put(key, value);
            }
        }
    }
}
