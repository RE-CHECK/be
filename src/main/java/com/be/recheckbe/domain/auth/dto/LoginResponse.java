package com.be.recheckbe.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private final Long id; // ga 트래킹 요구사항 반영 (userId)

  private final String accessToken;

  @JsonIgnore private final String refreshToken;
}
