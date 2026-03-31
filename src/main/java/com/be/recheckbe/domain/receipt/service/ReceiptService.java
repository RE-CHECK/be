package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.TotalPaymentResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

    String uploadReceiptImage(Long userId, MultipartFile image);

    TotalPaymentResponse getTotalPaymentAmount(Long userId);

    TotalParticipationResponse getTotalParticipationCount();
}
