package com.be.recheckbe.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "UserRegistrationStatsResponse DTO", description = "가입자 수 통계 응답")
public class UserRegistrationStatsResponse {

  @Schema(description = "오늘 가입자 수", example = "5")
  private long todayCount;

  @Schema(description = "누적 가입자 수", example = "120")
  private long totalCount;
}