package com.be.recheckbe.domain.admin.controller;

import com.be.recheckbe.domain.admin.dto.UserRegistrationStatsResponse;
import com.be.recheckbe.domain.admin.service.AdminUserService;
import com.be.recheckbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User", description = "관리자 유저 관리 API")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping("/stats")
  @Operation(summary = "가입자 수 통계 조회", description = "오늘 가입자 수와 누적 가입자 수를 조회합니다. (관리자 전용)")
  public BaseResponse<UserRegistrationStatsResponse> getUserRegistrationStats() {
    return BaseResponse.success(adminUserService.getUserRegistrationStats());
  }
}