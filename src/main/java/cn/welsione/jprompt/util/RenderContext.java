package cn.welsione.jprompt.util;

import cn.welsione.jprompt.MissingVariablePolicy;
import cn.welsione.jprompt.engine.TemplateEngine;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

final class RenderContext {

    private final Map<String, Object> root;
    private final Map<String, TemplateEngine.TemplateFunction> functions;
    private final MissingVariablePolicy missingVariablePolicy;
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();

    RenderContext(Map<String, Object> root, Map<String, TemplateEngine.TemplateFunction> functions) {
        this(root, functions, MissingVariablePolicy.KEEP_PLACEHOLDER);
    }

    RenderContext(Map<String, Object> root, Map<String, TemplateEngine.TemplateFunction> functions,
                  MissingVariablePolicy missingVariablePolicy) {
        this.root = root;
        this.functions = functions;
        this.missingVariablePolicy = missingVariablePolicy;
    }

    void push(Map<String, Object> scope) {
        scopes.push(scope);
    }

    void pop() {
        scopes.pop();
    }

    Object resolve(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(path)) {
                return scope.get(path);
            }
        }
        if (root.containsKey(path)) {
            return root.get(path);
        }

        String[] parts = path.split("\\.");
        Object current = resolveFirstPart(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            current = readPart(current, parts[i]);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    TemplateEngine.TemplateFunction function(String name) {
        return functions.get(name);
    }

    MissingVariablePolicy missingVariablePolicy() {
        return missingVariablePolicy;
    }

    private Object resolveFirstPart(String part) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(part)) {
                return scope.get(part);
            }
        }
        return root.get(part);
    }

    private static Object readPart(Object current, String part) {
        if (current == null) {
            return null;
        }
        if (current instanceof Map<?, ?> map) {
            return map.get(part);
        }
        if (current instanceof List<?> list) {
            return readIndexed(list, part);
        }
        if (current.getClass().isArray()) {
            return readArray(current, part);
        }
        return null;
    }

    private static Object readIndexed(List<?> list, String part) {
        try {
            int index = Integer.parseInt(part);
            return index >= 0 && index < list.size() ? list.get(index) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Object readArray(Object array, String part) {
        try {
            int index = Integer.parseInt(part);
            return index >= 0 && index < Array.getLength(array) ? Array.get(array, index) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
