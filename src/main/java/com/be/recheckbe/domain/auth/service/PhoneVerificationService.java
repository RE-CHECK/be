package com.be.recheckbe.domain.auth.service;

import com.be.recheckbe.domain.auth.dto.VerifyCodeRequest;
import com.be.recheckbe.domain.auth.dto.VerifyCodeResponse;

public interface PhoneVerificationService {

  void sendCode(String phoneNumber);

  VerifyCodeResponse verifyCode(VerifyCodeRequest request);

  void validateAndConsumeToken(String phoneNumber, String verifiedToken);
}
