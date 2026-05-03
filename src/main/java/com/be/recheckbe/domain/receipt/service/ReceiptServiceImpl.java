package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.college.entity.College;
import com.be.recheckbe.domain.receipt.dto.*;
import com.be.recheckbe.domain.receipt.entity.Receipt;
import com.be.recheckbe.domain.receipt.exception.ReceiptErrorCode;
import com.be.recheckbe.domain.receipt.repository.ReceiptRepository;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.domain.week.entity.Week;
import com.be.recheckbe.domain.week.repository.WeekRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
import com.be.recheckbe.global.ocr.dto.OcrExtractedData;
import com.be.recheckbe.global.ocr.service.OcrService;
import com.be.recheckbe.global.s3.enums.PathName;
import com.be.recheckbe.global.s3.exception.S3ErrorCode;
import com.be.recheckbe.global.s3.service.S3Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

  @Qualifier("taskExecutor")
  private final Executor taskExecutor;

  private static final List<String> SUPPORTED_CARD_KEYWORDS = List.of("국민", "KB");
  private static final int RANKING_TOP_N = 4;

  private static final List<String> WEEK_DAY_NUMBERS = List.of("2", "3", "4", "5", "6", "7", "1");
  private static final Map<String, String> DAY_NUM_TO_KR =
      Map.of("2", "월", "3", "화", "4", "수", "5", "목", "6", "금", "7", "토", "1", "일");

  // 3주차 대진
  private static final String CHALLENGE_STORE_NAME_23_24 = "사랑집4";
  private static final String CHALLENGE_STORE_NAME_25_26 = "사랑집5";
  // 학번 범위 (앞 4자리가 입학년도, e.g. 23학번 = 2023_000_000 ~ 2023_999_999)
  private static final int STUDENT_NUM_MIN_23 = 2023_000_000;
  private static final int STUDENT_NUM_MAX_23 = 2023_999_999;
  private static final int STUDENT_NUM_MIN_24 = 2024_000_000;
  private static final int STUDENT_NUM_MAX_24 = 2024_999_999;
  private static final int STUDENT_NUM_MIN_25 = 2025_000_000;
  private static final int STUDENT_NUM_MAX_25 = 2025_999_999;
  private static final int STUDENT_NUM_MIN_26 = 2026_000_000;
  private static final int STUDENT_NUM_MAX_26 = 2026_999_999;

  // 대진 1: 사랑집1
  private static final String RANKING_STORE_NAME_1 = "사랑집1";
  private static final List<String> RANKING_ELIGIBLE_COLLEGES_1 =
      List.of("공과대학", "소프트웨어융합대학", "첨단ICT융합대학", "인문대학");

  // 대진 2: 사랑집2
  private static final String RANKING_STORE_NAME_2 = "사랑집2";
  private static final List<String> RANKING_ELIGIBLE_COLLEGES_2 =
      List.of("자연과학대학", "경영대학", "의과대학", "간호대학", "약학대학");
  // 의과대학, 간호대학, 약학대학은 "메디컬"로 통합 표시
  private static final List<String> RANKING_MEDICAL_COLLEGES_2 = List.of("의과대학", "간호대학", "약학대학");
  private static final String RANKING_MEDICAL_DISPLAY_NAME_2 = "메디컬";
  // 경제정치사회융합학부는 학과(department)이지만 예외적으로 랭킹 표시명을 단과대 자리에 사용
  private static final String RANKING_ELIGIBLE_DEPARTMENT_2 = "경제정치사회융합학부";

  // 대진 3: 사랑집3
  private static final String RANKING_STORE_NAME_3 = "사랑집3";
  private static final List<String> RANKING_ELIGIBLE_COLLEGES_3 =
      List.of("첨단바이오융합대학", "다산학부대학", "사회과학대학");
  // 국방디지털융합과는 학과(department)이지만 예외적으로 랭킹 표시명을 단과대 자리에 사용
  private static final String RANKING_ELIGIBLE_DEPARTMENT_3 = "국방디지털융합과";
  private static final String RANKING_ELIGIBLE_DEPARTMENT_DISPLAY_NAME_3 = "국방디지털융합학과";

  private final S3Service s3Service;
  private final OcrService ocrService;
  private final ReceiptRepository receiptRepository;
  private final UserRepository userRepository;
  private final WeekRepository weekRepository;

  @Override
  public AnalyzeReceiptResponse analyzeReceiptImage(Long userId, MultipartFile image) {
    // OCR 호출
    OcrExtractedData ocrData = ocrService.extractReceiptData(image);

    // 국민카드 영수증만 허용
    String cardCompany = ocrData.getCardCompany();
    if (cardCompany == null || SUPPORTED_CARD_KEYWORDS.stream().noneMatch(cardCompany::contains)) {
      throw new CustomException(ReceiptErrorCode.NOT_SUPPORT_CARD_COMPANY);
    }

    // 승인번호 중복 확인 (파싱 실패 시 0 반환 → 중복 체크 건너뜀)
    long confirmNum = parseConfirmNum(ocrData.getConfirmNum());
    if (confirmNum != 0 && receiptRepository.existsByConfirmNum(confirmNum)) {
      throw new CustomException(ReceiptErrorCode.DUPLICATE_RECEIPT);
    }

    return AnalyzeReceiptResponse.from(ocrData);
  }

  @Override
  public UploadReceiptResponse confirmReceiptUpload(
      Long userId, MultipartFile image, ConfirmReceiptRequest data) {
    User user = userRepository.getReferenceById(userId);

    // S3 업로드와 주차 조회를 병렬 실행 (S3 업로드가 완료되길 기다리는 동안 DB 커넥션 비점유)
    CompletableFuture<String> uploadFuture =
        CompletableFuture.supplyAsync(
            () -> s3Service.uploadFile(PathName.RECEIPT, image), taskExecutor);

    Integer currentWeekNumber =
        weekRepository.findById(Week.CONFIG_ID).map(Week::getWeekNumber).orElse(null);

    String imageUrl;
    try {
      imageUrl = uploadFuture.get();
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof CustomException) throw (CustomException) cause;
      throw new CustomException(S3ErrorCode.FILE_SERVER_ERROR);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new CustomException(S3ErrorCode.FILE_SERVER_ERROR);
    }

    Receipt receipt =
        Receipt.builder()
            .imageUrl(imageUrl)
            .paymentAmount(data.getPaymentAmount())
            .storeName(data.getStoreName())
            .cardCompany(data.getCardCompany())
            .confirmNum(Long.parseLong(data.getConfirmNum()))
            .weekNumber(currentWeekNumber)
            .user(user)
            .build();

    receiptRepository.save(receipt);

    return UploadReceiptResponse.builder()
        .imageUrl(imageUrl)
        .storeName(data.getStoreName())
        .paymentAmount(data.getPaymentAmount())
        .cardCompany(data.getCardCompany())
        .confirmNum(data.getConfirmNum())
        .build();
  }

  private long parseConfirmNum(String confirmNum) {
    if (confirmNum == null || confirmNum.isBlank()) {
      log.warn("[confirmNum] OCR 추출값 없음 (null 또는 blank) → 0 처리");
      return 0L;
    }
    try {
      long parsed = Long.parseLong(confirmNum.replaceAll("[^0-9]", ""));
      log.info("[confirmNum] raw='{}' → parsed={}", confirmNum, parsed);
      return parsed;
    } catch (NumberFormatException e) {
      log.warn("[confirmNum] 파싱 실패: raw='{}' → 0 처리", confirmNum);
      return 0L;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public TotalParticipationResponse getTotalParticipationCount() {
    int count = receiptRepository.countBy();
    return new TotalParticipationResponse(count);
  }

  @Override
  @Transactional(readOnly = true)
  public TotalAllPaymentResponse getTotalAllPaymentAmount() {
    int total = receiptRepository.sumAllPaymentAmount();
    return new TotalAllPaymentResponse(total);
  }

  @Override
  @Transactional(readOnly = true)
  public CollegeTotalPaymentResponse getCollegeTotalPaymentAmount(Long userId) {
    User user =
        userRepository
            .findByIdWithCollegeInfo(userId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

    College college = user.getDepartment().getCollege();
    int total = receiptRepository.sumPaymentAmountByCollegeId(college.getId());

    return new CollegeTotalPaymentResponse(college.getId(), college.getName(), total);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Week2RankingGroupResponse> getWeek2Ranking() {
    return List.of(buildGroup1Ranking(), buildGroup2Ranking(), buildGroup3Ranking());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Week3ChallengeResponse> getWeek3Challenge() {
    int total23 =
        receiptRepository.sumWeek3PaymentByStudentNumRange(
            CHALLENGE_STORE_NAME_23_24, STUDENT_NUM_MIN_23, STUDENT_NUM_MAX_23);
    int total24 =
        receiptRepository.sumWeek3PaymentByStudentNumRange(
            CHALLENGE_STORE_NAME_23_24, STUDENT_NUM_MIN_24, STUDENT_NUM_MAX_24);

    int total25 =
        receiptRepository.sumWeek3PaymentByStudentNumRange(
            CHALLENGE_STORE_NAME_25_26, STUDENT_NUM_MIN_25, STUDENT_NUM_MAX_25);
    int total26 =
        receiptRepository.sumWeek3PaymentByStudentNumRange(
            CHALLENGE_STORE_NAME_25_26, STUDENT_NUM_MIN_26, STUDENT_NUM_MAX_26);

    return List.of(
        Week3ChallengeResponse.of("23학번", total23, "24학번", total24),
        Week3ChallengeResponse.of("25학번", total25, "26학번", total26));
  }

  private Week2RankingGroupResponse buildGroup1Ranking() {
    List<Object[]> rows =
        receiptRepository.findWeek2RankingByColleges(
            RANKING_STORE_NAME_1, RANKING_ELIGIBLE_COLLEGES_1);

    List<RankingResponse> rankings = new ArrayList<>();
    int limit = Math.min(rows.size(), RANKING_TOP_N);
    for (int i = 0; i < limit; i++) {
      String collegeName = (String) rows.get(i)[0];
      int totalPaymentAmount = ((Number) rows.get(i)[1]).intValue();
      rankings.add(new RankingResponse(i + 1, collegeName, totalPaymentAmount));
    }
    return new Week2RankingGroupResponse(RANKING_STORE_NAME_1, rankings);
  }

  private Week2RankingGroupResponse buildGroup2Ranking() {
    List<Object[]> rows =
        receiptRepository.findWeek2RankingByCollegesOrDepartment(
            RANKING_STORE_NAME_2, RANKING_ELIGIBLE_COLLEGES_2, RANKING_ELIGIBLE_DEPARTMENT_2);

    List<RankingResponse> rankings = new ArrayList<>();
    int limit = Math.min(rows.size(), RANKING_TOP_N);
    for (int i = 0; i < limit; i++) {
      String collegeName = (String) rows.get(i)[0];
      String departmentName = (String) rows.get(i)[1];
      int totalPaymentAmount = ((Number) rows.get(i)[2]).intValue();
      // 의과대학/간호대학/약학대학은 "메디컬"로 통합 표시
      // 경제정치사회융합학부는 학과(department)이지만 예외적으로 학과명을 단과대 자리에 표시
      String displayName;
      if (RANKING_MEDICAL_COLLEGES_2.contains(collegeName)) {
        displayName = RANKING_MEDICAL_DISPLAY_NAME_2;
      } else if (RANKING_ELIGIBLE_DEPARTMENT_2.equals(departmentName)) {
        displayName = RANKING_ELIGIBLE_DEPARTMENT_2;
      } else {
        displayName = collegeName;
      }
      rankings.add(new RankingResponse(i + 1, displayName, totalPaymentAmount));
    }
    return new Week2RankingGroupResponse(RANKING_STORE_NAME_2, rankings);
  }

  private Week2RankingGroupResponse buildGroup3Ranking() {
    List<Object[]> rows =
        receiptRepository.findWeek2RankingByCollegesOrDepartment(
            RANKING_STORE_NAME_3, RANKING_ELIGIBLE_COLLEGES_3, RANKING_ELIGIBLE_DEPARTMENT_3);

    List<RankingResponse> rankings = new ArrayList<>();
    int limit = Math.min(rows.size(), RANKING_TOP_N);
    for (int i = 0; i < limit; i++) {
      String collegeName = (String) rows.get(i)[0];
      String departmentName = (String) rows.get(i)[1];
      int totalPaymentAmount = ((Number) rows.get(i)[2]).intValue();
      // 국방디지털융합과는 학과(department)이지만 예외적으로 학과명을 단과대 자리에 표시
      String displayName =
          RANKING_ELIGIBLE_DEPARTMENT_3.equals(departmentName)
              ? RANKING_ELIGIBLE_DEPARTMENT_DISPLAY_NAME_3
              : collegeName;
      rankings.add(new RankingResponse(i + 1, displayName, totalPaymentAmount));
    }
    return new Week2RankingGroupResponse(RANKING_STORE_NAME_3, rankings);
  }

  @Override
  @Transactional(readOnly = true)
  public WeeklyRankingResponse getWeeklyCollegeRanking(Integer weekNumber) {
    // 1. 총액 기준 상위 4개 단과대 이름 확정
    List<Object[]> topRows = receiptRepository.findCollegeRankingByWeekNumber(weekNumber);
    int limit = Math.min(topRows.size(), RANKING_TOP_N);
    List<String> topCollegeNames = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      topCollegeNames.add((String) topRows.get(i)[0]);
    }

    if (topCollegeNames.isEmpty()) {
      return new WeeklyRankingResponse(weekNumber, List.of());
    }

    // 2. 상위 4개 단과대의 요일별 금액 조회
    List<Object[]> dailyRows =
        receiptRepository.findDailyAmountByCollegesAndWeekNumber(weekNumber, topCollegeNames);

    // Map<collegeName, Map<dayNum, amount>>
    Map<String, Map<String, Integer>> dailyMap = new HashMap<>();
    for (Object[] row : dailyRows) {
      String collegeName = (String) row[0];
      String dayNum = (String) row[1];
      int amount = ((Number) row[2]).intValue();
      dailyMap.computeIfAbsent(collegeName, k -> new HashMap<>()).put(dayNum, amount);
    }

    // 3. 순위 순서 유지하며 응답 구성 (데이터 없는 요일은 0으로 채움)
    List<CollegeDailyRankingResponse> rankings = new ArrayList<>();
    for (int i = 0; i < topCollegeNames.size(); i++) {
      String collegeName = topCollegeNames.get(i);
      Map<String, Integer> dayAmounts = dailyMap.getOrDefault(collegeName, Map.of());

      List<DailyAmountResponse> dailyAmounts =
          WEEK_DAY_NUMBERS.stream()
              .map(
                  dayNum ->
                      new DailyAmountResponse(
                          DAY_NUM_TO_KR.get(dayNum), dayAmounts.getOrDefault(dayNum, 0)))
              .collect(Collectors.toList());

      rankings.add(new CollegeDailyRankingResponse(i + 1, collegeName, dailyAmounts));
    }

    return new WeeklyRankingResponse(weekNumber, rankings);
  }
}
