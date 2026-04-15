package com.be.recheckbe.domain.admin.service;

import com.be.recheckbe.domain.admin.dto.UserRegistrationStatsResponse;
import com.be.recheckbe.domain.user.entity.Role;
import com.be.recheckbe.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserRegistrationStatsResponse getUserRegistrationStats() {
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
    LocalDateTime endOfToday = startOfToday.plusDays(1);

    long todayCount = userRepository.countByRoleAndCreatedAtBetween(
        Role.USER, startOfToday, endOfToday);
    long totalCount = userRepository.countByRole(Role.USER);

    return new UserRegistrationStatsResponse(todayCount, totalCount);
  }
}