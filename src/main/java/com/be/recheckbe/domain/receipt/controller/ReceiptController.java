package com.be.recheckbe.domain.receipt.controller;

import com.be.recheckbe.domain.receipt.dto.CollegeTotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalAllPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.TotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.UploadReceiptResponse;
import com.be.recheckbe.domain.receipt.service.ReceiptService;
import com.be.recheckbe.global.response.BaseResponse;
import com.be.recheckbe.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "영수증 이미지 업로드", description = "영수증 이미지를 S3에 업로드합니다.")
    public BaseResponse<UploadReceiptResponse> uploadReceiptImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("image") MultipartFile image
    ) {
        return BaseResponse.success(receiptService.uploadReceiptImage(userDetails.getId(), image));
    }

    @GetMapping("/total-user-payment")
    @Operation(summary = "사용자 별 총 결제금액 조회", description = "로그인한 사용자의 영수증 결제금액 합산을 조회합니다.")
    public BaseResponse<TotalPaymentResponse> getTotalPaymentAmount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return BaseResponse.success(receiptService.getTotalPaymentAmount(userDetails.getId()));
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
    @Operation(summary = "단과대별 총 누적 소비금액 조회", description = "로그인한 사용자의 단과대에 속한 전체 사용자의 영수증 결제금액 합산을 조회합니다.")
    public BaseResponse<CollegeTotalPaymentResponse> getCollegeTotalPaymentAmount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return BaseResponse.success(receiptService.getCollegeTotalPaymentAmount(userDetails.getId()));
    }
}