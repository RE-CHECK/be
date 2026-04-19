package com.be.recheckbe.aop;

import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import com.be.recheckbe.domain.receipt.dto.UploadReceiptResponse;
import com.be.recheckbe.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

  private static final long SLOW_THRESHOLD_MS = 1000;

  /** 회원가입 감사 로그 — 항상 INFO */
  @Around("execution(* com.be.recheckbe.domain.auth.service.AuthServiceImpl.register(..))")
  public Object logRegister(ProceedingJoinPoint joinPoint) throws Throwable {
    RegisterRequest request = extractArg(joinPoint, RegisterRequest.class);
    String username = request != null ? request.getUsername() : "unknown";
    Integer studentNumber = request != null ? request.getStudentNumber() : null;

    long start = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      long elapsed = System.currentTimeMillis() - start;
      log.info("[회원가입] username={} | studentNumber={} | {}ms", username, studentNumber, elapsed);
      return result;
    } catch (Exception e) {
      long elapsed = System.currentTimeMillis() - start;
      log.error(
          "[회원가입 실패] username={} | studentNumber={} | {}ms | [{}] {}",
          username,
          studentNumber,
          elapsed,
          errorCode(e),
          e.getMessage());
      throw e;
    }
  }

  /** 영수증 업로드 감사 로그 — 항상 INFO */
  @Around(
      "execution(* com.be.recheckbe.domain.receipt.service.ReceiptServiceImpl.uploadReceiptImage(..))")
  public Object logReceiptUpload(ProceedingJoinPoint joinPoint) throws Throwable {
    Long userId = extractArg(joinPoint, Long.class);

    long start = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      long elapsed = System.currentTimeMillis() - start;
      if (result instanceof UploadReceiptResponse res) {
        log.info(
            "[영수증 업로드] userId={} | store={} | amount={} | cardCompany={} | {}ms",
            userId,
            res.getStoreName(),
            res.getPaymentAmount(),
            res.getCardCompany(),
            elapsed);
      }
      return result;
    } catch (Exception e) {
      long elapsed = System.currentTimeMillis() - start;
      log.error(
          "[영수증 업로드 실패] userId={} | {}ms | [{}] {}",
          userId,
          elapsed,
          errorCode(e),
          e.getMessage());
      throw e;
    }
  }

  /** 전체 서비스 레이어 성능 모니터링 — 임계값 초과 시에만 WARN */
  @Around(
      "execution(* com.be.recheckbe.domain.*.service.*ServiceImpl.*(..))"
          + " && !execution(* com.be.recheckbe.domain.auth.service.AuthServiceImpl.register(..))"
          + " && !execution(* com.be.recheckbe.domain.receipt.service.ReceiptServiceImpl.uploadReceiptImage(..))")
  public Object logSlowService(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    try {
      return joinPoint.proceed();
    } finally {
      long elapsed = System.currentTimeMillis() - start;
      if (elapsed > SLOW_THRESHOLD_MS) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.warn(
            "[SLOW] {}.{}() | {}ms (임계값 {}ms 초과)",
            className,
            methodName,
            elapsed,
            SLOW_THRESHOLD_MS);
      }
    }
  }

  private String errorCode(Exception e) {
    if (e instanceof CustomException ce) {
      return ce.getErrorCode().getCode();
    }
    return "UNKNOWN";
  }

  @SuppressWarnings("unchecked")
  private <T> T extractArg(ProceedingJoinPoint joinPoint, Class<T> type) {
    for (Object arg : joinPoint.getArgs()) {
      if (type.isInstance(arg)) {
        return (T) arg;
      }
    }
    return null;
  }
}