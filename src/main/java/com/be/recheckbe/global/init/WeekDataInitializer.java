package com.be.recheckbe.global.init;

import com.be.recheckbe.domain.week.entity.Week;
import com.be.recheckbe.domain.week.repository.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(3)
public class WeekDataInitializer implements ApplicationRunner {

  private static final Long WEEK_CONFIG_ID = 1L;

  private final WeekRepository weekRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (weekRepository.existsById(WEEK_CONFIG_ID)) return;

    weekRepository.save(Week.builder().id(WEEK_CONFIG_ID).weekNumber(null).build());
  }
}
