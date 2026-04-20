package com.be.recheckbe.global.init;

import com.be.recheckbe.domain.popup.entity.Popup;
import com.be.recheckbe.domain.popup.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(4)
public class PopupDataInitializer implements ApplicationRunner {

  private final PopupRepository popupRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (popupRepository.existsById(Popup.CONFIG_ID)) return;

    popupRepository.save(
        Popup.builder().id(Popup.CONFIG_ID).content(null).active(false).build());
  }
}