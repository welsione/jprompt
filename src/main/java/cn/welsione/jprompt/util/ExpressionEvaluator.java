package cn.welsione.jprompt.util;

import cn.welsione.jprompt.TemplateException;
import cn.welsione.jprompt.engine.TemplateEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ExpressionEvaluator {

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)\\s+(.+)$");
    private static final Pattern DEFAULT_VALUE_PATTERN = Pattern.compile("^([^!]+)!\\s*\"([^\"]*)\"$");
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile(
            "^\\s*([a-zA-Z_][a-zA-Z0-9_.]*|\\d+\\.?\\d*)\\s*([+\\-*/])\\s*([a-zA-Z_][a-zA-Z0-9_.]*|\\d+\\.?\\d*)\\s*$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+\\.?\\d*");

    private ExpressionEvaluator() {
    }

    static String renderPlaceholder(String placeholder, RenderContext context) {
        Matcher defaultMatcher = DEFAULT_VALUE_PATTERN.matcher(placeholder);
        if (defaultMatcher.matches()) {
            String path = defaultMatcher.group(1).trim();
            String defaultValue = defaultMatcher.group(2);
            Object value = context.resolve(path);
            return TemplateUtils.isTruthy(value) ? TemplateUtils.toStringOrEmpty(value) : defaultValue;
        }

        Matcher funcMatcher = FUNCTION_PATTERN.matcher(placeholder);
        if (funcMatcher.matches()) {
            String funcName = funcMatcher.group(1);
            TemplateEngine.TemplateFunction func = context.function(funcName);
            if (func != null) {
                Object result = func.apply(parseArgs(funcMatcher.group(2), context).toArray());
                return TemplateUtils.toStringOrEmpty(result);
            }
        }

        Object result = evaluate(placeholder, context);
        return result != null ? TemplateUtils.toStringOrEmpty(result) : "{{" + placeholder + "}}";
    }

    static Object evaluate(String expr, RenderContext context) {
        Matcher exprMatcher = EXPRESSION_PATTERN.matcher(expr);
        if (exprMatcher.matches()) {
            Object leftVal = getNumericValue(exprMatcher.group(1), context);
            Object rightVal = getNumericValue(exprMatcher.group(3), context);

            if (leftVal instanceof Number && rightVal instanceof Number) {
                double leftNum = ((Number) leftVal).doubleValue();
                double rightNum = ((Number) rightVal).doubleValue();

                return switch (exprMatcher.group(2)) {
                    case "+" -> leftNum + rightNum;
                    case "-" -> leftNum - rightNum;
                    case "*" -> leftNum * rightNum;
                    case "/" -> rightNum != 0 ? leftNum / rightNum : 0;
                    default -> expr;
                };
            }
        }
        return context.resolve(expr);
    }

    static List<Object> parseArgs(String argsStr, RenderContext context) {
        List<Object> args = new ArrayList<>();
        for (String token : tokenizeArgs(argsStr)) {
            if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
                args.add(token.substring(1, token.length() - 1));
                continue;
            }
            Object value = evaluate(token, context);
            args.add(Objects.requireNonNullElse(value, token));
        }
        return args;
    }

    private static Object getNumericValue(String value, RenderContext context) {
        if (NUMERIC_PATTERN.matcher(value).matches()) {
            try {
                return value.contains(".") ? Double.parseDouble(value) : Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return context.resolve(value);
    }

    private static List<String> tokenizeArgs(String argsStr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
                current.append(c);
            } else if (Character.isWhitespace(c) && !inQuote) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (inQuote) {
            throw new TemplateException("函数参数引号未闭合: " + argsStr);
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
