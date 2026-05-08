package com.be.recheckbe.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyCodeResponse {

  private String verifiedToken;
}
