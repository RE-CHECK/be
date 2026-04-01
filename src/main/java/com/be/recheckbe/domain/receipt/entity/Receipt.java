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
@Table(name = "receipts")
public class Receipt extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // s3 url

    @Column(nullable = false)
    private int paymentAmount; // 소비금액

    @Column
    private Integer weekNumber; // 주차 (1, 2, 3 / null = 테스트 주간)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
