package com.be.recheckbe.domain.receipt.exception;

import com.be.recheckbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReceiptErrorCode implements BaseErrorCode {
  NOT_SUPPORT_CARD_COMPANY("RECEIPT4001", "지원하지 않는 카드사 입니다.", HttpStatus.BAD_REQUEST),
  DUPLICATE_RECEIPT("RECEIPT4002", "이미 등록된 영수증입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
