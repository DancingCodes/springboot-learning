# Knife4j 接口文档

## 和手写 Markdown 的区别

手写接口文档的问题是——加了接口要记得改文档，忘了改就对不上。Knife4j 是**从代码注解自动生成**，注解和代码在一起，改了代码忘了改注解 IDE 会提示。

## 依赖

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

> Spring Boot 3.x 必须用带 `jakarta` 的 artifact，老的 `knife4j-spring-boot-starter` 是给 2.x 的。

> **重要**：不要额外声明 `springdoc-openapi-starter-webmvc-ui` 依赖，Knife4j 4.5.0 已经内置了 springdoc 2.5.0，重复声明会导致版本冲突 → "文档请求异常"。

> 传递依赖中 commons-lang3 版本有 CVE-2025-48924 漏洞，需显式覆盖为 3.18.0：
> ```xml
> <!-- 覆盖 knife4j → springdoc → swagger-core 传递进来的低版本 commons-lang3，修复 CVE-2025-48924 -->
> <dependency>
>     <groupId>org.apache.commons</groupId>
>     <artifactId>commons-lang3</artifactId>
>     <version>3.18.0</version>
> </dependency>
> ```

## 配置类

```java
@Bean
public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info().title("标题").version("1.0"))
        .addSecurityItem(...)  // 全局 JWT token 输入框
        .components(new Components()
            .addSecuritySchemes("Bearer", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer")));
}
```

配置了全局 Bearer token 后，Knife4j 页面右上角会出现锁图标，填一次 token 所有接口调试都会带上。

## 核心注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Tag(name = "用户管理")` | Controller 类 | 左侧导航分组名 |
| `@Operation(summary = "分页查询")` | Controller 方法 | 接口说明 |
| `@Parameter(description = "页码")` | 方法参数 | 参数说明 |
| `@Schema(description = "用户")` | 实体类 | 实体说明 |
| `@Schema(description = "姓名")` | 实体字段 | 字段说明 |

## 访问

启动后浏览器打开：`http://localhost:8080/doc.html`

页面功能：
- 左侧按 `@Tag` 分组展示所有接口
- 点击接口 → Try it → 填参数 → 发送 → 直接看到响应
- 右上角锁图标 → 填 token → 所有接口自动带 `Authorization: Bearer <token>`

## Security 配置

需要放行 Knife4j 的三个路径，否则文档页 403：

```java
.requestMatchers("/auth/login").permitAll()
.requestMatchers("/doc.html", "/v3/api-docs/**", "/webjars/**").permitAll()
```

- `/doc.html` — 文档页面
- `/v3/api-docs/**` — OpenAPI 3.0 的 JSON 数据
- `/webjars/**` — 页面用到的 JS/CSS 静态资源

## @Hidden 注解

`@RestControllerAdvice` 的全局异常处理类需要加 `@Hidden` 注解，否则 Swagger 扫描时会触发异常，异常被全局处理器 catch 后返回 Result 格式 JSON，Knife4j 前端收到非标准 OpenAPI 格式就报"文档请求异常"。

```java
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler { ... }
```

## 注解在哪看效果

重启应用 → `http://localhost:8080/doc.html` → 左侧看到"用户管理""账户管理""认证"三个分组 → 点开就是具体接口。

全局 token 测试流程：
1. 先点"认证 → 登录获取 token" → Try it → 填 admin/admin123 → 拿到 token
2. 右上角锁图标 → 粘贴 token → 点 Authorize
3. 再去调用户管理的接口 → 自动带 Authorization 头

## 常见问题："文档请求异常"

**根因**：Knife4j 前端请求 `/v3/api-docs` 失败，返回的不是标准 OpenAPI JSON。

可能的原因：

| 原因 | 解决 |
|------|------|
| 额外声明了 springdoc-openapi 依赖，版本与 Knife4j 内置冲突 | 删除冗余依赖，只用 knife4j starter |
| `@RestControllerAdvice` 异常被全局处理转成 Result 格式 | 加 `@Hidden` 排除扫描 |
| Security 未放行 `/v3/api-docs/**` | SecurityConfig 加 `.permitAll()` |
