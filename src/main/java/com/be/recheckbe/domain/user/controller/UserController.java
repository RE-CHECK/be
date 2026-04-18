package com.be.recheckbe.domain.user.controller;

import com.be.recheckbe.domain.user.dto.UserDashboardResponse;
import com.be.recheckbe.domain.user.service.UserService;
import com.be.recheckbe.global.response.BaseResponse;
import com.be.recheckbe.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 API")
public class UserController {

  private final UserService userService;

  @GetMapping("/me/dashboard")
  @Operation(summary = "메인 대시보드 조회", description = "로그인한 사용자의 이름, 단과대, 누적 영수증 결제금액을 조회합니다.")
  public BaseResponse<UserDashboardResponse> getDashboard(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseResponse.success(userService.getDashboard(userDetails.getId()));
  }
}
