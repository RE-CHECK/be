package com.be.recheckbe.domain.week.service;

import com.be.recheckbe.domain.week.dto.CurrentWeekResponse;

public interface WeekService {

  CurrentWeekResponse getCurrentWeek();

  CurrentWeekResponse activateWeek(int weekNumber);

  CurrentWeekResponse deactivateWeek();
}
