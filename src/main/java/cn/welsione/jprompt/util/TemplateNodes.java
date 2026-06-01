package cn.welsione.jprompt.util;

import cn.welsione.jprompt.TemplateException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

interface TemplateNode {
    String render(RenderContext context);
}

record ParsedTemplate(List<TemplateNode> nodes) {
    String render(RenderContext context) {
        return TemplateNodes.render(nodes, context);
    }
}

record TextNode(String text) implements TemplateNode {
    @Override
    public String render(RenderContext context) {
        return text;
    }
}

record VariableNode(String expression) implements TemplateNode {
    @Override
    public String render(RenderContext context) {
        return ExpressionEvaluator.renderPlaceholder(expression, context);
    }
}

record ConditionalNode(String expression, boolean unless, List<TemplateNode> truthyNodes,
                       List<TemplateNode> falsyNodes) implements TemplateNode {
    @Override
    public String render(RenderContext context) {
        boolean result = TemplateUtils.isTruthy(ExpressionEvaluator.evaluate(expression, context));
        List<TemplateNode> selected = result ^ unless ? truthyNodes : falsyNodes;
        return TemplateNodes.render(selected, context);
    }
}

record EqNode(String expression, List<TemplateNode> truthyNodes, List<TemplateNode> falsyNodes)
        implements TemplateNode {
    @Override
    public String render(RenderContext context) {
        List<Object> values = ExpressionEvaluator.parseArgs(expression, context);
        if (values.size() < 2) {
            throw new TemplateException("eq 标签至少需要两个参数: " + expression);
        }
        boolean equals = Objects.equals(
                TemplateUtils.toStringOrEmpty(values.get(0)),
                TemplateUtils.toStringOrEmpty(values.get(1))
        );
        return TemplateNodes.render(equals ? truthyNodes : falsyNodes, context);
    }
}

record EachNode(String expression, String itemName, List<TemplateNode> nodes) implements TemplateNode {
    @Override
    public String render(RenderContext context) {
        Iterable<?> iterable = toIterable(context.resolve(expression));
        if (iterable == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Object item : iterable) {
            Map<String, Object> scope = buildScope(item, index);
            context.push(scope);
            try {
                result.append(TemplateNodes.render(nodes, context));
            } finally {
                context.pop();
            }
            index++;
        }
        return result.toString();
    }

    private Map<String, Object> buildScope(Object item, int index) {
        Map<String, Object> scope = new HashMap<>();
        scope.put(itemName, item);
        scope.put(itemName + "_index", index);
        scope.put("item", item);
        scope.put("item_index", index);

        if (item instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    scope.put(entry.getKey().toString(), entry.getValue());
                }
            }
        }
        return scope;
    }

    private static Iterable<?> toIterable(Object value) {
        if (value instanceof Iterable<?>) {
            return (Iterable<?>) value;
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet();
        }
        if (value != null && value.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < Array.getLength(value); i++) {
                list.add(Array.get(value, i));
            }
            return list;
        }
        return null;
    }
}

final class TemplateNodes {

    private TemplateNodes() {
    }

    static String render(List<TemplateNode> nodes, RenderContext context) {
        if (nodes == null || nodes.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (TemplateNode node : nodes) {
            result.append(node.render(context));
        }
        return result.toString();
    }

    static List<TemplateNode> empty() {
        return Collections.emptyList();
    }
}
