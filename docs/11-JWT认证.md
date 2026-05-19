# JWT 无状态认证

## HTTP Basic 的问题

HTTP Basic 每次请求都带用户名密码的 Base64，相当于**把密码写在每封信上**。而且服务端没有"登出"的概念——客户端忘了关，别人就能一直用。

JWT 解决的是：**登录一次，拿一个有时效的通行证，后续请求带通行证就行，密码不再出门。**

## JWT 是什么

JSON Web Token，一串用 `.` 分隔的三段字符串：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.4GZx9yHyG...
↑                  ↑                    ↑
Header             Payload              Signature
```

| 段 | 内容 | 说明 |
|----|------|------|
| Header | `{"alg":"HS256"}` | 签名算法 |
| Payload | `{"sub":"admin","exp":1234567890}` | 用户名、过期时间等 |
| Signature | 对前两段的签名 | 用密钥算出，防止篡改 |

**关键**：任何人都能解开前两段看内容（Base64 不是加密），但签名的验证只有持有密钥的服务端能做。改一个字节，签名就对不上。

## 认证流程

```
1. 客户端                  2. 服务端
   POST /auth/login         验证密码 → 生成 JWT → 返回
   {username, password}  →  {"token":"eyJh..."}

3. 后续请求                4. JwtAuthFilter
   GET /user                提取 Authorization 头
   Authorization:           → 解析 JWT → 验证签名/过期
   Bearer eyJh...           → 设置 SecurityContext → 放行
```

对比 HTTP Basic：

| | HTTP Basic | JWT |
|---|---|---|
| 密码传输 | 每次请求都带 | 只在登录时带一次 |
| 过期 | 依赖客户端"忘记" | 内嵌到期时间 |
| 状态 | 无状态 | 无状态 |
| 存储 | 无 | 客户端存 token |

## 代码结构

```
config/
├── SecurityConfig.java   ← 声明 /auth/login 公开，其余需认证，无状态
├── JwtUtil.java          ← 生成 token、解析 token、验证
└── JwtAuthFilter.java    ← 每次请求拦截，从 Header 提取 JWT 并注入认证信息

controller/
└── AuthController.java   ← POST /auth/login，验证密码后返回 JWT

dto/
└── LoginRequest.java     ← {username, password}
```

## JwtUtil — 核心逻辑

```java
// 生成：把用户名写入 payload，设过期时间，用密钥签名
public String generateToken(String username) {
    return Jwts.builder()
            .subject(username)
            .expiration(new Date(now + 2小时))
            .signWith(密钥)
            .compact();
}

// 验证：解析 token，签名不对或过期则抛异常
public boolean validateToken(String token) {
    try {
        Jwts.parser().verifyWith(密钥).build().parseSignedClaims(token);
        return true;
    } catch (JwtException e) {
        return false;  // 签名被篡改 / token 已过期
    }
}
```

## JwtAuthFilter — 过滤器

继承 `OncePerRequestFilter`，保证每个请求只执行一次：

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    String authHeader = request.getHeader("Authorization");   // 1. 取 Header
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);  // 没 token，直接放行（Security 会拒）
        return;
    }
    String token = authHeader.substring(7);   // 2. 去掉 "Bearer "

    if (!jwtUtil.validateToken(token)) {      // 3. 验证
        filterChain.doFilter(request, response);
        return;
    }

    String username = jwtUtil.getUsernameFromToken(token);  // 4. 提取用户名
    // 5. 加载用户信息，注入 SecurityContext
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(...);
    SecurityContextHolder.getContext().setAuthentication(auth);

    filterChain.doFilter(request, response);  // 6. 放行
}
```

过滤器的**位置**：通过 `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` 放在用户名密码过滤器之前。这样 JWT 先行处理，有 token 就认证，没 token 才走后面的流程。

## SecurityConfig 的关键变化

| 配置项 | HTTP Basic 版 | JWT 版 | 原因 |
|--------|-------------|--------|------|
| httpBasic | `withDefaults()` | **删除** | 不再用 HTTP Basic |
| sessionManagement | 无 | `STATELESS` | 无状态，不创建 session |
| addFilterBefore | 无 | JwtAuthFilter | JWT 过滤器插到认证链前面 |
| permitAll | 无 | `/auth/login` | 登录接口必须公开 |

## 为什么设为 STATELESS

默认 Spring Security 会创建 HTTP Session 来存放认证信息。JWT 本身就是"自带状态"的凭证，不需要 Session。设为 `STATELESS` 后：

- 服务端完全不存任何用户状态
- 每个请求独立验证（token 里带的信息就够了）
- 水平扩展友好（每台服务器都能独立验证 token）

## 用 Postman 测试

**1. 登录拿 token：**
```
POST /auth/login
Body: {"username":"admin", "password":"admin123"}

Response: {"code":200, "msg":"成功", "data":{"token":"eyJh..."}}
```

**2. 用 token 访问接口：**
```
GET /user
Header: Authorization: Bearer eyJh...

Response: {"code":200, "msg":"成功", "data":{...}}
```

**3. 不带 token：**
```
GET /user
→ 403 Forbidden
```

**4. 密码错：**
```
POST /auth/login
Body: {"username":"admin", "password":"wrong"}
→ 403 Forbidden
```
