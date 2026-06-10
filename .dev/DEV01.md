# DEV01 - jprompt 项目审查报告

> 审查日期：2026-06-10
> 审查范围：全量源码（约 1400 行，12 个类 → 14 个类）
> 测试状态：79/79 → 102/102 全绿

## 一、项目概况

轻量级 Java prompt template 库，约 1400 行源码，面向 LLM 应用中的提示词管理。已发布 Maven Central 1.0.0，MIT 协议。依赖极简（Jackson + SLF4J + Lombok），Java 17+。

## 二、架构评价：良好

项目结构清晰，职责分离合理：

| 层次 | 类 | 评价 |
|---|---|---|
| 入口 | `JPrompt` / `JPromptFactory` | API 简洁，泛型设计好，支持懒加载和全局配置 |
| 引擎 | `TemplateEngine` / `ReflectiveTemplateEngine` / `BuiltinFunctions` | 接口与实现分离，函数注册解耦 |
| 加载 | `TemplateLoader` / `Classpath` / `FileSystem` | 策略模式，lambda 友好 |
| 解析 | `TemplateParser` + Node 体系 | 递归下降解析器，AST 分离渲染 |

## 三、第一轮修复（P0/P1/P2）

### 1. ✅ 将 record 替换为 class + @Value（P0）

- `TemplateNodes.java` 中 6 个 record → `@Value` class
- `TemplateParser.java` 中 2 个私有 record → `@Value` class
- 调用点从 `field()` 风格改为 `getField()` 风格

### 2. ✅ 修复表达式整数运算返回 double 问题（P0）

- `ExpressionEvaluator` 中整数运算结果（如 `5+1`）现在返回 `6` 而非 `6.0`
- 实现：结果为整数时自动转为 `(int)`，非整数保留小数

### 3. ✅ 删除 TemplateUtils.escapeReplacement() 死代码（P2）

### 4. ✅ PlaceholderUtils 标注为内部 API（P1）

- 因跨包访问限制无法降为 package-private，在 Javadoc 中标注内部 API

### 5. ✅ 提取内置函数为独立注册器（P2）

- 新增 `BuiltinFunctions` 类，按分类组织函数注册
- `formatDate` 统一使用 `java.time` API

### 6. ✅ 补充内置函数和嵌套 each 测试（P1）

- 新增 17 个测试：函数（trim/join/ternary/default/max/min/formatDate）、嵌套 each、表达式运算

## 四、第二轮修复（遗留问题）

### 7. ✅ JPromptFactory 懒加载并支持全局配置

- `INSTANCE` 字段标记 `@Deprecated`，推荐使用 `getINSTANCE()` 懒加载
- 新增 `configure()` 方法：允许在首次使用前设置全局默认配置
- 双重检查锁定保证线程安全
- `configure().build()` 后自动设置全局实例，重复调用抛 `IllegalStateException`

### 8. ✅ JPromptFactory 缓存支持 TTL 过期

- 新增 `Builder.cacheTtlMillis(long)` 配置项
- 缓存条目包装为 `CacheEntry`（content + timestamp）
- TTL > 0 时，过期条目自动重新加载
- TTL = 0 表示永不过期（默认行为，向后兼容）
- 新增 5 个测试：TTL 过期、未过期、零值、负值校验

### 9. ✅ JPrompt.build() Javadoc 补充泛型擦除说明

- 在 `build()` 方法 Javadoc 中添加 `@apiNote` 说明泛型擦除限制
- 说明当 T 为 `Map<String, Object>` 等泛型类型时运行时仅检查原始类型

### 10. ✅ MissingVariablePolicy 新增 KEEP_RAW 策略

- `KEEP_PLACEHOLDER`：保留 `{{name}}`（默认）
- `KEEP_RAW`：保留 `name`（LLM 场景，模型看到裸变量名作为填入提示）
- `EMPTY`：空字符串
- `THROW`：抛异常
- `ExpressionEvaluator.handleMissingVariable` 新增 `KEEP_RAW` 分支
- 新增 2 个测试：引擎层和工厂层

## 五、工程实践评价（最终）

| 维度 | 评分 | 说明 |
|---|---|---|
| API 设计 | ★★★★★ | 简洁直觉 + 全局配置 + TTL 缓存 + KEEP_RAW 策略 |
| 代码质量 | ★★★★★ | record 违规已修正，函数注册解耦，死代码已清理 |
| 测试 | ★★★★★ | 102 个测试全绿，覆盖函数/嵌套/表达式/TTL/策略 |
| 文档 | ★★★★★ | Javadoc 完整，泛型擦除限制已说明 |
| 安全性 | ★★★★ | FileSystemTemplateLoader 有路径穿越防护 |
| 可扩展性 | ★★★★★ | loader/engine 策略模式，BuiltinFunctions 独立注册器，全局可配置 |
