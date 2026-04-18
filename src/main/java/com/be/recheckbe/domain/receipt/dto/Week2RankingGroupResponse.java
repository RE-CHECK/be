package com.be.recheckbe.domain.receipt.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Week2RankingGroupResponse {

  private String storeName;
  private List<RankingResponse> rankings;
}