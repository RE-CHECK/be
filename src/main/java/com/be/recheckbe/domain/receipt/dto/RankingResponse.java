package com.be.recheckbe.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankingResponse {

  private int rank;
  private String collegeName;
  private int totalPaymentAmount;
}