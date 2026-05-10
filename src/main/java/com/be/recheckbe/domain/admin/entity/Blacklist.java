package com.be.recheckbe.domain.admin.entity;

import com.be.recheckbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "blacklist",
    indexes = {@Index(name = "idx_blacklist_phone_number", columnList = "phone_number")})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Blacklist extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String phoneNumber;

  @Column private Long bannedByAdminId;

  @Column(nullable = false)
  private boolean active;

  public void unban() {
    this.active = false;
  }
}
