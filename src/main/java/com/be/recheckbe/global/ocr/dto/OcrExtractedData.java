package com.be.recheckbe.global.ocr.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OcrExtractedData {

  private String storeName; // storeInfo.name.text
  private int paymentAmount; // totalPrice.price.text
  private String cardCompany; // paymentInfo.cardInfo.company.text (nullable)
  private String confirmNum; // paymentInfo.confirmNum.text (nullable)
}
