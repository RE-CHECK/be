package com.be.recheckbe.domain.user.entity;

import com.be.recheckbe.domain.department.entity.Department;
import com.be.recheckbe.domain.receipt.entity.Receipt;
import com.be.recheckbe.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_user_department_id", columnList = "department_id"),
      @Index(name = "idx_user_student_number", columnList = "student_number")
    })
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username; // 사용자 아아디

  @Column(nullable = false)
  @JsonIgnore
  private String password;

  @Column(nullable = false)
  private String name; // 사용자 이름

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role; // user or admin

  @Column private String phoneNumber; // 전화번호

  @Column private Long studentNumber; // 학번

  @Column private String studentCardImageUrl; // 학생증 사진 이미지 url

  private String refreshToken;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  private Department department;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Receipt> receipts = new ArrayList<>();

  // 비밀번호 변경 시 사용하는 메서드
  public void updatePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
