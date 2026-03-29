# jprompt

```
=========================================================================

                            jprompt

                     轻量级提示词模板框架
               Lightweight Prompt Template Framework

=========================================================================
```

轻量级提示词模板框架，支持静态提示词和动态模板渲染，专为 AI Agent 和 LLM 应用设计。

## Badges

[![Maven Central](https://img.shields.io/maven-central/v/cn.welsione/jprompt)](https://search.maven.org/artifact/cn.welsione/jprompt)
[![License](https://img.shields.io/github/license/welsione/jprompt)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-green.svg)](https://adoptium.net/)
[![Stars](https://img.shields.io/github/stars/welsione/jprompt?style=social)](https://github.com/welsione/jprompt)

## 功能特性

```
┌─────────────────────────────────────────────────────────────────┐
│                     jprompt 模板渲染流程                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────┐      ┌──────────────┐      ┌─────────────┐  │
│   │  Template   │ ───▶ │    Engine    │ ───▶ │    Output   │  │
│   │  {{name}}   │      │   (Render)    │      │   "张三"    │  │
│   └─────────────┘      └──────────────┘      └─────────────┘  │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  支持的占位符格式                                        │   │
│   │  • 双花括号 {{name}}         • 单花括号 {name}           │   │
│   │  • 默认值 {{name!"默认值"}}   • 嵌套 {{user.name}}       │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  块级标签                                                │   │
│   │  • {{#if}} / {{#unless}}   条件渲染                     │   │
│   │  • {{#each}}               循环迭代                     │   │
│   │  • {{#eq}}                 相等判断                     │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

- **静态提示词** - 不可变的提示词内容，直接获取内容
- **动态模板** - 支持占位符替换，泛型类型安全
- **双占位符格式** - 支持 `{{key}}` 和 `{key}` 两种格式
- **嵌套属性** - 支持 `{{user.name}}` 嵌套路径解析
- **块级标签** - 条件判断、循环迭代、相等判断
- **内置函数** - 大小写转换、日期格式化、集合操作等
- **表达式运算** - 支持基本算术运算
- **轻量级依赖** - 仅依赖 Jackson 和 SLF4J

## 适用场景

```
┌─────────────────────────────────────────────────────────────────┐
│                         典型使用场景                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   🤖 AI Agent                                                   │
│   ├── 系统提示词管理        ├── 角色定义模板                     │
│   └── 工具调用提示词       └── 多轮对话模板                     │
│                                                                  │
│   📝 LLM 应用开发                                                │
│   ├── 提示词版本管理        ├── 模板复用                        │
│   └── 动态内容渲染          └── 类型安全                        │
│                                                                  │
│   🎮 游戏开发                                                    │
│   ├── NPC 对话模板          ├── 任务描述模板                    │
│   └── 剧情脚本生成          └── 多语言支持                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

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
    private boolean isActive;

    // getters/setters
    // Lombok @Data 注解可简化
}
```

### 3. 准备模板文件

**模板文件放在 `src/main/resources/` 目录下**，jprompt 从 classpath 加载模板文件。

```
src/main/resources/
└── prompts/
    └── user.md    ← 模板文件位置
```

在 `resources/prompts/user.md` 中：

```markdown
用户姓名: {{name}}
用户年龄: {{age}}
状态: {{#if isActive}}在线{{/if}}
```

### 4. 使用模板

```java
// 获取静态提示词内容
String content = systemPrompt.get();

// 渲染动态模板
UserData data = new UserData();
data.setName("张三");
data.setAge(30);
data.setActive(true);

String rendered = userTemplate.build(data);
```

**运行结果：**

```
用户姓名: 张三
用户年龄: 30
状态: 在线
```

## 模板文件

### 文件位置

jprompt 从 classpath 加载模板文件，模板文件应放在 `src/main/resources/` 或 `src/test/resources/` 目录下：

```
项目目录/
├── src/
│   ├── main/
│   │   └── resources/
│   │       ├── prompts/
│   │       │   ├── system.md
│   │       │   ├── user.md
│   │       │   └── agent/
│   │       │       └── planner.md
│   │       └── templates/
│   │           └── story.md
│   └── test/
│       └── resources/
│           └── prompts/
│               └── test.md
```

### 目录结构图

```
resources/
├── prompts/                    # 提示词模板
│   ├── system.md               # 系统提示词
│   ├── user.md                 # 用户提示词
│   └── agent/                  # Agent 相关
│       ├── radar.md            # 雷达 Agent
│       ├── architect.md        # 架构 Agent
│       └── writer.md            # 写作 Agent
└── templates/                  # 其他模板
    ├── email.md
    └── story.md
```

## 占位符格式

### 基本格式

| 格式 | 示例 | 说明 |
|------|------|------|
| 双花括号 | `{{name}}` | 推荐使用 |
| 单花括号 | `{name}` | 兼容现有模板 |

### 嵌套属性

支持点号路径解析：

```
{{user.name}}        → data.getUser().getName()
{{address.city}}     → data.getAddress().getCity()
{{company.dept.name}} → 多层嵌套
```

### 默认值语法

占位符不存在时使用默认值：

```markdown
{{name!"匿名用户"}}
{{title!"无标题"}}
{{description!"暂无描述"}}
```

## 块级标签

### 条件渲染

**if 标签** - 条件为真时渲染内容：

```markdown
{{#if isActive}}
用户已激活
{{/if}}

{{#if user.isAdmin}}
管理员面板
{{/if}}
```

**unless 标签** - 条件为假时渲染内容（与 if 相反）：

```markdown
{{#unless isEmpty}}
列表不为空
{{/unless}}

{{#unless user.isBanned}}
用户未被禁用
{{/unless}}
```

**流程图：**

```
{{#if condition}}              {{#unless condition}}
     |                              |
     v                              v
  [True?]                      [False?]
     |                              |
   Yes | No                    Yes | No
     |   |                        |   |
     v   v                        v   v
   显示  隐藏                    隐藏  显示
```

### 循环迭代

**each 标签** - 遍历集合：

```markdown
用户列表:
{{#each users as user}}
- {{user.name}} ({{user.age}}岁)
{{/each}}
```

**嵌套循环：**

```markdown
{{#each departments as dept}}
部门: {{dept.name}}
{{#each dept.employees as emp}}
  - {{emp.name}}
{{/each}}
{{/each}}
```

**流程图：**

```
{{#each items as item}}
       │
       ▼
   [Collection]
       │
    ┌──┴──┐
    │     │
    ▼     ▼
  Item1  Item2  ...
    │     │
    ▼     ▼
 渲染   渲染
    │     │
    └──┬──┘
       ▼
    [Next]
       │
       ▼
    (End)
```

### 相等判断

**eq 标签** - 判断值是否相等：

```markdown
{{#eq status "active"}}
状态: 激活
{{/eq}}

{{#eq role "admin"}}
管理员权限
{{/eq}}
```

**流程图：**

```
{{#eq status "active"}}
       │
       ▼
  [status == "active"?]
       │
    ┌──┴──┐
    │     │
   Yes    No
    │     │
    ▼     ▼
  显示   跳过
```

## 函数调用

### 内置函数

```markdown
{{upperCase name}}         → 大写: JOHN
{{lowerCase name}}         → 小写: john
{{capitalize name}}        → 首字母大写: John
{{trim description}}       → 去除首尾空格
{{length items}}           → 集合长度: 5
{{join tags ","}}          → 数组转字符串: a,b,c
{{formatDate date "yyyy-MM-dd"}} → 日期格式化: 2024-01-15
{{ternary cond "yes" "no"}} → 三元运算
{{max a b}}                → 最大值
{{min a b}}                → 最小值
```

### 自定义函数

jprompt 支持注册自定义函数：

```java
// 注册自定义函数
ReflectiveTemplateEngine engine = new ReflectiveTemplateEngine();
engine.registerFunction("pluralize", (args) -> {
    int count = (int) args[0];
    String singular = (String) args[1];
    String plural = (String) args[2];
    return count == 1 ? singular : plural;
});

// 使用自定义函数
TemplateData data = new TemplateData();
data.setItemCount(3);
data.setItemName("苹果");

String result = engine.render("我有 {{itemCount}} {{pluralize itemCount \"个\" \"些\"}} {{itemName}}", data);
// 输出: 我有 3 些 苹果
```

**函数注册流程图：**

```
┌─────────────────────────────────────────────────────────┐
│                  自定义函数注册与调用                      │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  1. 创建引擎                                             │
│     ReflectiveTemplateEngine engine = new ...            │
│              │                                           │
│              ▼                                           │
│  2. 注册函数                                             │
│     engine.registerFunction("pluralize", fn)            │
│              │                                           │
│              ▼                                           │
│  3. 渲染模板                                             │
│     engine.render("{{pluralize count ...}}", data)       │
│              │                                           │
│              ▼                                           │
│  4. 函数调用                                             │
│     ┌──────────────────┐                               │
│     │  pluralize(3,"个","些")  → "些"                   │
│     └──────────────────┘                               │
│              │                                           │
│              ▼                                           │
│  5. 输出结果                                             │
│     "我有 3 些 苹果"                                     │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

## 表达式运算

支持基本算术运算：

```markdown
{{index + 1}}           → 加法: 序号计算
{{price * quantity}}     → 乘法: 总价
{{total / count}}       → 除法: 平均值
{{remaining - 1}}       → 减法: 递减
{{budget % 1000}}       → 取模: 分组
```

**示例：**

```java
public class OrderData {
    private int quantity;
    private double price;
    private double discount;
    // getters/setters
}

OrderData order = new OrderData();
order.setQuantity(3);
order.setPrice(100.0);
order.setDiscount(10.0);

// 模板: 总价: {{price * quantity}}, 折后: {{price * quantity - discount}}
// 输出: 总价: 300.0, 折后: 290.0
```

## 高级用法

### Map 作为数据源

除了对象，还可以用 Map 作为数据源：

```java
Map<String, Object> data = new HashMap<>();
data.put("name", "张三");
data.put("age", 30);
data.put("skills", Arrays.asList("Java", "Python", "Go"));

JPrompt<Map> template = JPrompt.template("prompts/resume.md", Map.class);
String result = template.build(data);
```

### 嵌套集合的使用

```java
public class Company {
    private String name;
    private List<Department> departments;

    public static class Department {
        private String name;
        private List<Employee> employees;
        // getters/setters
    }

    public static class Employee {
        private String name;
        private String title;
        // getters/setters
    }
}
```

**模板：**

```markdown
公司: {{name}}
{{#each departments as dept}}
  部门: {{dept.name}}
  {{#each dept.employees as emp}}
    - {{emp.name}} ({{emp.title}})
  {{/each}}
{{/each}}
```

**数据流转图：**

```
┌─────────────────────────────────────────────────────────┐
│                    数据流转架构                          │
├─────────────────────────────────────────────────────────┤
│                                                          │
│   ┌─────────┐     ┌──────────────┐     ┌─────────────┐  │
│   │  Java   │ ──▶ │  Template    │ ──▶ │   String    │  │
│   │  Object │     │   Engine     │     │   Output    │  │
│   └─────────┘     └──────────────┘     └─────────────┘  │
│        │                                    │           │
│        ▼                                    ▼           │
│   ┌─────────┐                        ┌─────────────┐    │
│   │ Jackson │                        │  Rendered   │    │
│   │ flatten │                        │   Text      │    │
│   └─────────┘                        └─────────────┘    │
│        │                                    │           │
│        ▼                                    ▼           │
│   ┌─────────────────────────────────────────────┐      │
│   │  Map<String, Object>                        │      │
│   │  {                                          │      │
│   │    "name": "张三",                         │      │
│   │    "dept.employees[0].name": "李四",       │      │
│   │    ...                                     │      │
│   │  }                                         │      │
│   └─────────────────────────────────────────────┘      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### 缺失占位符

当占位符在模板中但数据中不存在时，保留原占位符格式 `{{placeholder}}`：

```java
// 模板: 欢迎, {{username}}, 您的余额: {{balance}}
// 数据: {username: "张三"}
// 输出: 欢迎, 张三, 您的余额: {{balance}}
```

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

// 强制刷新模板缓存
JPromptFactory.INSTANCE.clearCache();

// 获取所有缓存的模板
Map<String, String> cached = JPromptFactory.INSTANCE.getCache();
```

### ReflectiveTemplateEngine

```java
// 创建自定义引擎
ReflectiveTemplateEngine engine = new ReflectiveTemplateEngine();

// 注册自定义函数
engine.registerFunction("myFunc", args -> {
    // 函数实现
    return result;
});

// 渲染模板
String result = engine.render(templateContent, dataObject);
```

## 架构

```
jprompt/src/main/java/cn/welsione/jprompt/
├── JPrompt.java                   # 提示词模板类（泛型 T）
├── JPromptFactory.java            # 工厂类（枚举单例）
├── TemplateException.java         # 异常类
├── engine/
│   ├── TemplateEngine.java        # 引擎接口
│   └── ReflectiveTemplateEngine.java  # 基于 Jackson 反射的引擎实现
└── util/
    ├── PlaceholderUtils.java      # 占位符处理核心逻辑
    └── TemplateUtils.java         # 工具类（isTruthy、escapeReplacement）
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `JPrompt<T>` | 模板对象，泛型参数 T 在编译时进行类型检查 |
| `JPromptFactory` | 工厂类，负责加载模板和缓存 |
| `ReflectiveTemplateEngine` | 模板引擎实现，使用 Jackson 反射展平对象 |
| `PlaceholderUtils` | 核心渲染逻辑，三阶段处理占位符 |

## License

MIT License - 详见 [LICENSE](LICENSE) 文件。

## Contributing

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -m 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request
