package cn.welsione.jprompt.engine;

import cn.welsione.jprompt.MissingVariablePolicy;
import cn.welsione.jprompt.TemplateException;
import cn.welsione.jprompt.util.PlaceholderUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于反射的模板引擎实现。
 * 支持：变量插值、条件判断、循环迭代、函数调用、表达式运算、默认值。
 */
@Slf4j
public class ReflectiveTemplateEngine implements TemplateEngine {

    private final ObjectMapper objectMapper;
    private final Map<String, TemplateFunction> functions = new ConcurrentHashMap<>();
    private final MissingVariablePolicy missingVariablePolicy;

    public ReflectiveTemplateEngine() {
        this(MissingVariablePolicy.KEEP_PLACEHOLDER);
    }

    public ReflectiveTemplateEngine(MissingVariablePolicy missingVariablePolicy) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.missingVariablePolicy = missingVariablePolicy == null
                ? MissingVariablePolicy.KEEP_PLACEHOLDER
                : missingVariablePolicy;
        BuiltinFunctions.registerAll(this);
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
            Map<String, Object> placeholders = buildJsonPlaceholderMap(data);
            return PlaceholderUtils.render(template, placeholders, functions, missingVariablePolicy);
        } catch (Exception e) {
            log.error("模板渲染失败: {}", e.getMessage(), e);
            throw new TemplateException("模板渲染失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerFunction(String name, TemplateFunction function) {
        functions.put(name, function);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildJsonPlaceholderMap(Object data) throws Exception {
        return objectMapper.convertValue(data, Map.class);
    }
}
