package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.receipt.dto.CollegeTotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalAllPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.UploadReceiptResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

  UploadReceiptResponse uploadReceiptImage(Long userId, MultipartFile image);

  TotalParticipationResponse getTotalParticipationCount();

  TotalAllPaymentResponse getTotalAllPaymentAmount();

  CollegeTotalPaymentResponse getCollegeTotalPaymentAmount(Long userId);
}
