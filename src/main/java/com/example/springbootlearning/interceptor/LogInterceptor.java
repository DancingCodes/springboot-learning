package com.example.springbootlearning.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        log.info("[preHandle] {} {} — handler: {}", request.getMethod(), request.getRequestURI(), handler);
        return true; // true = 放行
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        log.info("[postHandle] 状态码: {}", response.getStatus());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        long startTime = (long) request.getAttribute("startTime");
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[afterCompletion] 耗时: {}ms", elapsed);
        if (ex != null) {
            log.error("[afterCompletion] 异常: {}", ex.getMessage());
        }
    }
}
