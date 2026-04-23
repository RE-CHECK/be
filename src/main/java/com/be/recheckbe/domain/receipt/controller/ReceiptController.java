package com.be.recheckbe.domain.receipt.controller;

import com.be.recheckbe.domain.receipt.dto.AnalyzeReceiptResponse;
import com.be.recheckbe.domain.receipt.dto.CollegeTotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.ConfirmReceiptRequest;
import com.be.recheckbe.domain.receipt.dto.TotalAllPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.UploadReceiptResponse;
import com.be.recheckbe.domain.receipt.dto.Week2RankingGroupResponse;
import com.be.recheckbe.domain.receipt.dto.Week3ChallengeResponse;
import com.be.recheckbe.domain.receipt.dto.WeeklyRankingResponse;
import com.be.recheckbe.domain.receipt.service.ReceiptService;
import com.be.recheckbe.global.response.BaseResponse;
import com.be.recheckbe.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipts")
@Tag(name = "Receipt", description = "영수증 API")
public class ReceiptController {

  private final ReceiptService receiptService;

  @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "영수증 OCR 분석",
      description = "영수증 이미지를 OCR로 분석하여 결제 정보를 반환합니다. S3 업로드 및 DB 저장은 하지 않습니다.")
  public BaseResponse<AnalyzeReceiptResponse> analyzeReceiptImage(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("image") MultipartFile image) {
    return BaseResponse.success(receiptService.analyzeReceiptImage(userDetails.getId(), image));
  }

  @PostMapping(value = "/confirm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "영수증 업로드 확정", description = "OCR 분석 결과를 확인한 후 영수증 이미지를 S3에 업로드하고 DB에 저장합니다.")
  public BaseResponse<UploadReceiptResponse> confirmReceiptUpload(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestPart("image") MultipartFile image,
      @RequestPart("data") ConfirmReceiptRequest data) {
    return BaseResponse.success(
        receiptService.confirmReceiptUpload(userDetails.getId(), image, data));
  }

  @GetMapping("/total-participation")
  @Operation(summary = "총 누적 참여 횟수 조회", description = "서비스 내 전체 사용자의 영수증 업로드 횟수(누적 참여 횟수)를 조회합니다.")
  public BaseResponse<TotalParticipationResponse> getTotalParticipationCount() {
    return BaseResponse.success(receiptService.getTotalParticipationCount());
  }

  @GetMapping("/total-all-payment")
  @Operation(summary = "총 누적 소비금액 조회", description = "서비스 내 전체 사용자의 영수증 결제금액 합산을 조회합니다.")
  public BaseResponse<TotalAllPaymentResponse> getTotalAllPaymentAmount() {
    return BaseResponse.success(receiptService.getTotalAllPaymentAmount());
  }

  @GetMapping("/college-total-payment")
  @Operation(
      summary = "단과대별 총 누적 소비금액 조회",
      description = "로그인한 사용자의 단과대에 속한 전체 사용자의 영수증 결제금액 합산을 조회합니다.")
  public BaseResponse<CollegeTotalPaymentResponse> getCollegeTotalPaymentAmount(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return BaseResponse.success(receiptService.getCollegeTotalPaymentAmount(userDetails.getId()));
  }

  @GetMapping("/week2-ranking")
  @Operation(summary = "2주차 랭킹 조회", description = "대진별(store_name 기준) 2주차 영수증 금액 합산 1~4등을 조회합니다.")
  public BaseResponse<List<Week2RankingGroupResponse>> getWeek2Ranking() {
    return BaseResponse.success(receiptService.getWeek2Ranking());
  }

  @GetMapping("/week3-challenge")
  @Operation(
      summary = "3주차 대결 결과 조회",
      description =
          "3주차 학번 대진 결과를 조회합니다. (23학번 vs 24학번 / 25학번 vs 26학번) 금액이 높은 학번이 승리하며, 동점이면 무승부로 반환됩니다.")
  public BaseResponse<List<Week3ChallengeResponse>> getWeek3Challenge() {
    return BaseResponse.success(receiptService.getWeek3Challenge());
  }

  @GetMapping("/weekly-college-ranking")
  @Operation(
      summary = "주차별 단과대 소비금액 랭킹 조회",
      description = "요청한 주차(weekNumber: 1~3)의 단과대별 영수증 소비금액 합산 상위 4개 단과대를 반환합니다.")
  public BaseResponse<WeeklyRankingResponse> getWeeklyCollegeRanking(
      @RequestParam int weekNumber) {
    return BaseResponse.success(receiptService.getWeeklyCollegeRanking(weekNumber));
  }
}
