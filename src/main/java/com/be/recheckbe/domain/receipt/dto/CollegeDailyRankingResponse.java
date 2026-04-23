package com.be.recheckbe.domain.receipt.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollegeDailyRankingResponse {
  private int rank;
  private String collegeName;
  private List<DailyAmountResponse> dailyAmounts;
}
