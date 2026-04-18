package com.be.recheckbe.domain.admin.service;

import com.be.recheckbe.domain.admin.dto.UserRegistrationStatsResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AdminUserService {

  UserRegistrationStatsResponse getUserRegistrationStats();

  void downloadUsersCsv(HttpServletResponse response) throws IOException;
}
