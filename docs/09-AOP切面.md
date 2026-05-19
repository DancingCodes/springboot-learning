# AOP 切面编程

## 解决什么问题

Controller 里每个方法都要写日志，做法是复制粘贴？如果有一天要改日志格式或者关掉日志，得把所有 Controller 改一遍。

AOP（Aspect-Oriented Programming，面向切面编程）把这类**横切关注点**（日志、事务、权限）从业务代码里抽出来，放到一个地方统一管理。

## 核心概念

### JoinPoint（连接点）

程序执行过程中可以插入切面的点。Spring AOP 只支持**方法执行**这一个连接点（方法调用前、后、环绕、抛异常时）。

### Pointcut（切入点）

一个**匹配规则**，定义切面要拦截哪些方法。

```java
@Pointcut("execution(* com.example.springbootlearning.controller.*.*(..))")
public void controllerMethods() {}
```

这条表达式的含义：

```
execution(* com.example.springbootlearning.controller.*.*(..))
          ↑                                    ↑ ↑  ↑
      返回值（任意）                            类 方法 参数（任意）
```

- 第一个 `*` — 任意返回值类型
- `controller.*` — controller 包下的任意类
- `.*(..)` — 任意方法，任意参数

### Advice（通知）

在切入点**做什么**。五种类型：

| 注解 | 时机 | 用途 |
|------|------|------|
| `@Before` | 方法执行前 | 权限检查 |
| `@After` | 方法执行后（不管成功还是抛异常） | 资源释放 |
| `@AfterReturning` | 方法正常返回后 | 记录返回值 |
| `@AfterThrowing` | 方法抛异常后 | 异常日志 |
| `@Around` | 包裹方法执行 | **最强大，计时、事务都用它** |

`@Around` 可以控制方法是否执行、拿到参数和返回值、测量执行时间——其他四种都是 `@Around` 的简化版。

### Aspect（切面）

= Pointcut + Advice 的组合。一个类就是一个切面。

```java
@Slf4j
@Aspect          // ← 声明这是一个切面
@Component       // ← 交给 Spring 管理
public class ControllerLogAspect {

    @Pointcut("execution(* com.example..controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 方法执行前
        log.info("→ {} | 参数: {}", method, args);

        // 2. 执行原始方法
        Object result = joinPoint.proceed();

        // 3. 方法执行后
        log.info("← {} | 耗时: {}ms", method, elapsed);

        return result;
    }
}
```

`joinPoint.proceed()` 就是调用原始方法。不调这句，原始方法就不会执行。

## 启动后效果

每次访问接口，控制台输出：

```
→ UserController.list(..) | 参数: [1, 10]
← UserController.list(..) | 耗时: 15ms
→ AccountController.transfer(..) | 参数: [TransferRequest(fromId=1, toId=2, amount=100.00)]
← AccountController.transfer(..) | 耗时: 32ms
```

## Spring AOP 的底层原理：代理

Spring AOP 不是直接改你的 `.class` 文件（那是 AspectJ 的做法），而是**在运行时创建代理对象**。

```
客户端 → Controller 代理 → 切面逻辑（日志）→ 真正的 Controller 方法
```

流程：
1. Spring 启动时扫描到 `@Aspect`
2. 找到匹配 Pointcut 的 Bean
3. 为这些 Bean **生成代理对象**（CGLIB 子类），替换原 Bean
4. 调用方法时，先经过代理 → 执行切面逻辑 → 再调原方法

因为 Controller 没有实现接口，Spring 用 **CGLIB** 生成子类代理（通过继承）。如果类实现了接口，则用 **JDK 动态代理**。

### 为什么 this 调用不触发 AOP

```java
// 在同一个类里
public void methodA() {
    this.methodB();  // ← 不触发 AOP！
}
```

因为 `this` 指向的是**原始对象**，不是代理对象。走代理才能触发切面。

## 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## 常见用途

| 场景 | 切面 |
|------|------|
| 接口日志 | `@Around` + 参数 + 耗时 |
| 权限校验 | `@Before` + 自定义注解 |
| 事务管理 | `@Around`（`@Transactional` 本身就是 AOP 实现的） |
| 异常统一处理 | `@AfterThrowing`（`@RestControllerAdvice` 本质上也是 AOP） |
| 缓存 | `@Around`（查缓存命中则直接返回，不调方法） |

## 关键点

- `@Aspect` + `@Component` 两个注解缺一不可
- `@Around` 必须调用 `joinPoint.proceed()` 并返回结果，否则方法不执行
- AOP 只对 Spring 管理的 Bean 生效，自己 `new` 的对象不走代理
- 同一个类内方法互调不走 AOP（`this` 绕过了代理）
