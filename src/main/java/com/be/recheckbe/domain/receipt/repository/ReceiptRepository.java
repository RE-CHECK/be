package com.be.recheckbe.domain.receipt.repository;

import com.be.recheckbe.domain.receipt.entity.Receipt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

  boolean existsByConfirmNum(int confirmNum); // 승인번호 중복 확인

  @Query("SELECT COALESCE(SUM(r.paymentAmount), 0) FROM Receipt r WHERE r.user.id = :userId")
  int sumPaymentAmountByUserId(@Param("userId") Long userId); // 사용자 별 누적 소비 금액

  int countBy(); // 전체 영수증 수 (총 누적 참여 횟수)

  @Query("SELECT COALESCE(SUM(r.paymentAmount), 0) FROM Receipt r")
  int sumAllPaymentAmount(); // 총 누적 소비 금액

  @Query(
      "SELECT COALESCE(SUM(r.paymentAmount), 0) FROM Receipt r WHERE r.user.department.college.id = :collegeId")
  int sumPaymentAmountByCollegeId(@Param("collegeId") Long collegeId); // 단과대별 누적 소비 금액

  @Query(
      "SELECT FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM-DD'), c.name, SUM(r.paymentAmount) "
          + "FROM Receipt r "
          + "JOIN r.user u "
          + "JOIN u.department d "
          + "JOIN d.college c "
          + "GROUP BY FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM-DD'), c.id, c.name "
          + "ORDER BY FUNCTION('TO_CHAR', r.createdAt, 'YYYY-MM-DD') ASC, c.name ASC")
  List<Object[]> findDailyPaymentAmountByCollege(); // 일자별 단과대 소비금액 집계
}
