# AOP 切面编程

## 解决什么问题

每个 Controller 方法都要写日志，传统做法是到处复制粘贴。哪天要改日志格式，所有 Controller 都得改一遍。

AOP 把这些横切关注点（日志、事务、权限）从业务代码里抽出来，集中管理。

## 核心概念

### Pointcut（切入点）

定义要拦截哪些方法。

```java
import org.aspectj.lang.annotation.Pointcut;

@Pointcut("execution(* com.example.springbootlearning.controller.*.*(..))")
public void controllerMethods() {}
```

表达式解读：
- 第一个 `*` — 任意返回值类型
- `controller.*` — controller 包下的任意类
- `.*(..)` — 任意方法，任意参数

### Advice（通知）

在切入点上做什么。五种类型：

| 注解 | 时机 | 用途 |
|------|------|------|
| `@Before` | 方法执行前 | 权限检查 |
| `@After` | 方法执行后（成功或异常都执行） | 资源释放 |
| `@AfterReturning` | 正常返回后 | 记录返回值 |
| `@AfterThrowing` | 抛异常后 | 异常日志 |
| `@Around` | 包裹方法执行 | 最强大，计时、事务都用它 |

### Aspect（切面）

= Pointcut + Advice 的组合。

```java
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect        // 声明这是一个切面
@Component     // 交给 Spring 管理（缺一不可）
public class ControllerLogAspect {

    @Pointcut("execution(* com.example..controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String id = Integer.toHexString(ThreadLocalRandom.current().nextInt(0x1000, 0x10000));
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        String user = currentUser();

        log.info("→ [{}] {} | {} | 参数: {}", id, user, method, args.toString());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("← [{}] {} | {} | 耗时: {}ms", id, user, method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            log.error("✕ [{}] {} | {} | 耗时: {}ms | 异常: {}", id, user, method, System.currentTimeMillis() - start, e.toString());
            throw e;  // 重新抛出，不吞异常
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return "-";
        return auth.getName();
    }
}
```

**日志效果：**
```
→ [a3f2] admin | UserController.list(..) | 参数: [1, 10]
← [a3f2] admin | UserController.list(..) | 耗时: 35ms
```

### 设计要点

- **随机 hex ID** — 4 位随机 hex，并发请求不会交叉分不清，不像递增序号那样随时间变大
- **不记返回值** — Page 对象 toString 太啰嗦，记核心信息就够
- **异常重新抛出** — catch 后 `throw e`，不吞异常，`@RestControllerAdvice` 正常兜底
- **匿名用户** — Spring Security 给没登录的人自动塞 `AnonymousAuthenticationToken`，需排除后显示 `-`

`@Around` 必须调 `joinPoint.proceed()` 并返回其结果，否则原方法不会执行。

## 底层原理：代理

Spring 不给你的类加代码，而是**在运行时创建一个代理对象**包在你外面：

```
调用方 → 代理对象 → 切面逻辑（日志）→ 你写的原方法
```

1. Spring 扫描到 `@Aspect`，找到匹配 Pointcut 的 Bean
2. 为这些 Bean 生成代理对象（CGLIB 子类），替换原 Bean
3. 调用方法时先经过代理 → 执行切面 → 再调原方法

没有实现接口的类用 CGLIB 子类代理，实现了接口的用 JDK 动态代理。

### 为什么 this 调用不触发 AOP

```java
public void methodA() {
    this.methodB();  // this 是原始对象，不是代理 → 不走 AOP
}
```

## 关键约束

- `@Aspect` + `@Component` 缺一不可
- `@Around` 必须调 `proceed()` 并返回结果
- AOP 只对 Spring Bean 生效，自己 `new` 的对象不走代理
- 同类的 `this` 方法互调不走 AOP
- `@Transactional` 本质上也是 AOP

## 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```
