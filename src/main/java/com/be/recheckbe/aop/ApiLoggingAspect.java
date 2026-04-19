package com.be.recheckbe.aop;

import com.be.recheckbe.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class ApiLoggingAspect {

  @Around("execution(* com.be.recheckbe.domain.*.controller.*.*(..))")
  public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = currentRequest();
    String method = request != null ? request.getMethod() : "UNKNOWN";
    String uri = request != null ? request.getRequestURI() : "UNKNOWN";
    String caller = resolveCallerIdentity();

    long start = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      long elapsed = System.currentTimeMillis() - start;
      log.info("[{}] {} | {}ms | {}", method, uri, elapsed, caller);
      return result;
    } catch (Exception e) {
      long elapsed = System.currentTimeMillis() - start;
      String code = (e instanceof CustomException ce) ? ce.getErrorCode().getCode() : "UNKNOWN";
      log.error("[{}] {} | {}ms | {} | [{}] {}", method, uri, elapsed, caller, code, e.getMessage());
      throw e;
    }
  }

  private HttpServletRequest currentRequest() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attrs != null ? attrs.getRequest() : null;
  }

  private String resolveCallerIdentity() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
      return "anonymous";
    }
    return "userId=" + auth.getName();
  }
}