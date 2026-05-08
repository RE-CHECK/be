package com.be.recheckbe.domain.auth.exception;

import com.be.recheckbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
  USERNAME_ALREADY_EXISTS("AUTH_4000", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
  LOGIN_FAIL("AUTH_4001", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
  INVALID_PASSWORD("AUTH_4002", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
  AUTHENTICATION_NOT_FOUND("AUTH_4003", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
  INVALID_AUTH_CONTEXT("AUTH_4004", "SecurityContext에 인증 정보가 없습니다.", HttpStatus.UNAUTHORIZED),

  PHONE_VERIFICATION_NOT_FOUND(
      "AUTH_4010", "인증 요청을 찾을 수 없습니다. 인증번호를 다시 요청해 주세요.", HttpStatus.BAD_REQUEST),
  PHONE_VERIFICATION_CODE_EXPIRED("AUTH_4011", "인증번호가 만료되었습니다.", HttpStatus.BAD_REQUEST),
  PHONE_VERIFICATION_CODE_MISMATCH("AUTH_4012", "인증번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
  PHONE_VERIFICATION_REQUIRED("AUTH_4013", "휴대폰 인증이 필요합니다.", HttpStatus.BAD_REQUEST),
  PHONE_VERIFICATION_TOKEN_EXPIRED(
      "AUTH_4014", "휴대폰 인증 유효시간이 만료되었습니다. 다시 인증해 주세요.", HttpStatus.BAD_REQUEST),
  SMS_SEND_FAIL("AUTH_5000", "문자 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.INTERNAL_SERVER_ERROR),

  JWT_TOKEN_EXPIRED("JWT_4001", "JWT 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
  UNSUPPORTED_TOKEN("JWT_4002", "지원되지 않는 JWT 형식입니다.", HttpStatus.UNAUTHORIZED),
  MALFORMED_JWT_TOKEN("JWT_4003", "JWT 형식이 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
  INVALID_SIGNATURE("JWT_4004", "JWT 서명이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
  ILLEGAL_ARGUMENT("JWT_4005", "JWT 토큰 값이 잘못되었습니다.", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
