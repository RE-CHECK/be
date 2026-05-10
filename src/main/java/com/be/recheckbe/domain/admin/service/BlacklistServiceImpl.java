package com.be.recheckbe.domain.admin.service;

import com.be.recheckbe.domain.admin.dto.BanUserRequest;
import com.be.recheckbe.domain.admin.entity.Blacklist;
import com.be.recheckbe.domain.admin.exception.AdminErrorCode;
import com.be.recheckbe.domain.admin.repository.BlacklistRepository;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.global.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final BlacklistRepository blacklistRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void banUser(BanUserRequest request, Long adminId) {
    if (blacklistRepository.existsByPhoneNumberAndActiveTrue(request.getPhoneNumber())) {
      throw new CustomException(AdminErrorCode.ALREADY_BLACKLISTED);
    }

    User user =
        userRepository
            .findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new CustomException(AdminErrorCode.USER_NOT_FOUND_BY_PHONE));
    userRepository.delete(user);

    blacklistRepository.save(
        Blacklist.builder()
            .phoneNumber(request.getPhoneNumber())
            .bannedByAdminId(adminId)
            .active(true)
            .build());
  }

  @Override
  @Transactional
  public void unbanBlacklist(String phoneNumber) {
    Blacklist blacklist =
        blacklistRepository
            .findByPhoneNumberAndActiveTrue(phoneNumber)
            .orElseThrow(() -> new CustomException(AdminErrorCode.BLACKLIST_NOT_FOUND));
    blacklist.unban();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isBlacklisted(String phoneNumber) {
    return blacklistRepository.existsByPhoneNumberAndActiveTrue(phoneNumber);
  }

  @Override
  @Transactional(readOnly = true)
  public void downloadBlacklistCsv(HttpServletResponse response) throws IOException {
    String filename = "blacklist_" + LocalDate.now(KST) + ".csv";
    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

    PrintWriter writer = response.getWriter();
    writer.write('﻿');
    writer.println("\"차단일시\",\"전화번호\",\"활성 여부\"");

    List<Blacklist> entries = blacklistRepository.findAllByOrderByCreatedAtDesc();
    for (Blacklist entry : entries) {
      String bannedAt =
          entry.getCreatedAt() != null ? entry.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
      String phoneNumber = escape(entry.getPhoneNumber());
      String active = entry.isActive() ? "차단중" : "해제됨";

      writer.printf("\"%s\",\"%s\",\"%s\"%n", bannedAt, phoneNumber, active);
    }

    writer.flush();
  }

  private String escape(String value) {
    if (value == null) return "";
    return value.replace("\"", "\"\"");
  }
}
