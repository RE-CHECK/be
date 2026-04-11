package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.receipt.dto.CollegeTotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalAllPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.TotalPaymentResponse;
import com.be.recheckbe.domain.college.entity.College;
import com.be.recheckbe.domain.receipt.entity.Receipt;
import com.be.recheckbe.domain.receipt.repository.ReceiptRepository;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
import com.be.recheckbe.global.ocr.dto.OcrExtractedData;
import com.be.recheckbe.global.ocr.service.OcrService;
import com.be.recheckbe.global.s3.enums.PathName;
import com.be.recheckbe.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final S3Service s3Service;
    private final OcrService ocrService;
    private final ReceiptRepository receiptRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String uploadReceiptImage(Long userId, MultipartFile image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

        // ocr 호출
        OcrExtractedData ocrData = ocrService.extractReceiptData(image);
        // ocr 성공 시 s3로 업로드 (파일 고아(orphan) 방지)
        String imageUrl = s3Service.uploadFile(PathName.RECEIPT, image);

        Receipt receipt = Receipt.builder()
                .imageUrl(imageUrl)
                .paymentAmount(ocrData.getPaymentAmount())
                .storeName(ocrData.getStoreName())
                .cardCompany(ocrData.getCardCompany())
                .confirmNum(parseConfirmNum(ocrData.getConfirmNum()))
                .user(user)
                .build();

        receiptRepository.save(receipt);

        return imageUrl;
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
    public TotalPaymentResponse getTotalPaymentAmount(Long userId) {
        int total = receiptRepository.sumPaymentAmountByUserId(userId);
        return new TotalPaymentResponse(total);
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
        User user = userRepository.findByIdWithCollegeInfo(userId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

        College college = user.getDepartment().getCollege();
        int total = receiptRepository.sumPaymentAmountByCollegeId(college.getId());

        return new CollegeTotalPaymentResponse(college.getId(), college.getName(), total);
    }
}
