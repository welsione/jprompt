package cn.welsione.jprompt.util;

import cn.welsione.jprompt.TemplateException;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TemplateParser {

    private static final Pattern DOUBLE_BRACE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL);
    private static final Pattern EACH_ALIAS_PATTERN = Pattern.compile("^(\\S+)\\s+as\\s+(\\S+)$");

    private final String template;
    private final Matcher matcher;
    private int cursor;

    TemplateParser(String template) {
        this.template = template;
        this.matcher = DOUBLE_BRACE_PATTERN.matcher(template);
    }

    ParsedTemplate parse() {
        ParseResult result = parseUntil(Set.of());
        return new ParsedTemplate(result.getNodes());
    }

    private ParseResult parseUntil(Set<String> endTags) {
        List<TemplateNode> nodes = new ArrayList<>();

        while (matcher.find()) {
            if (matcher.start() > cursor) {
                nodes.add(new TextNode(template.substring(cursor, matcher.start())));
            }

            String tag = matcher.group(1).trim();
            cursor = matcher.end();
            ParsedTag parsedTag = ParsedTag.parse(tag);

            if (endTags.contains(parsedTag.getName())) {
                return new ParseResult(nodes, parsedTag);
            }
            if (parsedTag.isClose()) {
                throw new TemplateException("未匹配的闭合标签: {{" + tag + "}}");
            }
            if ("#else".equals(parsedTag.getName())) {
                throw new TemplateException("未匹配的 else 标签");
            }

            switch (parsedTag.getName()) {
                case "#if" -> nodes.add(parseConditional(parsedTag.getContent(), false));
                case "#unless" -> nodes.add(parseConditional(parsedTag.getContent(), true));
                case "#each" -> nodes.add(parseEach(parsedTag.getContent()));
                case "#eq" -> nodes.add(parseEq(parsedTag.getContent()));
                default -> nodes.add(new VariableNode(tag));
            }
        }

        if (!endTags.isEmpty()) {
            throw new TemplateException("模板块未闭合，期望标签: " + endTags);
        }
        if (cursor < template.length()) {
            nodes.add(new TextNode(template.substring(cursor)));
            cursor = template.length();
        }
        return new ParseResult(nodes, null);
    }

    private ConditionalNode parseConditional(String expression, boolean unless) {
        String endTag = unless ? "/unless" : "/if";
        ParseResult truthy = parseUntil(Set.of("#else", endTag));
        List<TemplateNode> falsyNodes = TemplateNodes.empty();
        if (truthy.getEndTag() != null && "#else".equals(truthy.getEndTag().getName())) {
            ParseResult falsy = parseUntil(Set.of(endTag));
            falsyNodes = falsy.getNodes();
        }
        return new ConditionalNode(expression, unless, truthy.getNodes(), falsyNodes);
    }

    private EachNode parseEach(String expression) {
        Matcher aliasMatcher = EACH_ALIAS_PATTERN.matcher(expression);
        String itemName = "item";
        String itemsPath = expression;
        if (aliasMatcher.matches()) {
            itemsPath = aliasMatcher.group(1);
            itemName = aliasMatcher.group(2);
        }
        ParseResult body = parseUntil(Set.of("/each"));
        return new EachNode(itemsPath, itemName, body.getNodes());
    }

    private EqNode parseEq(String expression) {
        ParseResult truthy = parseUntil(Set.of("#else", "/eq"));
        List<TemplateNode> falsyNodes = TemplateNodes.empty();
        if (truthy.getEndTag() != null && "#else".equals(truthy.getEndTag().getName())) {
            ParseResult falsy = parseUntil(Set.of("/eq"));
            falsyNodes = falsy.getNodes();
        }
        return new EqNode(expression, truthy.getNodes(), falsyNodes);
    }

    @Value
    private static class ParseResult {
        List<TemplateNode> nodes;
        ParsedTag endTag;
    }

    @Value
    private static class ParsedTag {
        String name;
        String content;

        static ParsedTag parse(String raw) {
            if (raw.startsWith("#")) {
                int spaceIndex = raw.indexOf(' ');
                if (spaceIndex == -1) {
                    return new ParsedTag(raw, "");
                }
                return new ParsedTag(raw.substring(0, spaceIndex), raw.substring(spaceIndex + 1).trim());
            }
            if (raw.startsWith("/")) {
                return new ParsedTag(raw, "");
            }
            return new ParsedTag(raw, raw);
        }

        boolean isClose() {
            return name.startsWith("/");
        }
    }
}
