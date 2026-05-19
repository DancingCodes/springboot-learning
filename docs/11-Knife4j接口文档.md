# Knife4j 接口文档

## 和手写文档的区别

手写文档的问题是：加了接口要记得改文档，忘了改就对不上。Knife4j 从代码注解自动生成，注解和代码在一起，改了代码忘了改注解 IDE 会提示。

## 依赖

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

> Spring Boot 3.x 必须用带 `jakarta` 的 artifact。不要额外声明 `springdoc-openapi-starter-webmvc-ui`，Knife4j 已内置，重复声明会导致版本冲突。

## 配置类

```java
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("SpringBoot Learning API").version("1.0"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

配置 Bearer 安全方案后，Knife4j 页面右上角出现锁图标，填一次 token 所有接口调试自动带上。

## 核心注解

| 注解 | 位置 | 作用 |
|------|------|------|
| `@Tag(name = "用户管理")` | Controller 类 | 左侧导航分组 |
| `@Operation(summary = "分页查询")` | Controller 方法 | 接口说明 |
| `@Parameter(description = "页码")` | 方法参数 | 参数说明 |
| `@Schema(description = "用户")` | 实体类 / 字段 | 模型说明 |
| `@Hidden` | 类 / 方法 | 隐藏，不生成文档 |

## Security 放行

需要放行文档相关路径，否则文档页 403：

```java
.requestMatchers("/doc.html", "/v3/api-docs/**", "/webjars/**").permitAll()
```

| 路径 | 说明 |
|------|------|
| `/doc.html` | 文档页面 |
| `/v3/api-docs/**` | OpenAPI JSON 数据 |
| `/webjars/**` | JS/CSS 静态资源 |

## @Hidden 的作用

`@RestControllerAdvice` 类需要加 `@Hidden`，否则 Swagger 扫描时异常被全局处理成 Result 格式 JSON，Knife4j 收到非标准格式就报"文档请求异常"。

```java
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler { ... }
```

## 常见问题："文档请求异常"

| 原因 | 解决 |
|------|------|
| 额外声明了 springdoc-openapi 依赖，版本冲突 | 只用 knife4j starter |
| `@RestControllerAdvice` 影响扫描 | 加 `@Hidden` |
| Security 未放行 `/v3/api-docs/**` | 加 `.permitAll()` |
