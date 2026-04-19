package com.be.recheckbe.domain.receipt.service;

import com.be.recheckbe.domain.receipt.dto.AnalyzeReceiptResponse;
import com.be.recheckbe.domain.receipt.dto.CollegeTotalPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.ConfirmReceiptRequest;
import com.be.recheckbe.domain.receipt.dto.TotalAllPaymentResponse;
import com.be.recheckbe.domain.receipt.dto.TotalParticipationResponse;
import com.be.recheckbe.domain.receipt.dto.UploadReceiptResponse;
import com.be.recheckbe.domain.receipt.dto.Week2RankingGroupResponse;
import com.be.recheckbe.domain.receipt.dto.Week3ChallengeResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

  AnalyzeReceiptResponse analyzeReceiptImage(Long userId, MultipartFile image);

  UploadReceiptResponse confirmReceiptUpload(
      Long userId, MultipartFile image, ConfirmReceiptRequest data);

  TotalParticipationResponse getTotalParticipationCount();

  TotalAllPaymentResponse getTotalAllPaymentAmount();

  CollegeTotalPaymentResponse getCollegeTotalPaymentAmount(Long userId);

  List<Week2RankingGroupResponse> getWeek2Ranking();

  List<Week3ChallengeResponse> getWeek3Challenge();
}
