package com.be.recheckbe.domain.popup.service;

import com.be.recheckbe.domain.popup.dto.PopupResponse;
import com.be.recheckbe.domain.popup.entity.Popup;
import com.be.recheckbe.domain.popup.repository.PopupRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PopupServiceImpl implements PopupService {

  private final PopupRepository popupRepository;

  @Override
  @Transactional(readOnly = true)
  public PopupResponse getPopup() {
    return new PopupResponse(getPopupConfig());
  }

  @Override
  @Transactional
  public PopupResponse updatePopup(String content) {
    Popup popup = getPopupConfig();
    popup.update(content);
    return new PopupResponse(popup);
  }

  @Override
  @Transactional
  public PopupResponse deactivatePopup() {
    Popup popup = getPopupConfig();
    popup.deactivate();
    return new PopupResponse(popup);
  }

  private Popup getPopupConfig() {
    return popupRepository
        .findById(Popup.CONFIG_ID)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));
  }
}