package com.dazzle.asklepios.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CacheLoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final CacheManager cacheManager;

    public CacheLoggingAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object logCacheableMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String cacheName = joinPoint.getTarget().getClass().getSimpleName() + "." + methodName;

        Cache cache = cacheManager.getCache("facilities");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get("all");
            if (wrapper != null) {
                log.info("Cache hit for '{}'", cacheName);
            } else {
                log.info("Cache miss for '{}', loading from DB...", cacheName);
            }
        }

        return joinPoint.proceed();
    }
}

