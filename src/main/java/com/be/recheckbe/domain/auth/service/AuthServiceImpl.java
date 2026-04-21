package com.be.recheckbe.domain.auth.service;

import com.be.recheckbe.domain.auth.dto.LoginRequest;
import com.be.recheckbe.domain.auth.dto.LoginResponse;
import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import com.be.recheckbe.domain.auth.dto.RegisterResponse;
import com.be.recheckbe.domain.auth.exception.AuthErrorCode;
import com.be.recheckbe.domain.department.entity.Department;
import com.be.recheckbe.domain.department.repository.DepartmentRepository;
import com.be.recheckbe.domain.user.entity.Role;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
import com.be.recheckbe.global.jwt.JwtProvider;
import com.be.recheckbe.global.s3.enums.PathName;
import com.be.recheckbe.global.s3.exception.S3ErrorCode;
import com.be.recheckbe.global.s3.service.S3Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  @Qualifier("taskExecutor")
  private final Executor taskExecutor;
  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final S3Service s3Service;
  private final JwtProvider jwtProvider;

  @Override
  public void checkUsername(String username) {
    if (userRepository.existsByUsername(username)) {
      throw new CustomException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
    }
  }

  @Override
  public RegisterResponse register(RegisterRequest request, MultipartFile studentCardImage) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new CustomException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
    }

    if (!request.getPassword().equals(request.getPasswordConfirm())) {
      throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
    }

    Department department =
        departmentRepository
            .findById(request.getDepartmentId())
            .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

    // S3 업로드와 BCrypt 인코딩을 병렬 실행
    CompletableFuture<String> uploadFuture =
        CompletableFuture.supplyAsync(
            () -> s3Service.uploadFile(PathName.STUDENT_CARD, studentCardImage), taskExecutor);
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    String studentCardImageUrl;
    try {
      studentCardImageUrl = uploadFuture.get();
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof CustomException) throw (CustomException) cause;
      throw new CustomException(S3ErrorCode.FILE_SERVER_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CustomException(S3ErrorCode.FILE_SERVER_ERROR);
    }

    User user =
        User.builder()
            .username(request.getUsername())
            .password(encodedPassword)
            .name(request.getName())
            .phoneNumber(request.getPhoneNumber())
            .studentNumber(request.getStudentNumber())
            .role(Role.USER)
            .department(department)
            .studentCardImageUrl(studentCardImageUrl)
            .build();

    User savedUser = userRepository.save(user);
    return new RegisterResponse(savedUser.getId());
  }

  @Override
  @Transactional
  public LoginResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new CustomException(AuthErrorCode.LOGIN_FAIL));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new CustomException(AuthErrorCode.LOGIN_FAIL);
    }

    String accessToken = jwtProvider.createAccessToken(user.getId());
    String refreshToken = jwtProvider.createRefreshToken(user.getId());

    user.updateRefreshToken(refreshToken);

    return new LoginResponse(user.getId(), accessToken, refreshToken);
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(AuthErrorCode.AUTHENTICATION_NOT_FOUND));
    user.updateRefreshToken(null);
  }
}
