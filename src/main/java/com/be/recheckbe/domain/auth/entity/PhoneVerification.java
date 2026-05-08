package com.be.recheckbe.domain.auth.entity;

import com.be.recheckbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "phone_verification",
    indexes = {@Index(name = "idx_phone_verification_phone_number", columnList = "phone_number")})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVerification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false, length = 6)
  private String code;

  @Column private String verifiedToken;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column private LocalDateTime tokenExpiresAt;

  @Column(nullable = false)
  private boolean verified;

  public void markVerified(String token) {
    this.verified = true;
    this.verifiedToken = token;
    this.tokenExpiresAt = LocalDateTime.now().plusMinutes(30);
  }
}
