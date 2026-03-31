package com.be.recheckbe.domain.receipt.repository;

import com.be.recheckbe.domain.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    @Query("SELECT COALESCE(SUM(r.paymentAmount), 0) FROM Receipt r WHERE r.user.id = :userId")
    int sumPaymentAmountByUserId(@Param("userId") Long userId); // 사용자 별 누적 소비 금액

    int countBy(); // 전체 영수증 수 (총 누적 참여 횟수)

    @Query("SELECT COALESCE(SUM(r.paymentAmount), 0) FROM Receipt r")
    int sumAllPaymentAmount(); // 총 누적 소비 금액
}