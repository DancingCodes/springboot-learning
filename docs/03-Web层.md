# Web 层

## 接收请求的几种方式

### @GetMapping + @RequestParam

URL 问号传参：`GET /hello?name=张三`

```java
@GetMapping("/hello")
public String hello(@RequestParam(defaultValue = "World") String name) {
    return "Hello, " + name;
}
```

### @GetMapping + @PathVariable

URL 路径里取参数：`GET /hello/张三`

```java
@GetMapping("/hello/{name}")
public String hello(@PathVariable String name) {
    return "Hello, " + name;
}
```

### @PostMapping + @RequestBody

POST 请求，接收 JSON 数据：

```java
@PostMapping("/hello")
public String helloPost(@RequestBody HelloRequest request) {
    return "Hello, " + request.getName();
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
