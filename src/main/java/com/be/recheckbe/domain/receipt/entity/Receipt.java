package com.be.recheckbe.domain.receipt.entity;

import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "receipts", indexes = {
    @Index(name = "idx_receipt_user_id", columnList = "user_id"),
    @Index(name = "idx_receipt_week_number", columnList = "week_number"),
    @Index(name = "idx_receipt_store_name", columnList = "store_name"),
    @Index(name = "idx_receipt_week_store", columnList = "week_number, store_name")
})
public class Receipt extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String imageUrl; // s3 url

  @Column(nullable = false)
  private int paymentAmount; // 소비금액

  @Column // (nullable = false)
  private String cardCompany; // 카드사

  @Column(unique = true) // (nullable = false)
  private long confirmNum; // 영수증 승인번호

  @Column // (nullable = false)
  private String storeName; // 점포 이름

  @Column private Integer weekNumber; // 주차 (1, 2, 3 / null = 테스트 주간)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
