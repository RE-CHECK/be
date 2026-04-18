package com.be.recheckbe.domain.user.service;

import com.be.recheckbe.domain.receipt.repository.ReceiptRepository;
import com.be.recheckbe.domain.user.dto.UserDashboardResponse;
import com.be.recheckbe.domain.user.entity.User;
import com.be.recheckbe.domain.user.repository.UserRepository;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final ReceiptRepository receiptRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDashboardResponse getDashboard(Long userId) {
    User user =
        userRepository
            .findByIdWithCollegeInfo(userId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND));

    String collegeName = user.getDepartment().getCollege().getName();
    int totalPaymentAmount = receiptRepository.sumPaymentAmountByUserId(userId);

    return new UserDashboardResponse(user.getName(), collegeName, totalPaymentAmount);
  }
}
