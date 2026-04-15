package com.be.recheckbe.global.ocr.exception;

import com.be.recheckbe.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OcrErrorCode implements BaseErrorCode {
  OCR_REQUEST_FAILED("OCR5001", "OCR API 요청 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  OCR_INFER_FAILED("OCR4001", "영수증 인식에 실패했습니다. 이미지를 확인해주세요.", HttpStatus.BAD_REQUEST),
  OCR_PAYMENT_NOT_FOUND("OCR4002", "영수증에서 결제금액을 추출할 수 없습니다.", HttpStatus.BAD_REQUEST),
  OCR_IMAGE_ENCODE_FAILED("OCR5002", "이미지 인코딩 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
