package com.be.recheckbe.domain.admin.service;

import com.be.recheckbe.domain.admin.dto.BanUserRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface BlacklistService {

  void banUser(BanUserRequest request, Long adminId);

  void unbanBlacklist(Long blacklistId);

  boolean isBlacklisted(String phoneNumber);

  void downloadBlacklistCsv(HttpServletResponse response) throws IOException;
}
