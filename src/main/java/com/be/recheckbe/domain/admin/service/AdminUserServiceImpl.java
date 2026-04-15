package com.be.recheckbe.domain.admin.service;

import com.be.recheckbe.domain.admin.dto.UserRegistrationStatsResponse;
import com.be.recheckbe.domain.user.entity.Role;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

  @Override
  @Transactional(readOnly = true)
  public void downloadUsersCsv(HttpServletResponse response) throws IOException {
    String filename = "users_" + LocalDate.now() + ".csv";
    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

    PrintWriter writer = response.getWriter();

    // Excel 한글 깨짐 방지 BOM
    writer.write('\uFEFF');

    // 헤더
    writer.println("\"가입일시\",\"유저 ID\",\"단과대\",\"학과\"");

    List<User> users = userRepository.findAllByRoleWithDepartmentAndCollege(Role.USER);
    for (User user : users) {
      String createdAt = user.getCreatedAt() != null
          ? user.getCreatedAt().format(DATE_TIME_FORMATTER)
          : "";
      String username = escape(user.getUsername());
      String college = escape(user.getDepartment().getCollege().getName());
      String department = escape(user.getDepartment().getName());

      writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n", createdAt, username, college, department);
    }

    writer.flush();
  }

  private String escape(String value) {
    if (value == null) return "";
    return value.replace("\"", "\"\"");
  }
}