package com.be.recheckbe.domain.admin.controller;

import com.be.recheckbe.domain.admin.dto.UserRegistrationStatsResponse;
import com.be.recheckbe.domain.admin.service.AdminReceiptService;
import com.be.recheckbe.domain.admin.service.AdminUserService;
import com.be.recheckbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 전용 API")
public class AdminController {

  private final AdminUserService adminUserService;
  private final AdminReceiptService adminReceiptService;

  @GetMapping("/users/stats")
  @Operation(summary = "가입자 수 통계 조회", description = "오늘 가입자 수와 누적 가입자 수를 조회합니다. (관리자 전용)")
  public BaseResponse<UserRegistrationStatsResponse> getUserRegistrationStats() {
    return BaseResponse.success(adminUserService.getUserRegistrationStats());
  }

  @GetMapping("/users/csv")
  @Operation(summary = "유저 가입 정보 CSV 다운로드", description = "가입일시, 유저 ID, 단과대, 학과 정보를 CSV로 다운로드합니다. (관리자 전용)")
  public void downloadUsersCsv(HttpServletResponse response) throws IOException {
    adminUserService.downloadUsersCsv(response);
  }

  @GetMapping("/receipts/csv")
  @Operation(summary = "단과대별 소비금액 CSV 다운로드", description = "일자별 단과대 소비금액 합계를 CSV로 다운로드합니다. (관리자 전용)")
  public void downloadCollegePaymentCsv(HttpServletResponse response) throws IOException {
    adminReceiptService.downloadCollegePaymentCsv(response);
  }
}