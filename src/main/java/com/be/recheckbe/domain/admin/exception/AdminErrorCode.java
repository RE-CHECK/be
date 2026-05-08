package com.be.recheckbe.domain.admin.exception;

import com.be.recheckbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {
  ALREADY_BLACKLISTED("ADMIN_4090", "이미 블랙리스트에 등록된 전화번호입니다.", HttpStatus.CONFLICT),
  BLACKLIST_NOT_FOUND("ADMIN_4040", "블랙리스트 항목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
