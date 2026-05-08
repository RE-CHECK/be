package com.be.recheckbe.domain.admin.controller;

import com.be.recheckbe.domain.admin.dto.BanUserRequest;
import com.be.recheckbe.domain.admin.dto.UserRegistrationStatsResponse;
import com.be.recheckbe.domain.admin.service.AdminReceiptService;
import com.be.recheckbe.domain.admin.service.AdminUserService;
import com.be.recheckbe.domain.admin.service.BlacklistService;
import com.be.recheckbe.domain.popup.dto.PopupResponse;
import com.be.recheckbe.domain.popup.dto.UpdatePopupRequest;
import com.be.recheckbe.domain.popup.service.PopupService;
import com.be.recheckbe.domain.week.dto.CurrentWeekResponse;
import com.be.recheckbe.domain.week.service.WeekService;
import com.be.recheckbe.global.response.BaseResponse;
import com.be.recheckbe.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 전용 API")
public class AdminController {

  private final AdminUserService adminUserService;
  private final AdminReceiptService adminReceiptService;
  private final WeekService weekService;
  private final PopupService popupService;
  private final BlacklistService blacklistService;

  @GetMapping("/users/stats")
  @Operation(summary = "가입자 수 통계 조회", description = "오늘 가입자 수와 누적 가입자 수를 조회합니다. (관리자 전용)")
  public BaseResponse<UserRegistrationStatsResponse> getUserRegistrationStats() {
    return BaseResponse.success(adminUserService.getUserRegistrationStats());
  }

  @GetMapping("/users/csv")
  @Operation(
      summary = "유저 가입 정보 CSV 다운로드",
      description = "가입일시, 유저 ID, 단과대, 학과 정보를 CSV로 다운로드합니다. (관리자 전용)")
  public void downloadUsersCsv(HttpServletResponse response) throws IOException {
    adminUserService.downloadUsersCsv(response);
  }

  @GetMapping("/receipts/csv")
  @Operation(
      summary = "단과대별 소비금액 CSV 다운로드",
      description = "일자별 단과대 소비금액 합계를 CSV로 다운로드합니다. (관리자 전용)")
  public void downloadCollegePaymentCsv(HttpServletResponse response) throws IOException {
    adminReceiptService.downloadCollegePaymentCsv(response);
  }

  @GetMapping("/weeks/current")
  @Operation(summary = "현재 활성화 주차 조회", description = "현재 활성화된 주차를 조회합니다. null이면 테스트 기간입니다. (전체 공개)")
  public BaseResponse<CurrentWeekResponse> getCurrentWeek() {
    return BaseResponse.success(weekService.getCurrentWeek());
  }

  @PatchMapping("/weeks/{weekNumber}/activate")
  @Operation(
      summary = "주차 활성화",
      description = "특정 주차(1~3)를 활성화합니다. 기존 활성화 주차는 자동으로 교체됩니다. (관리자 전용)")
  public BaseResponse<CurrentWeekResponse> activateWeek(@PathVariable int weekNumber) {
    return BaseResponse.success(weekService.activateWeek(weekNumber));
  }

  @PatchMapping("/weeks/deactivate")
  @Operation(summary = "주차 비활성화", description = "활성화된 주차를 비활성화하여 테스트 기간으로 전환합니다. (관리자 전용)")
  public BaseResponse<CurrentWeekResponse> deactivateWeek() {
    return BaseResponse.success(weekService.deactivateWeek());
  }

  @PatchMapping("/popup")
  @Operation(summary = "팝업 등록/수정", description = "팝업 텍스트를 등록하거나 수정합니다. 등록 즉시 활성화됩니다. (관리자 전용)")
  public BaseResponse<PopupResponse> updatePopup(@RequestBody @Valid UpdatePopupRequest request) {
    return BaseResponse.success(popupService.updatePopup(request.getContent()));
  }

  @PatchMapping("/popup/deactivate")
  @Operation(summary = "팝업 비활성화", description = "현재 활성화된 팝업을 비활성화합니다. (관리자 전용)")
  public BaseResponse<PopupResponse> deactivatePopup() {
    return BaseResponse.success(popupService.deactivatePopup());
  }

  @PostMapping("/blacklist")
  @Operation(
      summary = "블랙리스트 등록",
      description = "전화번호를 블랙리스트에 등록합니다. 해당 번호로 가입된 계정이 있으면 함께 삭제됩니다. (관리자 전용)")
  public BaseResponse<Void> banUser(
      @RequestBody @Valid BanUserRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    blacklistService.banUser(request, userDetails.getId());
    return BaseResponse.success(null);
  }

  @PatchMapping("/blacklist/unban")
  @Operation(summary = "블랙리스트 해제", description = "전화번호로 블랙리스트를 해제하여 해당 번호로 재가입을 허용합니다. (관리자 전용)")
  public BaseResponse<Void> unbanUser(@RequestBody @Valid BanUserRequest request) {
    blacklistService.unbanBlacklist(request.getPhoneNumber());
    return BaseResponse.success(null);
  }

  @GetMapping("/blacklist/csv")
  @Operation(
      summary = "블랙리스트 CSV 다운로드",
      description = "차단 이력 전체를 CSV로 다운로드합니다. 차단 해제된 항목도 포함됩니다. (관리자 전용)")
  public void downloadBlacklistCsv(HttpServletResponse response) throws IOException {
    blacklistService.downloadBlacklistCsv(response);
  }
}
