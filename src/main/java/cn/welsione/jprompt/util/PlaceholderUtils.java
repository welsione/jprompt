package cn.welsione.jprompt.util;

import cn.welsione.jprompt.engine.TemplateEngine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 占位符工具类 - 增强版
 * 支持：变量插值、条件判断、循环迭代、函数调用、表达式运算、默认值
 */
public final class PlaceholderUtils {

    // 双花括号占位符正则表达式
    private static final Pattern DOUBLE_BRACE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    // 单花括号占位符正则表达式
    private static final Pattern SINGLE_BRACE_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    // 块级标签正则：{{#if}}, {{#unless}}, {{#each}}, {{#eq}}
    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "\\{\\{(#if|#unless|#each|#eq|#else|/if|/unless|/each|/eq)\\s*([^}]*)\\}\\}");

    // 函数调用正则：{{funcName arg1 arg2}}
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "^([a-zA-Z_][a-zA-Z0-9_]*)\\s+(.+)$");

    // 默认值语法：name!"default"
    private static final Pattern DEFAULT_VALUE_PATTERN = Pattern.compile(
            "^([^!]+)!\\s*\"([^\"]*)\"$");

    // 算术表达式正则
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "^\\s*([a-zA-Z_][a-zA-Z0-9_]*|\\d+\\.?\\d*)\\s*([+\\-*/])\\s*([a-zA-Z_][a-zA-Z0-9_]*|\\d+\\.?\\d*)\\s*$");

    // each 别名语法：{{#each items as item}}
    private static final Pattern EACH_ALIAS_PATTERN = Pattern.compile(
            "^#each\\s+(\\S+)\\s+as\\s+(\\S+)$");

    private PlaceholderUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 渲染模板
     *
     * @param template    模板内容
     * @param placeholders 占位符映射
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> placeholders) {
        return render(template, placeholders, Collections.emptyMap());
    }

    /**
     * 渲染模板（带函数注册）
     *
     * @param template    模板内容
     * @param placeholders 占位符映射
     * @param functions   函数映射
     * @return 渲染后的字符串
     */
    public static String render(String template, Map<String, Object> placeholders,
                                Map<String, TemplateEngine.TemplateFunction> functions) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (placeholders == null) {
            placeholders = Collections.emptyMap();
        }

        // 第一遍：处理块级标签（if、unless、each、eq）
        String result = processBlocks(template, placeholders, functions);

        // 第二遍：处理简单变量插值和函数调用
        result = processSimplePlaceholders(result, placeholders, functions);

        // 第三遍：处理单花括号格式（兼容现有模板）
        result = processSingleBrace(result, placeholders);

        return result;
    }

    /**
     * 处理块级标签
     */
    private static String processBlocks(String template, Map<String, Object> placeholders,
                                        Map<String, TemplateEngine.TemplateFunction> functions) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = BLOCK_PATTERN.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(template, lastEnd, matcher.start());

            String tag = matcher.group(1);
            String content = matcher.group(2).trim();
            int matchStart = matcher.start();
            int matchEnd = matcher.end();

            switch (tag) {
                case "#if":
                    result.append(processIfBlock(template, content, placeholders, functions, matcher, lastEnd));
                    lastEnd = matchEnd;
                    break;
                case "#unless":
                    result.append(processUnlessBlock(template, content, placeholders, functions, matcher, lastEnd));
                    lastEnd = matchEnd;
                    break;
                case "#each":
                    result.append(processEachBlock(template, content, placeholders, functions, matcher, lastEnd));
                    lastEnd = matchEnd;
                    break;
                case "#eq":
                    result.append(processEqBlock(template, content, placeholders, functions, matcher, lastEnd));
                    lastEnd = matchEnd;
                    break;
                case "#else":
                case "/if":
                case "/unless":
                case "/each":
                case "/eq":
                    // 这些标签在 processXxxBlock 中已经处理过
                    lastEnd = matchEnd;
                    break;
            }
        }

        result.append(template.substring(lastEnd));
        return result.toString();
    }

    /**
     * 处理 if 块
     */
    private static String processIfBlock(String template, String condition, Map<String, Object> placeholders,
                                         Map<String, TemplateEngine.TemplateFunction> functions,
                                         Matcher blockMatcher, int lastEnd) {
        // 找到对应的 {{/if}}
        String innerTemplate = findBlockEnd(template, blockMatcher.end(), "/if");
        if (innerTemplate == null) {
            return template.substring(lastEnd);
        }

        String[] parts = innerTemplate.split("\n", 2);
        String blockContent = parts[0];
        String endTag = parts[1];

        // 评估条件
        Object condValue = evaluateExpression(condition, placeholders, functions);
        boolean condResult = TemplateUtils.isTruthy(condValue);

        if (condResult) {
            return blockContent + endTag;
        } else {
            // 检查是否有 {{#else}}
            int elseIndex = blockContent.indexOf("{{#else}}");
            if (elseIndex != -1) {
                String ifPart = blockContent.substring(0, elseIndex);
                String elsePart = blockContent.substring(elseIndex + 9);
                return elsePart + endTag;
            }
            return endTag;
        }
    }

    /**
     * 处理 unless 块
     */
    private static String processUnlessBlock(String template, String condition, Map<String, Object> placeholders,
                                              Map<String, TemplateEngine.TemplateFunction> functions,
                                              Matcher blockMatcher, int lastEnd) {
        String innerTemplate = findBlockEnd(template, blockMatcher.end(), "/unless");
        if (innerTemplate == null) {
            return template.substring(lastEnd);
        }

        String[] parts = innerTemplate.split("\n", 2);
        String blockContent = parts[0];
        String endTag = parts[1];

        // unless 与 if 相反
        Object condValue = evaluateExpression(condition, placeholders, functions);
        boolean condResult = TemplateUtils.isTruthy(condValue);

        if (!condResult) {
            return blockContent + endTag;
        } else {
            return endTag;
        }
    }

    /**
     * 处理 each 块
     */
    private static String processEachBlock(String template, String expression, Map<String, Object> placeholders,
                                           Map<String, TemplateEngine.TemplateFunction> functions,
                                           Matcher blockMatcher, int lastEnd) {
        // 解析 each 别名语法
        Matcher aliasMatcher = EACH_ALIAS_PATTERN.matcher(expression);
        String itemName = "item";
        String itemsPath;

        if (aliasMatcher.matches()) {
            itemsPath = aliasMatcher.group(1);
            itemName = aliasMatcher.group(2);
        } else {
            itemsPath = expression;
        }

        String innerTemplate = findBlockEnd(template, blockMatcher.end(), "/each");
        if (innerTemplate == null) {
            return template.substring(lastEnd);
        }

        String[] parts = innerTemplate.split("\n", 2);
        String blockContent = parts[0];
        String endTag = parts[1];

        // 获取迭代对象
        Object items = resolvePath(itemsPath, placeholders);
        if (!(items instanceof Iterable)) {
            return endTag;
        }

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Object item : (Iterable<?>) items) {
            // 创建临时上下文，将当前迭代项和索引加入
            Map<String, Object> iterationContext = new HashMap<>(placeholders);
            iterationContext.put(itemName, item);
            iterationContext.put(itemName + "_index", index);

            // 如果迭代项是 Map，展开其键值到上下文中，便于直接访问
            if (item instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                    if (entry.getKey() != null) {
                        iterationContext.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }

            // 递归渲染块内容
            String iterationResult = processSimplePlaceholders(blockContent, iterationContext, functions);
            sb.append(iterationResult);
            index++;
        }

        return sb + endTag;
    }

    /**
     * 处理 eq 块
     */
    private static String processEqBlock(String template, String expression, Map<String, Object> placeholders,
                                         Map<String, TemplateEngine.TemplateFunction> functions,
                                         Matcher blockMatcher, int lastEnd) {
        // 解析 {{#eq a b}} 或 {{#eq a "value"}}
        String[] exprParts = expression.split("\\s+", 2);
        if (exprParts.length < 2) {
            return template.substring(lastEnd);
        }

        String left = exprParts[0];
        String right = exprParts[1].replace("\"", "").trim();

        String innerTemplate = findBlockEnd(template, blockMatcher.end(), "/eq");
        if (innerTemplate == null) {
            return template.substring(lastEnd);
        }

        String[] blockParts = innerTemplate.split("\n", 2);
        String blockContent = blockParts[0];
        String endTag = blockParts[1];

        // 比较两个值
        Object leftValue = evaluateExpression(left, placeholders, functions);
        String leftStr = leftValue != null ? leftValue.toString() : "";
        boolean isEqual = leftStr.equals(right);

        if (isEqual) {
            return blockContent + endTag;
        } else {
            return endTag;
        }
    }

    /**
     * 查找块结束标签
     */
    private static String findBlockEnd(String template, int start, String endTag) {
        String searchStr = "{{" + endTag + "}}";
        int endIndex = template.indexOf(searchStr, start);
        if (endIndex == -1) {
            return null;
        }
        // 返回：块内容 + 换行符 + 结束标签
        // 块内容 = 从 start 到 endIndex（结束标签开始位置）
        // 结束标签 = searchStr
        return template.substring(start, endIndex) + "\n" + searchStr;
    }

    /**
     * 处理简单占位符（变量、函数调用、默认值、表达式）
     */
    private static String processSimplePlaceholders(String template, Map<String, Object> placeholders,
                                                     Map<String, TemplateEngine.TemplateFunction> functions) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = DOUBLE_BRACE_PATTERN.matcher(template);
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(template, lastEnd, matcher.start());

            String placeholder = matcher.group(1).trim();
            int matchStart = matcher.start();
            int matchEnd = matcher.end();

            String replacement = processPlaceholder(placeholder, placeholders, functions);
            result.append(replacement);

            lastEnd = matchEnd;
        }

        result.append(template.substring(lastEnd));
        return result.toString();
    }

    /**
     * 处理单个占位符
     */
    private static String processPlaceholder(String placeholder, Map<String, Object> placeholders,
                                             Map<String, TemplateEngine.TemplateFunction> functions) {
        // 检查默认值语法
        Matcher defaultMatcher = DEFAULT_VALUE_PATTERN.matcher(placeholder);
        if (defaultMatcher.matches()) {
            String path = defaultMatcher.group(1).trim();
            String defaultValue = defaultMatcher.group(2);
            Object value = resolvePath(path, placeholders);
            if (value != null && TemplateUtils.isTruthy(value)) {
                return TemplateUtils.escapeReplacement(value.toString());
            }
            return TemplateUtils.escapeReplacement(defaultValue);
        }

        // 检查表达式
        Matcher exprMatcher = EXPRESSION_PATTERN.matcher(placeholder);
        if (exprMatcher.matches()) {
            Object result = evaluateExpression(placeholder, placeholders, functions);
            return result != null ? TemplateUtils.escapeReplacement(result.toString()) : "";
        }

        // 检查函数调用
        Matcher funcMatcher = FUNCTION_PATTERN.matcher(placeholder);
        if (funcMatcher.matches()) {
            String funcName = funcMatcher.group(1);
            String argsStr = funcMatcher.group(2);

            TemplateEngine.TemplateFunction func = functions.get(funcName);
            if (func != null) {
                Object[] args = parseArgs(argsStr, placeholders, functions);
                Object result = func.apply(args);
                return result != null ? TemplateUtils.escapeReplacement(result.toString()) : "";
            }
        }

        // 普通变量
        Object value = resolvePath(placeholder, placeholders);
        return value != null ? TemplateUtils.escapeReplacement(value.toString()) : "";
    }

    /**
     * 评估表达式
     */
    private static Object evaluateExpression(String expr, Map<String, Object> placeholders,
                                             Map<String, TemplateEngine.TemplateFunction> functions) {
        Matcher exprMatcher = EXPRESSION_PATTERN.matcher(expr);
        if (exprMatcher.matches()) {
            String left = exprMatcher.group(1);
            String op = exprMatcher.group(2);
            String right = exprMatcher.group(3);

            Object leftVal = getNumericValue(left, placeholders);
            Object rightVal = getNumericValue(right, placeholders);

            if (leftVal instanceof Number && rightVal instanceof Number) {
                double leftNum = ((Number) leftVal).doubleValue();
                double rightNum = ((Number) rightVal).doubleValue();

                return switch (op) {
                    case "+" -> leftNum + rightNum;
                    case "-" -> leftNum - rightNum;
                    case "*" -> leftNum * rightNum;
                    case "/" -> rightNum != 0 ? leftNum / rightNum : 0;
                    default -> expr;
                };
            }
        }

        // 直接返回变量值
        return resolvePath(expr, placeholders);
    }

    /**
     * 获取数值
     */
    private static Object getNumericValue(String value, Map<String, Object> placeholders) {
        try {
            if (value.matches("\\d+\\.?\\d*")) {
                return value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);
            }
        } catch (NumberFormatException ignored) {
        }
        return resolvePath(value, placeholders);
    }

    /**
     * 解析参数列表
     */
    private static Object[] parseArgs(String argsStr, Map<String, Object> placeholders,
                                       Map<String, TemplateEngine.TemplateFunction> functions) {
        List<Object> args = new ArrayList<>();
        String[] parts = argsStr.trim().split("\\s+");

        for (String part : parts) {
            // 去除引号
            if (part.startsWith("\"") && part.endsWith("\"") && part.length() >= 2) {
                args.add(part.substring(1, part.length() - 1));
            } else {
                // 尝试作为变量解析
                Object value = resolvePath(part, placeholders);
                if (value != null) {
                    args.add(value);
                } else {
                    // 作为字符串原样返回
                    args.add(part);
                }
            }
        }

        return args.toArray();
    }

    /**
     * 处理单花括号格式（兼容）
     */
    private static String processSingleBrace(String template, Map<String, Object> placeholders) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = SINGLE_BRACE_PATTERN.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            Object value = resolvePath(placeholder, placeholders);
            String replacement = value != null ? TemplateUtils.escapeReplacement(value.toString()) : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 解析路径，支持嵌套如 user.name
     */
    private static Object resolvePath(String path, Map<String, Object> placeholders) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = placeholders;

        for (String part : parts) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else if (current instanceof List) {
                try {
                    int index = Integer.parseInt(part);
                    if (index >= 0 && index < ((List<?>) current).size()) {
                        current = ((List<?>) current).get(index);
                    } else {
                        return null;
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * 检查模板中是否包含某个占位符（双花括号格式）
     */
    public static boolean containsPlaceholder(String template, String placeholder) {
        if (template == null || placeholder == null) {
            return false;
        }
        return template.contains("{{" + placeholder + "}}");
    }
}
