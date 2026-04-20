package com.be.recheckbe.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private final String accessToken;

  @JsonIgnore
  private final String refreshToken;
}
