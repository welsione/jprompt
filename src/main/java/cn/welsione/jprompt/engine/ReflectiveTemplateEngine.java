package cn.welsione.jprompt.engine;

import cn.welsione.jprompt.TemplateException;
import cn.welsione.jprompt.util.PlaceholderUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 基于反射的模板引擎实现 - 增强版
 * 支持：变量插值、条件判断、循环迭代、函数调用、表达式运算、默认值
 */
@Slf4j
public class ReflectiveTemplateEngine implements TemplateEngine {

    private final ObjectMapper objectMapper;
    private final Map<String, TemplateFunction> functions = new HashMap<>();

    public ReflectiveTemplateEngine() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        registerBuiltInFunctions();
    }

    /**
     * 注册内置函数
     */
    private void registerBuiltInFunctions() {
        // 字符串函数
        registerFunction("upperCase", args -> args.length > 0 && args[0] != null
                ? args[0].toString().toUpperCase() : "");
        registerFunction("lowerCase", args -> args.length > 0 && args[0] != null
                ? args[0].toString().toLowerCase() : "");
        registerFunction("capitalize", args -> args.length > 0 && args[0] != null
                ? capitalize(args[0].toString()) : "");
        registerFunction("trim", args -> args.length > 0 && args[0] != null
                ? args[0].toString().trim() : "");

        // 集合函数
        registerFunction("length", args -> {
            if (args.length > 0 && args[0] != null) {
                Object arg = args[0];
                if (arg instanceof Collection) {
                    return String.valueOf(((Collection<?>) arg).size());
                } else if (arg instanceof Map) {
                    return String.valueOf(((Map<?, ?>) arg).size());
                } else if (arg instanceof String) {
                    return String.valueOf(((String) arg).length());
                } else if (arg.getClass().isArray()) {
                    return String.valueOf(java.lang.reflect.Array.getLength(arg));
                }
            }
            return "0";
        });

        registerFunction("join", args -> {
            if (args.length > 0 && args[0] instanceof Collection) {
                String separator = args.length > 1 ? args[1].toString() : ",";
                return String.join(separator, args[0].toString());
            }
            return args.length > 0 ? args[0].toString() : "";
        });

        // 日期函数
        registerFunction("formatDate", args -> {
            if (args.length > 0 && args[0] != null) {
                String pattern = args.length > 1 ? args[1].toString() : "yyyy-MM-dd";
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    if (args[0] instanceof LocalDateTime) {
                        return ((LocalDateTime) args[0]).format(formatter);
                    } else if (args[0] instanceof LocalDate) {
                        return ((LocalDate) args[0]).format(formatter);
                    } else if (args[0] instanceof java.util.Date) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
                        return sdf.format((java.util.Date) args[0]);
                    }
                } catch (Exception e) {
                    return args[0].toString();
                }
            }
            return "";
        });

        // 逻辑函数
        registerFunction("ternary", args -> args.length >= 3
                ? (isTruthy(args[0]) ? args[1] : args[2]).toString()
                : "");
        registerFunction("default", args -> args.length > 0
                ? (isTruthy(args[0]) ? args[0] : args.length > 1 ? args[1] : "").toString()
                : "");

        // 数学函数
        registerFunction("max", args -> {
            if (args.length >= 2) {
                double[] nums = new double[args.length];
                for (int i = 0; i < args.length; i++) {
                    nums[i] = toDouble(args[i]);
                }
                return String.valueOf((int) Math.max(nums[0], nums[1]));
            }
            return "0";
        });
        registerFunction("min", args -> {
            if (args.length >= 2) {
                double[] nums = new double[args.length];
                for (int i = 0; i < args.length; i++) {
                    nums[i] = toDouble(args[i]);
                }
                return String.valueOf((int) Math.min(nums[0], nums[1]));
            }
            return "0";
        });
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return !((String) value).isEmpty();
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof Collection) return !((Collection<?>) value).isEmpty();
        return true;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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

            // 渲染模板（带函数）
            return PlaceholderUtils.render(template, placeholders, functions);
        } catch (Exception e) {
            log.error("模板渲染失败: {}", e.getMessage(), e);
            throw new TemplateException("模板渲染失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerFunction(String name, TemplateFunction function) {
        functions.put(name, function);
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
