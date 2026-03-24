package com.be.recheckbe.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckUsernameRequest {

    @NotBlank
    private String username;
}