package com.be.recheckbe.domain.week.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CurrentWeekResponse {

  private Integer weekNumber; // null = 테스트 기간, 1~3 = 활성화된 주차
}