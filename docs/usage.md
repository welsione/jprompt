# jprompt Usage Guide

jprompt 是一个轻量级 Java prompt template 库，面向 AI Agent 和 LLM 应用中的提示词管理、模板渲染和模板加载。

## Requirements

- Java 17+
- Maven

## Installation

```xml
<dependency>
    <groupId>cn.welsione</groupId>
    <artifactId>jprompt</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

把模板文件放到 `src/main/resources/prompts/user.md`：

```markdown
用户姓名: {{name}}
用户年龄: {{age}}
状态: {{#if active}}在线{{#else}}离线{{/if}}
```

定义数据对象并渲染：

```java
public class UserData {
    private String name;
    private int age;
    private boolean active;

    // getters/setters
}

JPrompt<UserData> prompt = JPrompt.template("prompts/user.md", UserData.class);

UserData data = new UserData();
data.setName("张三");
data.setAge(30);
data.setActive(true);

String rendered = prompt.build(data);
```

输出：

```text
用户姓名: 张三
用户年龄: 30
状态: 在线
```

静态提示词也使用同一个入口：

```java
JPrompt<Void> systemPrompt = JPrompt.get("prompts/system.md");
String content = systemPrompt.get();
```

运行时字符串模板可以直接使用 `templateInline`：

```java
JPrompt<Map> prompt = JPrompt.templateInline("Hello, {{name}}", Map.class);
String rendered = prompt.build(Map.of("name", "Alice"));
```

## Template Syntax

### Variables

```markdown
{{name}}
{{user.name}}
{{users.0.name}}
```

单花括号如 `{name}` 会按普通文本处理，不是模板语法。

### Default Values

```markdown
{{name!"匿名用户"}}
{{title!"无标题"}}
```

默认值会在变量不存在、为 `null`、空字符串、`false`、数字 `0` 或空集合时生效。

### Conditions

```markdown
{{#if active}}
用户在线
{{#else}}
用户离线
{{/if}}

{{#unless banned}}
用户未禁用
{{/unless}}
```

### Loops

```markdown
{{#each users as user}}
- {{user.name}} ({{user.age}})
{{/each}}
```

循环体内也可以直接访问 Map item 的字段：

```markdown
{{#each users}}
- {{name}}
{{/each}}
```

循环索引通过 `item_index` 或别名前缀访问：

```markdown
{{#each users as user}}
{{user_index}}. {{user.name}}
{{/each}}
```

### Equality

```markdown
{{#eq status "active"}}
已激活
{{/eq}}

{{#eq status expectedStatus}}
状态匹配
{{#else}}
状态不匹配
{{/eq}}
```

`eq` 当前按字符串结果比较。

### Functions

内置函数：

```markdown
{{upperCase name}}
{{lowerCase name}}
{{capitalize name}}
{{trim description}}
{{length items}}
{{join tags ","}}
{{formatDate date "yyyy-MM-dd"}}
{{ternary active "在线" "离线"}}
{{max a b}}
{{min a b}}
```

注册自定义函数：

```java
ReflectiveTemplateEngine engine = new ReflectiveTemplateEngine();
engine.registerFunction("wrap", args -> "[" + args[0] + "]");

String rendered = engine.render("Name: {{wrap name}}", Map.of("name", "Alice"));
```

### Simple Expressions

支持单个二元算术表达式：

```markdown
{{index + 1}}
{{price * quantity}}
{{total / count}}
{{remaining - 1}}
```

整数运算结果自动格式化为整数，例如 `5 + 1` 输出 `6`，而不是 `6.0`。

不支持复合表达式，例如 `{{price * quantity - discount}}`。

## Data Sources

可以传 Java Bean：

```java
public class Company {
    private String name;
    private List<Department> departments;
    // getters/setters
}
```

也可以传 `Map`：

```java
Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
data.put("skills", List.of("Java", "Python"));

JPrompt<Map> prompt = JPrompt.template("prompts/resume.md", Map.class);
String rendered = prompt.build(data);
```

嵌套集合示例：

```markdown
公司: {{name}}
{{#each departments as dept}}
部门: {{dept.name}}
{{#each dept.employees as emp}}
- {{emp.name}} / {{emp.title}}
{{/each}}
{{/each}}
```

## Loaders And Cache

默认从 classpath 加载模板：

```java
JPrompt<Void> prompt = JPrompt.get("prompts/system.md");
```

使用文件系统加载器，适合开发期热改提示词：

```java
JPromptFactory factory = JPromptFactory.builder()
    .loader(new FileSystemTemplateLoader(Paths.get("src/main/resources")))
    .cacheEnabled(false)
    .build();

JPrompt<UserData> prompt = factory.template("prompts/user.md", UserData.class);
```

使用自定义加载器：

```java
JPromptFactory factory = JPromptFactory.builder()
    .loader(path -> "Hello, {{name}}")
    .build();
```

组合多个加载器：

```java
JPromptFactory factory = JPromptFactory.builder()
    .loader(new CompositeTemplateLoader(
        new FileSystemTemplateLoader(Paths.get("prompts")),
        new ClasspathTemplateLoader()
    ))
    .build();
```

缓存控制：

```java
JPromptFactory factory = JPromptFactory.getInstance();
factory.clearCache();
factory.evictCache("prompts/user.md");
```

### Cache TTL

设置缓存存活时间，过期后自动重新加载模板文件：

```java
JPromptFactory factory = JPromptFactory.builder()
    .loader(new FileSystemTemplateLoader(Paths.get("src/main/resources")))
    .cacheTtlMillis(5000)
    .build();
```

`cacheTtlMillis(0)` 表示永不过期，也是默认行为。

## Global Configuration

默认实例通过 `JPromptFactory.getInstance()` 懒加载获取。可在首次使用前通过 `configure()` 替换全局配置：

```java
JPromptFactory factory = JPromptFactory.configure()
    .loader(new FileSystemTemplateLoader(Paths.get("prompts")))
    .missingVariablePolicy(MissingVariablePolicy.KEEP_RAW)
    .cacheTtlMillis(3000)
    .build();

// 之后 JPrompt.get() / JPrompt.template() 将使用此配置
```

如需完全独立的配置，使用 `JPromptFactory.builder()` 创建新实例。

## Missing Variable Policy

默认保留缺失变量占位符：

```markdown
Hello, {{name}}!
```

如果 `name` 不存在，输出仍为 `Hello, {{name}}!`。

可以通过 `MissingVariablePolicy` 调整：

```java
JPromptFactory factory = JPromptFactory.builder()
    .missingVariablePolicy(MissingVariablePolicy.EMPTY)
    .build();
```

| 策略 | 行为 |
|------|------|
| `KEEP_PLACEHOLDER` | 保留原始占位符 `{{name}}`，默认行为 |
| `KEEP_RAW` | 仅保留变量名 `name`，适用于 LLM 场景 |
| `EMPTY` | 输出空字符串 |
| `THROW` | 抛出 `TemplateException` |

也可以直接配置引擎：

```java
ReflectiveTemplateEngine engine = new ReflectiveTemplateEngine(MissingVariablePolicy.THROW);
```

## API Reference

### JPrompt

```java
JPrompt<Void> staticPrompt = JPrompt.get("prompts/static.md");
JPrompt<UserData> template = JPrompt.template("prompts/user.md", UserData.class);
JPrompt<Map> inline = JPrompt.templateInline("Hello, {{name}}", Map.class);

String content = staticPrompt.get();
String rendered = template.build(data);
```

### JPromptFactory

```java
JPromptFactory factory = JPromptFactory.builder()
    .loader(new ClasspathTemplateLoader())
    .engine(new ReflectiveTemplateEngine())
    .missingVariablePolicy(MissingVariablePolicy.KEEP_PLACEHOLDER)
    .cacheEnabled(true)
    .cacheTtlMillis(0)
    .build();

JPromptFactory defaultFactory = JPromptFactory.getInstance();
```

### TemplateLoader

```java
TemplateLoader classpathLoader = new ClasspathTemplateLoader();
TemplateLoader fileLoader = new FileSystemTemplateLoader(Paths.get("prompts"));
TemplateLoader memoryLoader = path -> "Template: {{name}}";
```

## Current Boundaries

jprompt 设计目标是轻量 prompt template，不是完整通用模板语言。当前有几个刻意保持简单的边界：

- 表达式只支持单个二元运算
- 函数参数支持空格分隔和双引号字符串，不支持转义引号
- `eq` 按字符串结果比较
- 模板每次渲染都会解析一次，暂未暴露预编译模板 API

## Project Structure

```text
src/main/java/cn/welsione/jprompt/
├── JPrompt.java
├── JPromptFactory.java
├── MissingVariablePolicy.java
├── TemplateException.java
├── engine/
│   ├── TemplateEngine.java
│   ├── ReflectiveTemplateEngine.java
│   └── BuiltinFunctions.java
├── loader/
│   ├── TemplateLoader.java
│   ├── ClasspathTemplateLoader.java
│   ├── FileSystemTemplateLoader.java
│   └── CompositeTemplateLoader.java
└── util/
    ├── PlaceholderUtils.java
    ├── TemplateParser.java
    ├── TemplateNodes.java
    ├── RenderContext.java
    ├── ExpressionEvaluator.java
    └── TemplateUtils.java
```
