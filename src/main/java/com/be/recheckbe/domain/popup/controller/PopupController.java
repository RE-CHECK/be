package com.be.recheckbe.domain.popup.controller;

import com.be.recheckbe.domain.popup.dto.PopupResponse;
import com.be.recheckbe.domain.popup.service.PopupService;
import com.be.recheckbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/popup")
@Tag(name = "Popup", description = "팝업 API")
public class PopupController {

  private final PopupService popupService;

  @GetMapping
  @Operation(summary = "팝업 조회", description = "현재 활성화된 팝업 정보를 조회합니다. active=false면 팝업 미표시.")
  public BaseResponse<PopupResponse> getPopup() {
    return BaseResponse.success(popupService.getPopup());
  }
}
