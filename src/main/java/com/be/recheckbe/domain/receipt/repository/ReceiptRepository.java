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

  @Query(
      "SELECT c.name, SUM(r.paymentAmount) "
          + "FROM Receipt r "
          + "JOIN r.user u "
          + "JOIN u.department d "
          + "JOIN d.college c "
          + "WHERE r.weekNumber = 2 "
          + "AND r.storeName = :storeName "
          + "AND c.name IN :collegeNames "
          + "GROUP BY u.id, c.id, c.name "
          + "ORDER BY SUM(r.paymentAmount) DESC, MIN(u.createdAt) ASC")
  List<Object[]> findWeek2RankingByColleges(
      @Param("storeName") String storeName,
      @Param("collegeNames") List<String> collegeNames); // 2주차 단과대별 사용자 랭킹

  @Query(
      "SELECT c.name, d.name, SUM(r.paymentAmount) "
          + "FROM Receipt r "
          + "JOIN r.user u "
          + "JOIN u.department d "
          + "JOIN d.college c "
          + "WHERE r.weekNumber = 2 "
          + "AND r.storeName = :storeName "
          + "AND (c.name IN :collegeNames OR d.name = :departmentName) "
          + "GROUP BY u.id, c.id, c.name, d.id, d.name "
          + "ORDER BY SUM(r.paymentAmount) DESC, MIN(u.createdAt) ASC")
  List<Object[]> findWeek2RankingByCollegesOrDepartment(
      @Param("storeName") String storeName,
      @Param("collegeNames") List<String> collegeNames,
      @Param("departmentName") String departmentName); // 2주차 단과대+학과 혼합 사용자 랭킹

  @Query(
      "SELECT COALESCE(SUM(r.paymentAmount), 0) "
          + "FROM Receipt r "
          + "JOIN r.user u "
          + "WHERE r.weekNumber = 3 "
          + "AND r.storeName = :storeName "
          + "AND u.studentNumber >= :minStudentNum "
          + "AND u.studentNumber <= :maxStudentNum")
  int sumWeek3PaymentByStudentNumRange(
      @Param("storeName") String storeName,
      @Param("minStudentNum") int minStudentNum,
      @Param("maxStudentNum") int maxStudentNum); // 3주차 학번 범위별 영수증 금액 합산

  @Query(
      "SELECT c.name, SUM(r.paymentAmount) "
          + "FROM Receipt r "
          + "JOIN r.user u "
          + "JOIN u.department d "
          + "JOIN d.college c "
          + "WHERE r.weekNumber = :weekNumber "
          + "GROUP BY c.id, c.name "
          + "ORDER BY SUM(r.paymentAmount) DESC")
  List<Object[]> findCollegeRankingByWeekNumber(@Param("weekNumber") int weekNumber);

  @Query(
      "SELECT c.name, FUNCTION('TO_CHAR', r.createdAt, 'D'), SUM(r.paymentAmount) "
          + "FROM Receipt r "
          + "JOIN r.user u "
          + "JOIN u.department d "
          + "JOIN d.college c "
          + "WHERE r.weekNumber = :weekNumber "
          + "AND c.name IN :collegeNames "
          + "GROUP BY c.id, c.name, FUNCTION('TO_CHAR', r.createdAt, 'D') "
          + "ORDER BY c.name, FUNCTION('TO_CHAR', r.createdAt, 'D')")
  List<Object[]> findDailyAmountByCollegesAndWeekNumber(
      @Param("weekNumber") int weekNumber, @Param("collegeNames") List<String> collegeNames);
}
