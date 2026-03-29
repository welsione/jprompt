# jprompt

轻量级提示词模板框架，支持静态提示词和动态模板渲染。

## 特性

- **静态提示词** - 不可变的提示词内容，直接获取内容
- **动态模板** - 支持占位符替换，泛型类型安全
- **双占位符格式** - 支持 `{{key}}` 和 `{key}` 两种格式
- **嵌套属性** - 支持 `{{user.name}}` 嵌套路径解析
- **轻量级依赖** - 仅依赖 Jackson 和 SLF4J

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
// 输出:
// 用户姓名: 张三
// 用户年龄: 30
```

## 占位符格式

支持两种占位符格式：

| 格式 | 示例 | 说明 |
|------|------|------|
| 双花括号 | `{{name}}` | 推荐使用 |
| 单花括号 | `{name}` | 兼容现有模板 |

支持嵌套属性：`{{user.name}}`、`{{address.city}}`

### 默认值语法

占位符不存在时使用默认值：

```
{{name!"匿名用户"}}
```

### 块级标签

支持条件判断和循环迭代：

```
{{#if hasPermission}}有权限{{/if}}
{{#unless isEmpty}}有内容{{/unless}}
{{#each items as item}}{{item.name}}{{/each}}
{{#eq status "active"}}在线{{/eq}}
```

### 函数调用

支持内置函数：

```
{{upperCase name}}    -> 大写
{{lowerCase name}}    -> 小写
{{capitalize name}}   -> 首字母大写
{{length items}}      -> 集合长度
{{formatDate date "yyyy-MM-dd"}}  -> 日期格式化
```

### 表达式运算

支持基本算术运算：

```
{{index + 1}}   -> 加法
{{count - 1}}   -> 减法
{{price * 2}}   -> 乘法
{{total / 2}}   -> 除法
```

### 缺失占位符

当占位符在模板中但数据中不存在时，保留原占位符格式 `{{placeholder}}`。

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
    ├── PlaceholderUtils.java # 占位符处理工具
    └── TemplateUtils.java    # 模板工具类
```

## License

MIT
