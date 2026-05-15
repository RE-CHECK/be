package com.be.recheckbe.global.config;

import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.ocr.exception.OcrErrorCode;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OcrResilienceConfig {

  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(50)
            .permittedNumberOfCallsInHalfOpenState(2)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordException(
                e ->
                    e instanceof CustomException ce
                        && ce.getErrorCode() == OcrErrorCode.OCR_REQUEST_FAILED)
            .build();

    return CircuitBreakerRegistry.of(config);
  }

  @Bean
  public CircuitBreaker ocrCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
    return circuitBreakerRegistry.circuitBreaker("ocr");
  }

  @Bean
  public BulkheadRegistry bulkheadRegistry() {
    BulkheadConfig config =
        BulkheadConfig.custom()
            .maxConcurrentCalls(3) // 스레드 풀 내에서 해당 요청을 동시에 3개의 슬롯만 허용
            .maxWaitDuration(Duration.ZERO) // 대기 시간 0초 -> fail fast
            .build();

    return BulkheadRegistry.of(config);
  }

  @Bean
  public Bulkhead ocrBulkhead(BulkheadRegistry bulkheadRegistry) {
    return bulkheadRegistry.bulkhead("ocr");
  }
}
