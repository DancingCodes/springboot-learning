# Spring 核心原理

## IoC（控制反转）

**传统写法**：你需要什么，自己 `new`。

```java
import com.example.Calculator;

public class Main {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();  // 自己 new
    }
}
```

**Spring 写法**：你只声明要什么，Spring 帮你创建好送过来。

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component  // 交给 Spring 管理（成为 Bean），里面的 @Autowired 才会生效
public class AppRunner {

    @Autowired  // 从容器中注入 Calculator 的实例
    private Calculator calculator;  // 不用自己 new
}
```

控制权从"你自己 new"反转到"Spring 帮你造"——这就是**控制反转**。

## Bean

加了 `@Component`（或 `@Service`、`@Controller`、`@Repository`、`@Bean` 等）的类，Spring 启动时会扫描到，`new` 好之后放进容器里保管。这个被 Spring 管起来的对象就叫 **Bean**。

```java
import org.springframework.stereotype.Component;

@Component  // Spring 扫描到 → new Calculator() → 放入容器 → 这就是一个 Bean
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

本质上就是对象，换个名字——被 Spring 管的叫 Bean，没被管的叫普通对象。

## DI（依赖注入）

DI 是 IoC 的实现方式。看一个完整例子：

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

@Component
public class MathService {

    @Autowired
    private Calculator calculator;

    public int square(int x) {
        return calculator.add(x, 0);  // 使用注入进来的 Bean
    }
}
```

Spring 发现 `MathService` 需要 `Calculator`，自动在容器里找到对应的 Bean 并注入到字段上。这就是"注入"。

注入流程：

```
1. 扫描到 Calculator（@Component）→ 实例化 → 放入容器
2. 扫描到 MathService（@Component），发现字段标注了 @Autowired
   → 从容器找到 Calculator Bean → 注入 → MathService 实例化完成
```

你只声明了依赖关系（`@Autowired`），所有 `new` 和赋值都由 Spring 完成。

## `@SpringBootApplication` 启动时做了什么

```
1. @ComponentScan → 扫描启动类所在包及子包下所有带注解的类
2. 实例化 → 按依赖顺序把 Bean 一个个 new 出来放入容器
3. 注入 → 把每个 Bean 需要的依赖从容器中找出来塞进去
```