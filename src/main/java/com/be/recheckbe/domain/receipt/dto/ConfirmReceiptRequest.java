package com.be.recheckbe.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmReceiptRequest {

  private String storeName;
  private int paymentAmount;
  private String cardCompany;
  private String confirmNum;
}
