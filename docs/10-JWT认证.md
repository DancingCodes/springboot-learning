# JWT 无状态认证

## HTTP Basic 的问题

每次请求都带密码的 Base64，相当于每封信上写密码。而且没有"登出"概念，密码暴露时间太长。

JWT：登录一次拿通行证，后续带通行证，密码不再出门。

## JWT 是什么

一串用 `.` 分隔的三段 Base64 字符串：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.4GZx9yHyG...
↑                  ↑                    ↑
Header             Payload              Signature
```

| 段 | 内容 | 说明 |
|----|------|------|
| Header | `{"alg":"HS256"}` | 签名算法 |
| Payload | `{"sub":"admin","exp":...}` | 用户名、过期时间等 |
| Signature | 前两段的签名 | 用密钥算出，防篡改 |

前两段只是 Base64，任何人都能解码看内容。但签名的验证只有持有密钥的服务端能做——改一个字节签名就对不上。

## 认证流程

```
1. POST /auth/login {username, password}
       → 验证密码 → 返回 {"token":"eyJh..."}

2. 后续请求带 Authorization: Bearer eyJh...
       → JwtAuthFilter 解析 → 验证签名/过期 → 设置 SecurityContext → 放行
```

对比 HTTP Basic：

| | HTTP Basic | JWT |
|---|---|---|
| 密码传输 | 每次请求都带 | 只在登录时带一次 |
| 过期 | 依赖客户端"忘记" | 内嵌到期时间 |
| 服务端状态 | 无状态 | 无状态 |

## JwtUtil — 核心工具

```java
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private final SecretKey key = Keys.hmacShaKeyFor(
        "your-256-bit-secret-key-minimum-32-bytes!".getBytes()
    );

    // 生成 token：写入用户名 + 2 小时过期 + 签名
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + 2 * 3600_000))
                .signWith(key)
                .compact();
    }

    // 验证 token：签名被篡改或过期则返回 false
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 从 token 中提取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload().getSubject();
    }
}
```

## JwtAuthFilter — 过滤器

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 没有 token → 直接放行，后面的 Security 会拒绝
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);  // 去掉 "Bearer "

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取用户名 → 加载用户 → 注入 SecurityContext
        String username = jwtUtil.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
```

通过 `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` 插到认证链前面。

## SecurityConfig 关键变化

```java
import org.springframework.security.config.http.SessionCreationPolicy;

http
    .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 无状态
    )
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/login").permitAll()
        .anyRequest().authenticated()
    );
```

JWT 自带状态，不需要服务端 Session。`STATELESS` 后每个请求独立验证，水平扩展友好。
