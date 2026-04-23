package com.be.recheckbe.domain.receipt.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyRankingResponse {
  private int weekNumber;
  private List<CollegeDailyRankingResponse> rankings;
}
