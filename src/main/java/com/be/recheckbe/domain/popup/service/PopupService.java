package com.be.recheckbe.domain.popup.service;

import com.be.recheckbe.domain.popup.dto.PopupResponse;

public interface PopupService {

  PopupResponse getPopup();

  PopupResponse updatePopup(String content);

  PopupResponse deactivatePopup();
}