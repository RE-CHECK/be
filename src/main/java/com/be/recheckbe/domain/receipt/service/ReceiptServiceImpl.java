package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.receipt.entity.Receipt;
import com.be.recheckbe.domain.receipt.repository.ReceiptRepository;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
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

        String imageUrl = s3Service.uploadFile(PathName.RECEIPT, image);
        int paymentAmount = ocrService.extractPaymentAmount(image);

        Receipt receipt = Receipt.builder()
                .imageUrl(imageUrl)
                .paymentAmount(paymentAmount)
                .user(user)
                .build();

        receiptRepository.save(receipt);

        return imageUrl;
    }
}
