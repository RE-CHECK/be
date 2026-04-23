package com.be.recheckbe.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyAmountResponse {
  private String day;
  private int amount;
}