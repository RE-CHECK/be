package com.be.recheckbe.domain.popup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePopupRequest {

  @NotBlank(message = "팝업 내용을 입력해주세요.")
  private String content;
}
