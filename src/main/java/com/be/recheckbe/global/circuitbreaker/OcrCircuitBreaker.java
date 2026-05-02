package com.be.recheckbe.global.circuitbreaker;

import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.ocr.exception.OcrErrorCode;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OcrCircuitBreaker {

  private enum State {
    CLOSED, // 요청 자유롭게 통과
    OPEN, // 장애 반복 시 요청 차단(네트워크/연결 오류) -> 즉시 차단(빠르게 실패 반환)
    HALF_OPEN // 일정 시간 후 복구 시도 (일부 요청만 허용하여 테스트)
  }

  private static final int FAILURE_THRESHOLD = 5; // 5번 연속 실패시 OPEN 전환
  private static final int HALF_OPEN_PROBE_COUNT = 2; // HALF_OPEN에서 허용할 최대 탐침 요청 수
  private static final int HALF_OPEN_SUCCESS_THRESHOLD = 2; // HALF_OPEN 상태에서 2번 성공하면 CLOSED로 복구
  private static final long WAIT_DURATION_MS = 60_000L; // OPEN 상태 유지 시간 (60초)

  // Atomic 사용이유 : 멀티 스레드 환경에서 안전하게 상태 변경하기 위함 (동시성을 보장)
  private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED); // 현재 상태
  private final AtomicInteger failureCount = new AtomicInteger(0); // 연속 실패 횟수
  private final AtomicInteger halfOpenSuccessCount = new AtomicInteger(0); // HALF_OPEN에서 성공 횟수
  private final AtomicInteger halfOpenPermits = new AtomicInteger(0); // HALF_OPEN에서 남은 탐침 슬롯
  private final AtomicLong openedAt = new AtomicLong(0); // OPEN 상태 시작 시간

  public <T> T execute(Supplier<T> supplier) {
    if (!tryAcquirePermission()) {
      log.warn("[OCR Circuit] OPEN - 요청 차단");
      throw new CustomException(OcrErrorCode.OCR_CIRCUIT_OPEN);
    }

    try {
      T result = supplier.get();
      recordSuccess();
      return result;
    } catch (CustomException e) {
      // 네트워크/연결 실패만 장애로 카운트 (OCR 인식 실패 등 비즈니스 오류는 제외)
      if (e.getErrorCode() == OcrErrorCode.OCR_REQUEST_FAILED) {
        recordFailure();
      }
      throw e;
    }
  }

  // 요청 가능 여부 체크
  private boolean tryAcquirePermission() {
    State current = state.get();
    if (current == State.CLOSED) return true;
    if (current == State.HALF_OPEN) {
      // 남은 탐침 슬롯을 원자적으로 소비 — 0 이하면 차단
      return halfOpenPermits.getAndDecrement() > 0;
    }
    // OPEN 상태: 대기 시간이 지났으면 HALF_OPEN으로 전환 시도
    if (System.currentTimeMillis() - openedAt.get() >= WAIT_DURATION_MS) {
      if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
        halfOpenSuccessCount.set(0);
        halfOpenPermits.set(HALF_OPEN_PROBE_COUNT); // 탐침 슬롯 초기화
        log.info("[OCR Circuit] OPEN → HALF_OPEN");
      }
      // CAS 성공 여부와 무관하게 슬롯 소비 시도
      return halfOpenPermits.getAndDecrement() > 0;
    }
    return false;
  }

  private void recordSuccess() {
    if (state.get() == State.HALF_OPEN) {
      if (halfOpenSuccessCount.incrementAndGet() >= HALF_OPEN_SUCCESS_THRESHOLD) {
        state.compareAndSet(State.HALF_OPEN, State.CLOSED);
        failureCount.set(0);
        log.info("[OCR Circuit] HALF_OPEN → CLOSED (복구 완료)");
      }
    } else {
      failureCount.set(0);
    }
  }

  private void recordFailure() {
    if (state.get() == State.HALF_OPEN) {
      state.compareAndSet(State.HALF_OPEN, State.OPEN);
      openedAt.set(System.currentTimeMillis());
      log.warn("[OCR Circuit] HALF_OPEN → OPEN (복구 실패)");
    } else {
      int count = failureCount.incrementAndGet();
      if (count >= FAILURE_THRESHOLD) {
        if (state.compareAndSet(State.CLOSED, State.OPEN)) {
          openedAt.set(System.currentTimeMillis());
          log.warn("[OCR Circuit] CLOSED → OPEN (연속 실패 {}회)", count);
        }
      }
    }
  }
}