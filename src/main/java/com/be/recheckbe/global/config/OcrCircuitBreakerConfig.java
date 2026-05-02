package com.be.recheckbe.global.config;

import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.ocr.exception.OcrErrorCode;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OcrCircuitBreakerConfig {

  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10) // 슬라이딩 윈도우 방식 : 최근 10개 요청 기준으로 판단
            .minimumNumberOfCalls(5) // 최소 요청 수 (치소 5번 호출되기 전까지는 판단 안함
            .failureRateThreshold(50) // 실패율이 50% 넘으면 OPEN
            .permittedNumberOfCallsInHalfOpenState(2) // HALF_OPEN에서 2개 요청만 허용함
            .waitDurationInOpenState(Duration.ofSeconds(60)) // OPEN 유지시간
            .automaticTransitionFromOpenToHalfOpenEnabled(true) // 자동 HALF_OPEN
            // 네트워크/연결 오류(OCR_REQUEST_FAILED)만 장애로 카운트
            .recordException(
                e ->
                    e instanceof CustomException ce
                        && ce.getErrorCode() == OcrErrorCode.OCR_REQUEST_FAILED)
            .build();

    return CircuitBreakerRegistry.of(config);
  }

  @Bean
  public CircuitBreaker ocrCircuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("ocr");
  }
}
