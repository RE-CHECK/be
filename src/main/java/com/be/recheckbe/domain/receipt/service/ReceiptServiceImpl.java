package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.college.entity.College;
import com.be.recheckbe.domain.receipt.dto.CollegeTotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.RankingResponse;
import com.be.recheckbe.domain.receipt.dto.TotalAllPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.UploadReceiptResponse;
import com.be.recheckbe.domain.receipt.dto.Week2RankingGroupResponse;
import com.be.recheckbe.domain.receipt.dto.Week3ChallengeResponse;
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
import com.be.recheckbe.global.s3.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

  private static final String SUPPORTED_CARD_COMPANY = "국민카드";
  private static final int RANKING_TOP_N = 4;

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
      List.of("공과대학", "소프트웨어융합대학", "첨단바이오융합대학", "인문대학");

  // 대진 2: 사랑집3
  private static final String RANKING_STORE_NAME_2 = "사랑집3";
  private static final List<String> RANKING_ELIGIBLE_COLLEGES_2 =
      List.of("첨단바이오융합대학", "다산학부대학", "사회과학대학");
  // 국방디지털융합과는 학과(department)이지만 예외적으로 랭킹 표시명을 단과대 자리에 사용
  private static final String RANKING_ELIGIBLE_DEPARTMENT_2 = "국방디지털융합과";
  private static final String RANKING_ELIGIBLE_DEPARTMENT_DISPLAY_NAME_2 = "국방디지털융합학과";

  // 대진 3: 사랑집2
  private static final String RANKING_STORE_NAME_3 = "사랑집2";
  private static final List<String> RANKING_ELIGIBLE_COLLEGES_3 =
      List.of("자연과학대학", "경영대학", "의과대학", "간호대학", "약학대학");
  // 의과대학, 간호대학, 약학대학은 "메디컬"로 통합 표시
  private static final List<String> RANKING_MEDICAL_COLLEGES_3 = List.of("의과대학", "간호대학", "약학대학");
  private static final String RANKING_MEDICAL_DISPLAY_NAME_3 = "메디컬";
  // 경제정치사회융합학부는 학과(department)이지만 예외적으로 랭킹 표시명을 단과대 자리에 사용
  private static final String RANKING_ELIGIBLE_DEPARTMENT_3 = "경제정치사회융합학부";

  private final S3Service s3Service;
  private final OcrService ocrService;
  private final ReceiptRepository receiptRepository;
  private final UserRepository userRepository;
  private final WeekRepository weekRepository;

  @Override
  @Transactional
  public UploadReceiptResponse uploadReceiptImage(Long userId, MultipartFile image) {
    User user = userRepository.getReferenceById(userId);

    // ocr 호출
    OcrExtractedData ocrData = ocrService.extractReceiptData(image);

    // 국민카드 영수증만 허용
    String cardCompany = ocrData.getCardCompany();
    if (cardCompany == null || !cardCompany.contains(SUPPORTED_CARD_COMPANY)) {
      throw new CustomException(ReceiptErrorCode.NOT_SUPPORT_CARD_COMPANY);
    }

    // 승인번호 중복 확인 (파싱 실패 시 0 반환 → 중복 체크 건너뜀)
    int confirmNum = parseConfirmNum(ocrData.getConfirmNum());
    if (confirmNum != 0 && receiptRepository.existsByConfirmNum(confirmNum)) {
      throw new CustomException(ReceiptErrorCode.DUPLICATE_RECEIPT);
    }

    // 현재 활성화된 주차 조회
    Integer currentWeekNumber =
        weekRepository.findById(Week.CONFIG_ID).map(week -> week.getWeekNumber()).orElse(null);

    // ocr 성공 시 s3로 업로드 (파일 고아(orphan) 방지)
    String imageUrl = s3Service.uploadFile(PathName.RECEIPT, image);

    Receipt receipt =
        Receipt.builder()
            .imageUrl(imageUrl)
            .paymentAmount(ocrData.getPaymentAmount())
            .storeName(ocrData.getStoreName())
            .cardCompany(cardCompany)
            .confirmNum(confirmNum)
            .weekNumber(currentWeekNumber)
            .user(user)
            .build();

    receiptRepository.save(receipt);

    return UploadReceiptResponse.of(imageUrl, ocrData);
  }

  private int parseConfirmNum(String confirmNum) {
    if (confirmNum == null || confirmNum.isBlank()) return 0;
    try {
      return Integer.parseInt(confirmNum.replaceAll("[^0-9]", ""));
    } catch (NumberFormatException e) {
      return 0;
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
      // 국방디지털융합과는 학과(department)이지만 예외적으로 학과명을 단과대 자리에 표시
      String displayName =
          RANKING_ELIGIBLE_DEPARTMENT_2.equals(departmentName)
              ? RANKING_ELIGIBLE_DEPARTMENT_DISPLAY_NAME_2
              : collegeName;
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
      // 의과대학/간호대학/약학대학은 "메디컬"로 통합 표시
      // 경제정치사회융합학부는 학과(department)이지만 예외적으로 학과명을 단과대 자리에 표시
      String displayName;
      if (RANKING_MEDICAL_COLLEGES_3.contains(collegeName)) {
        displayName = RANKING_MEDICAL_DISPLAY_NAME_3;
      } else if (RANKING_ELIGIBLE_DEPARTMENT_3.equals(departmentName)) {
        displayName = RANKING_ELIGIBLE_DEPARTMENT_3;
      } else {
        displayName = collegeName;
      }
      rankings.add(new RankingResponse(i + 1, displayName, totalPaymentAmount));
    }
    return new Week2RankingGroupResponse(RANKING_STORE_NAME_3, rankings);
  }
}
