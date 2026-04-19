package com.be.recheckbe.domain.receipt.dto;

import com.be.recheckbe.global.ocr.dto.OcrExtractedData;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyzeReceiptResponse {

  private String storeName;
  private int paymentAmount;
  private String cardCompany;
  private String confirmNum;

  public static AnalyzeReceiptResponse from(OcrExtractedData ocrData) {
    return AnalyzeReceiptResponse.builder()
        .storeName(ocrData.getStoreName())
        .paymentAmount(ocrData.getPaymentAmount())
        .cardCompany(ocrData.getCardCompany())
        .confirmNum(ocrData.getConfirmNum())
        .build();
  }
}