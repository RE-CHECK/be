package com.be.recheckbe.domain.receipt.controller;

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
    public BaseResponse<String> uploadReceiptImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("image") MultipartFile image
    ) {
        String imageUrl = receiptService.uploadReceiptImage(userDetails.getId(), image);
        return BaseResponse.success(imageUrl);
    }
}