package com.be.recheckbe.domain.user.service;

import com.be.recheckbe.domain.user.dto.UserDashboardResponse;

public interface UserService {

  UserDashboardResponse getDashboard(Long userId);
}