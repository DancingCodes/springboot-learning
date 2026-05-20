# Web 层

## 接收请求的几种方式

### @GetMapping + @RequestParam

URL 问号传参：`GET /hello?name=张三`

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return "Hello, " + name;
    }
}
```

### @GetMapping + @PathVariable

URL 路径里取参数：`GET /hello/张三`

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Hello, " + name;
    }
}
```

### @PostMapping + @RequestBody

POST 请求，接收 JSON 数据：

```java
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @PostMapping("/hello")
    public String hello(@RequestBody HelloRequest request) {
        return "Hello, " + request.getName();
    }
}
```

需要建一个 DTO 类（数据传输对象）：

```java
public class HelloRequest {
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

## 序列化 / 反序列化

**序列化**：Java 对象 → JSON 字符串（返回给客户端时）

```java
HelloRequest obj = new HelloRequest();
obj.setName("张三");
// 序列化后 → {"name":"张三"}
```

**反序列化**：JSON 字符串 → Java 对象（接收客户端数据时）

```java
// 客户端发来 JSON：{"name":"张三"}
// Jackson 底层相当于：
HelloRequest obj = new HelloRequest();
obj.setName("张三");     // ← 通过反射调用 setter，IDEA 显示"未使用"是正常的
```

Spring Boot 内置的 Jackson 库会自动完成这两步转换，不需要手动调任何序列化/反序列化方法。

## 参数校验（@Valid）

在 `@RequestBody` 参数前加 `@Valid`，Spring 会自动校验请求体中的字段：

```java
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @PostMapping("/user")
    public String add(@Valid @RequestBody User user) {
        // 校验通过才会执行到这里
        return "新增成功";
    }
}
```

校验规则写在实体类或 DTO 的字段上：

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class User {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄超出合理范围")
    private Integer age;

    // getter / setter 省略
}
```

**执行流程**：

```
请求 → @Valid 触发校验 → 字段不合法 → 抛出 MethodArgumentNotValidException
     → @RestControllerAdvice 拦截 → 返回 400 + 错误详情 JSON
```

校验通过才会进入方法体。校验注解来自 Jakarta Validation：
- `@NotBlank` — 字符串不能为 null 或空串
- `@Email` — 必须是合法邮箱格式
- `@Min` / `@Max` — 整数范围
- `@NotNull` — 不能为 null
- `@DecimalMin` — BigDecimal 最小值

**注意**：Spring Boot 3.x 的包是 `jakarta.validation.constraints.*`，不是老的 `javax.validation.*`（Jakarta EE 9 改名）。

### @Valid 校验的对象：DTO 而不是 Entity

上面的示例直接在 Entity（`User`）上加校验注解，学习阶段可以，但实际项目推荐用 **入参 DTO**：

```java
// ❌ 不推荐：直接暴露 Entity，前端能传 id、createTime 等后端控制的字段
@PostMapping("/user")
public Result<User> add(@Valid @RequestBody User user) { ... }

// ✅ 推荐：用 DTO 只暴露该填的字段
@PostMapping("/user")
public Result<UserVO> add(@Valid @RequestBody UserSaveDTO request) {
    // Controller 里做 DTO → Entity 转换
    User user = new User();
    user.setName(request.getName());
    // ...
}
```

| 方式 | 问题 |
|------|------|
| 直接收 Entity | 前端能传 `id`、`createTime` 等不该控制的字段 |
| 收 DTO | 只暴露 `name`、`email`、`age`，安全可控 |

> 详见 `04-数据层与事务.md` 的"VO 与 DTO"章节。
