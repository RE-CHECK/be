package com.be.recheckbe.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyCodeRequest {

  @NotBlank
  @Pattern(regexp = "^01[016789]\\d{7,8}$", message = "올바른 휴대폰 번호를 입력해 주세요.")
  private String phoneNumber;

  @NotBlank
  @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
  private String code;
}
