package com.be.recheckbe.domain.auth.controller;

import com.be.recheckbe.domain.auth.dto.CheckUsernameRequest;
import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import com.be.recheckbe.domain.auth.service.AuthService;
import com.be.recheckbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/check-username")
    @Operation(summary = "아이디 중복확인")
    public BaseResponse<Void> checkUsername(@RequestBody @Valid CheckUsernameRequest request) {
        authService.checkUsername(request.getUsername());
        return BaseResponse.success(null);
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