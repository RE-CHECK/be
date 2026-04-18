package com.be.recheckbe.domain.week.exception;

import com.be.recheckbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WeekErrorCode implements BaseErrorCode {
  INVALID_WEEK_NUMBER("WEEK4001", "유효하지 않은 주차입니다. 1~3 사이의 값을 입력해주세요.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}