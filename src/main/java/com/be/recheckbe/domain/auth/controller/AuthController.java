package com.be.recheckbe.domain.auth.controller;

import com.be.recheckbe.domain.auth.dto.LoginRequest;
import com.be.recheckbe.domain.auth.dto.LoginResponse;
import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import com.be.recheckbe.domain.auth.service.AuthService;
import com.be.recheckbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
@Validated
public class AuthController {

    private final AuthService authService;

    @GetMapping("/check-username")
    @Operation(summary = "아이디 중복확인")
    public BaseResponse<Void> checkUsername(@RequestParam @NotBlank String username) {
        authService.checkUsername(username);
        return BaseResponse.success(null);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 (사용자, 관리자 모두 사용)")
    public BaseResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return BaseResponse.success(authService.login(request));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입")
    public BaseResponse<Void> register(
            @RequestPart("request") @Valid RegisterRequest request,
            @RequestPart("studentCardImage") MultipartFile studentCardImage
    ) {
        authService.register(request, studentCardImage);
        return BaseResponse.success(null);
    }
}