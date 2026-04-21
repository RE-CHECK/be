package com.be.recheckbe.domain.auth.service;

import com.be.recheckbe.domain.auth.dto.LoginRequest;
import com.be.recheckbe.domain.auth.dto.LoginResponse;
import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import com.be.recheckbe.domain.auth.dto.RegisterResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

  void checkUsername(String username);

  RegisterResponse register(RegisterRequest request, MultipartFile studentCardImage);

  LoginResponse login(LoginRequest request);

  void logout(Long userId);
}
