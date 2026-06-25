<p align="center">
  <img src="assets/logo.svg" alt="jprompt logo" width="460">
</p>

<h1 align="center">jprompt</h1>

<p align="center">
  面向 AI Agent 与 LLM 应用的轻量级 Java Prompt Template 库。
</p>

<p align="center">
  <a href="README_EN.md">English</a> · <a href="docs/usage.md">使用文档</a>
</p>

<p align="center">
  <a href="https://search.maven.org/artifact/cn.welsione/jprompt"><img src="https://img.shields.io/maven-central/v/cn.welsione/jprompt" alt="Maven Central"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/welsione/jprompt" alt="License"></a>
  <a href="https://adoptium.net/"><img src="https://img.shields.io/badge/Java-17+-2f855a.svg" alt="Java 17+"></a>
</p>

## jprompt 是什么

jprompt 让提示词文件保持可读，同时为 Java 项目提供一个小而清晰的类型化入口，用来加载、渲染和管理 LLM Prompt。它适合把 Prompt 当作工程资产维护的团队：放进仓库、参与评审、跟随代码一起演进。

## 亮点

- 静态提示词和动态模板统一使用 `JPrompt<T>`
- 使用 Markdown 友好的 `{{name}}` 占位符
- 支持变量、默认值、条件、循环、相等判断、函数和简单表达式
- 支持 classpath、文件系统、组合加载器和自定义加载器
- 支持缓存开关、TTL 和手动缓存清理
- 支持多种缺失变量策略，方便 LLM 场景保留上下文
- Java 17+，依赖保持克制

## 安装

```xml
<dependency>
    <groupId>cn.welsione</groupId>
    <artifactId>jprompt</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 30 秒上手

创建一个 Prompt 文件：

```markdown
用户姓名: {{name}}
状态: {{#if active}}在线{{#else}}离线{{/if}}
```

在 Java 中渲染：

```java
JPrompt<UserData> prompt = JPrompt.template("prompts/user.md", UserData.class);
String text = prompt.build(userData);
```

静态提示词使用同一个入口：

```java
String systemPrompt = JPrompt.get("prompts/system.md").get();
```

## 使用文档

- [完整使用文档](docs/usage.md)：安装方式、模板语法、加载器、缓存、全局配置和 API 示例

## License

MIT License.
