package com.be.recheckbe.domain.auth.service;

import com.be.recheckbe.domain.admin.service.BlacklistService;
import com.be.recheckbe.domain.auth.dto.VerifyCodeRequest;
import com.be.recheckbe.domain.auth.dto.VerifyCodeResponse;
import com.be.recheckbe.domain.auth.entity.PhoneVerification;
import com.be.recheckbe.domain.auth.exception.AuthErrorCode;
import com.be.recheckbe.domain.auth.repository.PhoneVerificationRepository;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.sms.SmsService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

  private static final int CODE_LENGTH = 6;
  private static final int CODE_EXPIRE_MINUTES = 5;

  private final PhoneVerificationRepository phoneVerificationRepository;
  private final UserRepository userRepository;
  private final BlacklistService blacklistService;
  private final SmsService smsService;

  @Override
  @Transactional
  public void sendCode(String phoneNumber) {
    if (blacklistService.isBlacklisted(phoneNumber)) {
      throw new CustomException(AuthErrorCode.PHONE_NUMBER_BLACKLISTED);
    }
    if (userRepository.existsByPhoneNumber(phoneNumber)) {
      throw new CustomException(AuthErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    phoneVerificationRepository.deleteByPhoneNumber(phoneNumber);

    String code = generateCode();
    PhoneVerification verification =
        PhoneVerification.builder()
            .phoneNumber(phoneNumber)
            .code(code)
            .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES))
            .verified(false)
            .build();
    phoneVerificationRepository.save(verification);

    smsService.sendVerificationCode(phoneNumber, code);
  }

  @Override
  @Transactional
  public VerifyCodeResponse verifyCode(VerifyCodeRequest request) {
    PhoneVerification verification =
        phoneVerificationRepository
            .findTopByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber())
            .orElseThrow(() -> new CustomException(AuthErrorCode.PHONE_VERIFICATION_NOT_FOUND));

    if (verification.isVerified()) {
      throw new CustomException(AuthErrorCode.PHONE_VERIFICATION_NOT_FOUND);
    }
    if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
      throw new CustomException(AuthErrorCode.PHONE_VERIFICATION_CODE_EXPIRED);
    }
    if (!verification.getCode().equals(request.getCode())) {
      throw new CustomException(AuthErrorCode.PHONE_VERIFICATION_CODE_MISMATCH);
    }

    String token = UUID.randomUUID().toString();
    verification.markVerified(token);

    return new VerifyCodeResponse(token);
  }

  @Override
  @Transactional
  public void validateAndConsumeToken(String phoneNumber, String verifiedToken) {
    PhoneVerification verification =
        phoneVerificationRepository
            .findByVerifiedTokenAndVerifiedTrue(verifiedToken)
            .orElseThrow(() -> new CustomException(AuthErrorCode.PHONE_VERIFICATION_REQUIRED));

    if (!verification.getPhoneNumber().equals(phoneNumber)) {
      throw new CustomException(AuthErrorCode.PHONE_VERIFICATION_REQUIRED);
    }
    if (LocalDateTime.now().isAfter(verification.getTokenExpiresAt())) {
      throw new CustomException(AuthErrorCode.PHONE_VERIFICATION_TOKEN_EXPIRED);
    }

    phoneVerificationRepository.delete(verification);
  }

  private String generateCode() {
    SecureRandom random = new SecureRandom();
    int code = random.nextInt(900000) + 100000;
    return String.valueOf(code);
  }
}
