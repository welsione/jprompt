# jprompt

轻量级提示词模板框架，支持静态提示词和动态模板渲染。

## 特性

- **静态提示词** - 不可变的提示词内容，直接获取内容
- **动态模板** - 支持占位符替换，编译时类型检查
- **双占位符格式** - 支持 `{{key}}` 和 `{key}` 两种格式
- **嵌套属性** - 支持 `{{user.name}}` 嵌套路径解析
- **无外部依赖** - 仅依赖 Jackson 和 SLF4J

## 安装

```xml
<dependency>
    <groupId>cn.welsione</groupId>
    <artifactId>jprompt</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

### 1. 定义提示词

```java
// 静态提示词（不可变）
JPrompt<Void> systemPrompt = JPrompt.get("prompts/system.md");

// 动态模板（可变）
JPrompt<UserData> userTemplate = JPrompt.template("prompts/user.md", UserData.class);
```

### 2. 定义数据类

```java
public class UserData {
    private String name;
    private int age;
    // getters/setters
}
```

### 3. 准备模板文件

在 `resources/prompts/user.md` 中：

```
用户姓名: {{name}}
用户年龄: {{age}}
```

### 4. 使用模板

```java
// 获取静态提示词内容
String content = systemPrompt.get();

// 渲染动态模板
UserData data = new UserData();
data.setName("张三");
data.setAge(30);

String rendered = userTemplate.build(data);
// 输出: 用户姓名: 张三\n用户年龄: 30
```

## 占位符格式

支持两种占位符格式：

| 格式 | 示例 | 说明 |
|------|------|------|
| 双花括号 | `{{name}}` | 推荐使用 |
| 单花括号 | `{name}` | 兼容现有模板 |

支持嵌套属性：`{{user.name}}`、`{{address.city}}`

## API

### JPrompt

```java
// 创建静态提示词
JPrompt<Void> staticPrompt = JPrompt.get("prompts/static.md");

// 创建动态模板
JPrompt<DataClass> template = JPrompt.template("prompts/template.md", DataClass.class);

// 获取内容（静态）
String content = staticPrompt.get();

// 渲染模板（动态）
String rendered = template.build(dataObject);
```

### JPromptFactory

```java
// 直接使用工厂
JPrompt<Void> prompt = JPromptFactory.INSTANCE.get("prompts/xxx.md");
JPrompt<Data> template = JPromptFactory.INSTANCE.template("prompts/xxx.md", Data.class);
```

## 架构

```
jprompt/
├── JPrompt.java              # 提示词模板类
├── JPromptFactory.java       # 工厂类
├── TemplateException.java    # 异常类
├── engine/
│   ├── TemplateEngine.java               # 引擎接口
│   └── ReflectiveTemplateEngine.java     # 基于反射的模板引擎
└── util/
    └── PlaceholderUtils.java # 占位符处理工具
```

## License

MIT
