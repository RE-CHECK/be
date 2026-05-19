package com.be.recheckbe.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpecialMatchRankingResponse {

  private int rank;
  private String studentYear;
  private int totalPaymentAmount;
}