package com.be.recheckbe.global.init;

import com.be.recheckbe.domain.department.entity.Department;
import com.be.recheckbe.domain.department.repository.DepartmentRepository;
import com.be.recheckbe.domain.user.entity.Role;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(3)
public class TestUserDataInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final DepartmentRepository departmentRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  // 단과대별 대표 학과 (겹치지 않게 각기 다른 단과대에서 선정)
  private static final int[] STUDENT_YEAR_PREFIXES = {2023, 2024, 2025, 2026};
  private static final Random RANDOM = new Random();

  private static final List<String> TEST_DEPARTMENTS =
      List.of(
          "기계공학과",       // 공과대학
          "전자공학과",       // 첨단ICT융합대학
          "소프트웨어학과",   // 소프트웨어융합대학
          "수학과",           // 자연과학대학
          "경영학과",         // 경영대학
          "국어국문학과",     // 인문대학
          "행정학과",         // 사회과학대학
          "의학과",           // 의과대학
          "간호학과",         // 간호대학
          "약학과",           // 약학대학
          "첨단바이오융합대학", // 첨단바이오융합대학
          "자유전공학부(자연)" // 다산학부대학
      );

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.countByUsernameStartingWith("test") >= TEST_DEPARTMENTS.size()) return;

    String encodedPassword = passwordEncoder.encode("1234");

    for (int i = 0; i < TEST_DEPARTMENTS.size(); i++) {
      String deptName = TEST_DEPARTMENTS.get(i);
      Department department =
          departmentRepository
              .findByName(deptName)
              .orElseThrow(() -> new IllegalStateException("학과를 찾을 수 없습니다: " + deptName));

      User user =
          User.builder()
              .username("test" + (i + 1))
              .password(encodedPassword)
              .name("테스트유저" + (i + 1))
              .role(Role.TEST)
              .studentNumber(generateStudentNumber())
              .department(department)
              .build();

      userRepository.save(user);
    }
  }

  private long generateStudentNumber() {
    int year = STUDENT_YEAR_PREFIXES[RANDOM.nextInt(STUDENT_YEAR_PREFIXES.length)];
    int suffix = RANDOM.nextInt(1_000_000); // 000000 ~ 999999
    return (long) year * 1_000_000 + suffix;
  }
}