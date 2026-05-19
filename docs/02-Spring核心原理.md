# Spring 核心原理

## IoC（控制反转）

**传统写法**：你自己 `new` 对象，你掌控创建。

```java
GreetingService service = new GreetingService();  // 你主动 new
```

**Spring 写法**：你只声明需要什么，Spring 帮你创建好送过来。

```java
@Autowired
private GreetingService greetingService;  // 你不动手，Spring 帮你搞定
```

- `@Service` → 对象交给 Spring 管（变成 Bean）
- `@Autowired` → 从 Spring 手里拿回它管好的对象

## Bean

就是 Spring 容器管理的对象。本质上就是对象，换个名字。

加了 `@Service` / `@Component` / `@Repository` / `@Controller` 等注解，Spring 扫描到后帮你 `new` 好并保管起来 —— 这就叫"注册了一个 Bean"。

## DI（依赖注入）

Spring 发现 `HelloController` 需要 `GreetingService`，自动在容器里找到对应的 Bean 并赋值到字段上。这就是"注入"。

## `@SpringBootApplication` 启动时做了什么

```
1. 扫描（@ComponentScan）→ 找到所有 @Service/@Controller 等
2. 实例化 → new 出对象放入容器
3. 注入（@Autowired）→ 把需要的 Bean 塞到对应字段
```

## 一个请求的完整路径

```
浏览器: GET /hello?name=张三

1. Tomcat 收到请求
2. @GetMapping("/hello") 匹配到 HelloController.hello()
3. @RequestParam 提取 name="张三"
4. 调用 greetingService.greet("张三")
5. 返回 "Hello, 张三!"
6. @RestController 把字符串直接写入 Response Body
7. 浏览器显示结果
```
