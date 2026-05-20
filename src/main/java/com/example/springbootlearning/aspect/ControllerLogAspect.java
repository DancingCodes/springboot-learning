package com.example.springbootlearning.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Aspect
@Component
public class ControllerLogAspect {

    @Pointcut("execution(* com.example.springbootlearning.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String id = Integer.toHexString(ThreadLocalRandom.current().nextInt(0x1000, 0x10000));
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        String user = currentUser();

        log.info("→ [{}] {} | {} | 参数: {}", id, user, method, Arrays.toString(args));
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("← [{}] {} | {} | 耗时: {}ms", id, user, method, elapsed);
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("✕ [{}] {} | {} | 耗时: {}ms | 异常: {}", id, user, method, elapsed, e.toString());
            throw e;
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) return "-";
        return auth.getName();
    }
}
