package com.be.recheckbe.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequest {

  @NotBlank private String username;

  @NotBlank private String password;

  @NotBlank private String passwordConfirm;

  @NotBlank private String name;

  @NotBlank private String phoneNumber;

  @NotNull private Integer studentNumber;

  @NotNull private Long departmentId;
}
