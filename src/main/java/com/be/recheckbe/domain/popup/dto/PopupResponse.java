package com.be.recheckbe.domain.popup.dto;

import com.be.recheckbe.domain.popup.entity.Popup;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PopupResponse {

  private final boolean active;
  private final String content;
  private final LocalDateTime updatedAt;

  public PopupResponse(Popup popup) {
    this.active = popup.isActive();
    this.content = popup.getContent();
    this.updatedAt = popup.getModifiedAt();
  }
}