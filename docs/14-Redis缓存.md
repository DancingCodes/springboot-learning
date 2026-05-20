# Redis 缓存

## 为什么需要缓存

用户列表页每次点"查询"都：`COUNT(*)` + `SELECT ... LIMIT` + 查关联账户。并发上来后数据库压力大、响应慢加。

缓存思路：把查询结果存 Redis，下次来直接返回，跳过 SQL。

```
无缓存：  请求 → Controller → Service → SQL → DB
有缓存：  请求 → Controller → Service → Redis（命中）→ 直接返回
                                        ↓（未命中）
                                    SQL → DB → 存入 Redis → 返回
```

## 依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

`spring-boot-starter-data-redis` 包含 Spring Data Redis + Lettuce（Redis 客户端，替代老项目常用的 Jedis）。`commons-pool2` 提供连接池支持。

## Redis 连接配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:          # 生产环境加上
```

## Spring Cache — 注解驱动的缓存抽象

Spring Cache 是缓存抽象层。你只跟注解打交道，底层是 Redis 还是 Caffeine（本地缓存）由配置决定。换缓存实现不改业务代码。

### 三个核心注解

| 注解 | 行为 | 场景 |
|------|------|------|
| `@Cacheable` | **执行前**查缓存，命中就返回，未命中执行方法并存缓存 | 查询 |
| `@CacheEvict` | **执行后**删除缓存 | 增/改/删 |
| `@CachePut` | **始终执行**方法，更新缓存 | 少见 |

### 实战：UserService

```java
// 查询详情 —— 缓存 30 分钟
@Cacheable(value = "user", key = "#id", unless = "#result == null")
public UserVO getById(Long id) { ... }

// 分页查询 —— 按页码缓存
@Cacheable(value = "userPage", key = "#pageNum + '_' + #pageSize", unless = "#result == null")
public Page<UserVO> page(int pageNum, int pageSize) { ... }

// 新增 —— 清掉所有分页缓存（新数据影响所有页）
@CacheEvict(value = "userPage", allEntries = true)
@Transactional
public void add(User user) { ... }

// 修改 —— 清掉该用户的详情缓存 + 所有分页缓存
@Caching(evict = {
    @CacheEvict(value = "user", key = "#user.id"),
    @CacheEvict(value = "userPage", allEntries = true)
})
public void update(User user) { ... }

// 删除 —— 同上
@Caching(evict = {
    @CacheEvict(value = "user", key = "#id"),
    @CacheEvict(value = "userPage", allEntries = true)
})
public void delete(Long id) { ... }
```

**缓存命名空间**：用 `value` 区分不同缓存组。`user` 存单个用户，`userPage` 存分页结果。这样可以独立控制 TTL 和淘汰策略。

### `@Cacheable` 参数详解

| 参数 | 说明 | 示例 |
|------|------|------|
| `value` | 缓存名（命名空间） | `"user"` |
| `key` | 缓存键，SpEL 表达式 | `#id`、`#pageNum + '_' + #pageSize` |
| `unless` | 条件不缓存，SpEL | `"#result == null"` — 结果 null 时不缓存 |
| `condition` | 条件才缓存，SpEL | `"#id > 0"` — id 大于 0 才缓存 |

### `SpEL` 可用变量

| 变量 | 含义 |
|------|------|
| `#参数名` | 方法参数 |
| `#result` | 方法返回值（仅 unless / @CachePut） |
| `#root.method` | 方法对象 |

## CacheConfig — 启用缓存 + JSON 序列化

```java
@Configuration
@EnableCaching  // 激活 @Cacheable / @CacheEvict / @CachePut
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 默认 30 分钟过期
                .prefixCacheNameWith("springboot-learning::")  // 全局 key 前缀，隔离项目
                .serializeKeysWith(
                    SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                    SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
```

**为什么换 JSON 序列化**：Spring Boot 默认用 JDK 序列化（二进制），不可读、体积大。`GenericJackson2JsonRedisSerializer` 存成 JSON，用 `redis-cli` 能直接看懂：

```
127.0.0.1:6379> KEYS *
1) "springboot-learning::userPage::1_10"
2) "springboot-learning::user::1"
127.0.0.1:6379> GET "springboot-learning::user::1"
{"id":1,"name":"张三","email":"zhangsan@example.com",...,"@class":"com.example.dto.UserVO"}
```

`@class` 字段是 Jackson 反序列化时需要知道目标类型，Spring 自动处理。

**TTL（Time To Live）**：`entryTtl(Duration.ofMinutes(30))`。过期后 Redis 自动删除，下次查询走 SQL 重新生成。30 分钟对用户数据是合理的——改了数据 30 分钟内缓存生效，过了自动刷新。

## Redis 中的实际 key

Redis 默认 key 格式：`全局前缀::缓存名::key 值`。

```
springboot-learning::user::1          →  id=1 的用户详情
springboot-learning::user::2          →  id=2 的用户详情
springboot-learning::userPage::1_10   →  第 1 页，每页 10 条
springboot-learning::userPage::2_10   →  第 2 页，每页 10 条
```

**全局前缀**由 `prefixCacheNameWith("springboot-learning::")` 添加，确保多个项目共用同一 Redis 时 key 不冲突。

## 缓存更新策略

```
操作      →  缓存处理
getById   →  无则存（@Cacheable）
page      →  无则存（@Cacheable）
add       →  清分页缓存（新数据改变所有页）
update    →  清详情缓存 + 清分页缓存
delete    →  清详情缓存 + 清分页缓存
```

**为什么 add 不清详情缓存？** 新增前这个 id 不存在，缓存里也没有对应数据，没必要清。

**为什么 update/delete 要清分页缓存？** 改了名字/删了数据，分页里的记录可能变了，必须清掉下次重新查。

## 执行顺序：缓存注解 vs 事务注解

```
@CacheEvict(value = "userPage", allEntries = true)
@Transactional
public void add(User user) { ... }
```

缓存清在事务**之后**执行（`@CacheEvict` 默认 `beforeInvocation = false`）。如果事务回滚，缓存不会清——数据没变，缓存也无需清。这是对的。

## 常见坑

### 1. 默认缓存 null

`@Cacheable` 默认连 null 一起缓存。查不存在的用户 id，后续查同一个 id 永远返回 null。**必须加 `unless = "#result == null"`。**

### 2. 类内部调用不走代理

```java
// ❌ 在同一个 Service 里调自己的 @Cacheable 方法，缓存不生效
public void doSomething(Long id) {
    UserVO user = this.getById(id);  // 绕过了 Spring AOP 代理
}
```

Spring Cache 靠 AOP 代理生效。`this.getById()` 绕过了代理，注解无效。解决：注入自己（`@Lazy @Autowired`）或拆到另一个 Service。

### 3. Page 对象序列化问题

MyBatis-Plus 的 `Page` 对象序列化到 Redis 再反序列化时，内部字段（`orders`、`searchCount` 等）可能丢失或报错。现象是分页缓存反序列化失败。解法：缓存 `List<UserVO>` 而非 `Page<UserVO>`，或在 Controller 层包装 Page。

### 4. 缓存穿透

大量请求查不存在的 id → 缓存 null（或跳过缓存）→ 每次都打到数据库。解法：`unless = "#result == null"`（不缓存 null），或加布隆过滤器。

### 5. Spring Boot DevTools 导致类型错误

DevTools 用不同的 ClassLoader，反序列化时可能报 `ClassCastException`。如果遇到，临时关掉 DevTools 或加 `spring.devtools.restart.enabled=false`。
