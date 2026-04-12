package com.be.recheckbe.domain.receipt.dto;

import com.be.recheckbe.global.ocr.dto.OcrExtractedData;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadReceiptResponse {

    private String imageUrl;
    private String storeName;
    private int paymentAmount;
    private String cardCompany;
    private String confirmNum;

    public static UploadReceiptResponse of(String imageUrl, OcrExtractedData ocrData) {
        return UploadReceiptResponse.builder()
                .imageUrl(imageUrl)
                .storeName(ocrData.getStoreName())
                .paymentAmount(ocrData.getPaymentAmount())
                .cardCompany(ocrData.getCardCompany())
                .confirmNum(ocrData.getConfirmNum())
                .build();
    }
}