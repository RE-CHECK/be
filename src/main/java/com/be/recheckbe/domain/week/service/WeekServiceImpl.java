package com.be.recheckbe.domain.week.service;

import com.be.recheckbe.domain.week.dto.CurrentWeekResponse;
import com.be.recheckbe.domain.week.entity.Week;
import com.be.recheckbe.domain.week.exception.WeekErrorCode;
import com.be.recheckbe.domain.week.repository.WeekRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeekServiceImpl implements WeekService {

  private static final Long WEEK_CONFIG_ID = 1L;

  private final WeekRepository weekRepository;

  @Override
  @Transactional(readOnly = true)
  public CurrentWeekResponse getCurrentWeek() {
    Week week = getWeekConfig();
    return new CurrentWeekResponse(week.getWeekNumber());
  }

  @Override
  @Transactional
  public CurrentWeekResponse activateWeek(int weekNumber) {
    if (weekNumber < 1 || weekNumber > 3) {
      throw new CustomException(WeekErrorCode.INVALID_WEEK_NUMBER);
    }
    Week week = getWeekConfig();
    week.activate(weekNumber);
    return new CurrentWeekResponse(week.getWeekNumber());
  }

  @Override
  @Transactional
  public CurrentWeekResponse deactivateWeek() {
    Week week = getWeekConfig();
    week.deactivate();
    return new CurrentWeekResponse(week.getWeekNumber());
  }

  private Week getWeekConfig() {
    return weekRepository
        .findById(WEEK_CONFIG_ID)
        .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));
  }
}