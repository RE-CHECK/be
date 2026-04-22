package com.be.recheckbe.domain.auth.controller;

import com.be.recheckbe.domain.auth.dto.LoginRequest;
import com.be.recheckbe.domain.auth.dto.LoginResponse;
import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import com.be.recheckbe.domain.auth.dto.RegisterResponse;
import com.be.recheckbe.domain.auth.service.AuthService;
import com.be.recheckbe.global.response.BaseResponse;
import com.be.recheckbe.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
@Validated
public class AuthController {

  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

  private final AuthService authService;

  @GetMapping("/check-username")
  @Operation(summary = "아이디 중복확인")
  public BaseResponse<Void> checkUsername(@RequestParam @NotBlank String username) {
    authService.checkUsername(username);
    return BaseResponse.success(null);
  }

  @PostMapping("/login")
  @Operation(summary = "로그인 (사용자, 관리자 모두 사용)")
  public BaseResponse<LoginResponse> login(
      @RequestBody @Valid LoginRequest request, HttpServletResponse response) {
    LoginResponse loginResponse = authService.login(request);
    setRefreshTokenCookie(response, loginResponse.getRefreshToken());
    return BaseResponse.success(loginResponse);
  }

  @PostMapping("/logout")
  @Operation(summary = "로그아웃")
  public BaseResponse<Void> logout(
      @AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
    authService.logout(userDetails.getId());
    clearRefreshTokenCookie(response);
    return BaseResponse.success(null);
  }

  @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "회원가입")
  public BaseResponse<RegisterResponse> register(
      @RequestPart("request") @Valid RegisterRequest request,
      @RequestPart("studentCardImage") MultipartFile studentCardImage) {
    return BaseResponse.success(authService.register(request, studentCardImage));
  }

  private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(60 * 60 * 24 * 14) // 14일
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private void clearRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/")
            .maxAge(0)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
